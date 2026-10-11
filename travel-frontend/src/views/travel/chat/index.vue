<template>
  <div class="chat-page">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-inner">
        <!-- 左侧：Logo与标题 -->
        <div class="nav-brand" @click="router.push('/travel/plan')">
          <span class="brand-logo">🌍</span>
          <span class="brand-text">AI 智能旅游规划助手</span>
        </div>
        <!-- 右侧：返回规划页 + 我的行程 + 当前用户 + 退出登录 -->
        <div class="nav-user">
          <el-button size="small" plain :icon="Back" @click="router.push('/travel/plan')">
            返回规划页
          </el-button>
          <el-button type="success" size="small" plain :icon="Tickets" @click="router.push('/travel/itinerary')">
            我的行程
          </el-button>
          <el-button type="primary" size="small" plain :icon="User" @click="router.push('/travel/profile')">
            个人主页
          </el-button>
          <el-avatar :size="32" class="user-avatar">{{ userName.charAt(0) }}</el-avatar>
          <span class="user-name">{{ userName }}</span>
          <el-button type="danger" plain size="small" :icon="SwitchButton" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主体：左右两栏布局 -->
    <main class="main-area">
      <!-- ============ 左侧：AI生成的完整行程方案 ============ -->
      <section class="itinerary-panel">
        <div class="panel-card">
          <!-- 卡片标题与操作按钮 -->
          <div class="panel-header">
            <h2 class="panel-title">🗺️ 本次旅游行程方案</h2>
            <div class="panel-actions">
              <el-button type="primary" size="small" :icon="Check" @click="handleSave">
                保存行程
              </el-button>
              <el-button size="small" :icon="RefreshRight" @click="handleReplan">
                重新规划
              </el-button>
            </div>
          </div>

          <!-- 行程加载中骨架 -->
          <div v-if="loading" class="loading-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在加载你的专属行程…</span>
          </div>

          <!-- 加载失败 / 无行程ID -->
          <el-result v-else-if="loadError" icon="warning" :title="loadError">
            <template #extra>
              <el-button type="primary" @click="router.push('/travel/plan')">返回重新规划</el-button>
            </template>
          </el-result>

          <!-- 行程内容 -->
          <template v-else-if="itinerary.itineraryId">
            <!-- 基础信息 -->
            <div class="plan-base">
              <h3 class="plan-title">{{ itinerary.title || itinerary.destination + '行程方案' }}</h3>
              <div class="base-tags">
                <el-tag type="primary" effect="light">📍 {{ itinerary.destination }}</el-tag>
                <el-tag type="success" effect="light">📅 {{ formatDate(itinerary.startDate) }} ~ {{ formatDate(itinerary.endDate) }}</el-tag>
                <el-tag type="warning" effect="light">⏱ {{ itinerary.days }}天</el-tag>
                <el-tag type="danger" effect="light">💰 {{ itinerary.budget }}</el-tag>
                <el-tag type="info" effect="light">👥 {{ itinerary.crowd }}</el-tag>
              </div>
              <div v-if="itinerary.preferences && itinerary.preferences.length" class="base-prefs">
                <span class="prefs-label">出行偏好：</span>
                <el-tag v-for="p in itinerary.preferences" :key="p" size="small" round effect="plain" class="pref-tag">{{ p }}</el-tag>
              </div>
              <el-alert
                v-if="itinerary.summary"
                :title="itinerary.summary"
                type="success"
                :closable="false"
                show-icon
                class="plan-summary"
              />
            </div>

            <!-- 按天分段展示每日行程 -->
            <div class="day-list">
              <div v-for="day in dayList" :key="day.dayNum" class="day-card">
                <div class="day-header">
                  <span class="day-badge">Day{{ day.dayNum }}</span>
                  <span class="day-theme">{{ day.dayTheme }}</span>
                  <!-- 预算口径为人均：住宿按房间分摊（双人同住更省），返程日不含住宿，不含往返大交通 -->
                  <span
                    class="day-budget"
                    title="人均口径：含当天门票；住宿按房间分摊（双人同住一间更省），最后一天中午返程不含住宿；不含往返大交通"
                  >人均当日预算 ¥{{ formatMoney(day.dailyBudget) }}</span>
                </div>

                <!-- 当日景点安排 -->
                <div class="day-section">
                  <div class="section-label">🏛 景点安排</div>
                  <div v-if="day.spots && day.spots.length" class="spot-list">
                    <div v-for="spot in day.spots" :key="spot.sortNum" class="spot-item">
                      <div class="spot-main">
                        <span class="spot-order">{{ spot.sortNum }}</span>
                        <span class="spot-name">{{ spot.spotName }}</span>
                        <el-tag v-if="spot.spotLevel" size="small" type="warning" effect="plain">{{ spot.spotLevel }}</el-tag>
                      </div>
                      <div class="spot-tip">{{ spot.playTip }}</div>
                    </div>
                  </div>
                  <div v-else class="empty-line">当日为自由活动，可在右侧对话中让AI补充景点安排</div>
                </div>

                <!-- 美食推荐 -->
                <div class="day-section">
                  <div class="section-label">🍜 美食推荐</div>
                  <div class="section-text">
                    可在当日活动区域搜索当地老字号与高分特色餐厅；若想获得具体餐厅与小吃推荐，直接在右侧问AI（如：第{{ day.dayNum }}天附近有什么好吃的）。
                  </div>
                </div>

                <!-- 交通建议 -->
                <div v-if="day.trafficDesc" class="day-section">
                  <div class="section-label">🚌 交通建议</div>
                  <div class="section-text">{{ day.trafficDesc }}</div>
                </div>

                <!-- 住宿推荐 -->
                <div v-if="day.hotelRecommend" class="day-section">
                  <div class="section-label">🏨 住宿推荐</div>
                  <div class="section-text">{{ day.hotelRecommend }}</div>
                </div>

                <!-- 注意事项 -->
                <div v-if="day.attention" class="day-section">
                  <div class="section-label">⚠️ 注意事项</div>
                  <div class="section-text">{{ day.attention }}</div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </section>

      <!-- ============ 右侧：多轮对话聊天窗口 ============ -->
      <section class="chat-panel">
        <div class="panel-card chat-card">
          <!-- 对话标题 -->
          <div class="chat-header">
            <span class="chat-title">💬 与AI旅行规划师对话</span>
            <span class="chat-sub">可随时调整天数、预算、景点与项目</span>
          </div>

          <!-- 消息列表区域 -->
          <div ref="messageBoxRef" class="message-box">
            <!-- 首次进入无历史消息时的欢迎语 -->
            <div v-if="!loading && messages.length === 0" class="msg-row ai">
              <div class="msg-avatar">🤖</div>
              <div class="msg-bubble ai-bubble">
                你的{{ itinerary.destination }}{{ itinerary.days }}天行程已经生成好啦！<br/>
                你可以这样告诉我调整：<br/>
                · "行程增加到5天"<br/>
                · "预算改成经济型"<br/>
                · "最后一个景点换一个"<br/>
                · "想多吃当地美食"
              </div>
            </div>

            <!-- 历史消息 + 实时消息 -->
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="msg-row"
              :class="msg.role === 'user' ? 'user' : 'ai'"
            >
              <div v-if="msg.role !== 'user'" class="msg-avatar">🤖</div>
              <div class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
                <span style="white-space: pre-wrap;">{{ msg.content }}</span>
              </div>
              <div v-if="msg.role === 'user'" class="msg-avatar user-avatar-text">我</div>
            </div>

            <!-- AI回复中的loading气泡 -->
            <div v-if="aiReplying" class="msg-row ai">
              <div class="msg-avatar">🤖</div>
              <div class="msg-bubble ai-bubble typing">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
                <span class="typing-text">AI 正在结合知识库更新行程…</span>
              </div>
            </div>
          </div>

          <!-- 快捷提问按钮 -->
          <div class="quick-asks">
            <el-button
              v-for="q in quickQuestions"
              :key="q"
              size="small"
              round
              :disabled="aiReplying || !itinerary.itineraryId"
              @click="sendQuickQuestion(q)"
            >{{ q }}</el-button>
          </div>

          <!-- 输入区 -->
          <div class="input-area">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              resize="none"
              maxlength="300"
              show-word-limit
              placeholder="输入你想调整的内容，回车发送，Shift+回车换行…"
              :disabled="!itinerary.itineraryId"
              @keydown.enter.exact.prevent="handleSend"
            />
            <el-button
              type="primary"
              class="send-btn"
              :loading="aiReplying"
              :disabled="!inputText.trim() || !itinerary.itineraryId"
              @click="handleSend"
            >发 送</el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, SwitchButton, Check, RefreshRight, Loading, Tickets, User } from '@element-plus/icons-vue'
import { getItinerary, chatWithAi } from '@/api/travel'

const route = useRoute()
const router = useRouter()

/** 路由携带的行程ID */
const itineraryId = route.query.itineraryId

/** 页面加载状态 */
const loading = ref(false)
/** 加载失败提示 */
const loadError = ref('')
/** AI回复中状态 */
const aiReplying = ref(false)

/** 行程基础信息 */
const itinerary = reactive({
  itineraryId: null,
  title: '',
  destination: '',
  days: null,
  startDate: '',
  endDate: '',
  budget: '',
  crowd: '',
  preferences: [],
  summary: '',
  totalCost: null
})

/** 每日行程列表 */
const dayList = ref([])
/** 对话消息列表 [{ role: 'user'|'assistant', content, sendTime }] */
const messages = ref([])
/** 输入框内容 */
const inputText = ref('')

/** 消息列表DOM引用，用于自动滚动到底部 */
const messageBoxRef = ref(null)

/** 快捷提问选项 */
const quickQuestions = ['行程增加到5天', '预算改成经济型', '换一个景点', '想多吃当地美食']

/** 当前登录用户名 */
const userName = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('Tour-User') || '{}')
    return user.nickName || user.username || '游客'
  } catch (e) {
    return '游客'
  }
})

/**
 * 页面挂载：校验行程ID并加载行程详情与历史对话
 */
onMounted(() => {
  if (!itineraryId) {
    loadError.value = '缺少行程ID，请从行程规划页重新生成方案'
    return
  }
  loadItinerary()
})

/**
 * 调用后端加载行程详情
 */
function loadItinerary() {
  loading.value = true
  loadError.value = ''
  getItinerary(itineraryId)
    .then(res => {
      const data = res.data || {}
      // 填充基础信息
      Object.assign(itinerary, data.itinerary || {})
      dayList.value = data.dayList || []
      messages.value = data.messages || []
      scrollToBottom()
    })
    .catch(() => {
      loadError.value = '行程加载失败，可能网络异常或该行程不属于当前账号'
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 发送消息（按钮点击 / 回车）
 * 校验空内容 -> 乐观追加用户气泡 -> 请求AI -> 同步更新行程与对话
 */
function handleSend() {
  const content = inputText.value.trim()
  // 表单校验：空内容禁止发送
  if (!content) {
    ElMessage.warning('请输入要发送的内容')
    return
  }
  if (aiReplying.value) return

  // 先在界面追加用户消息（乐观渲染，体验更流畅）
  messages.value.push({ role: 'user', content })
  inputText.value = ''
  aiReplying.value = true
  scrollToBottom()

  chatWithAi({ itineraryId: itineraryId, userMessage: content })
    .then(res => {
      const data = res.data || {}
      // 同步更新左侧行程卡片内容
      if (data.itinerary) {
        Object.assign(itinerary, data.itinerary)
      }
      if (data.dayList) {
        dayList.value = data.dayList
      }
      // 以后端返回的完整消息记录为准（包含已落库的用户消息与AI回复）
      if (data.messages && data.messages.length) {
        messages.value = data.messages
      } else if (data.reply) {
        // 兜底：后端没返回全量消息时，仅追加AI回复
        messages.value.push({ role: 'assistant', content: data.reply })
      }
      ElMessage.success('行程已根据你的要求更新')
    })
    .catch(() => {
      // 异常时移除刚才乐观追加的用户消息，避免界面与实际不一致
      messages.value.pop()
      // request.js已统一弹错误提示，这里无需重复提示
    })
    .finally(() => {
      aiReplying.value = false
      nextTick(scrollToBottom)
    })
}

/**
 * 快捷提问：点击后直接发送
 */
function sendQuickQuestion(question) {
  inputText.value = question
  handleSend()
}

/**
 * 消息列表自动滚动到底部
 */
function scrollToBottom() {
  nextTick(() => {
    const box = messageBoxRef.value
    if (box) {
      box.scrollTop = box.scrollHeight
    }
  })
}

/**
 * 保存行程：行程方案在AI生成及每次对话调整时均已实时落库，
 * 点击后给出确认提示并可直接前往"我的行程"列表查看
 */
function handleSave() {
  ElMessageBox.confirm('当前行程方案已实时保存，是否前往「我的行程」查看全部行程？', '保存成功', {
    confirmButtonText: '去我的行程',
    cancelButtonText: '继续调整',
    type: 'success'
  }).then(() => {
    router.push('/travel/itinerary')
  }).catch(() => {})
}

/**
 * 重新规划：返回规划页
 */
function handleReplan() {
  ElMessageBox.confirm('重新规划将返回表单页填写新的出行信息，当前行程不会被删除，确定继续吗？', '重新规划', {
    confirmButtonText: '去重新规划',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    router.push('/travel/plan')
  }).catch(() => {})
}

/**
 * 退出登录
 */
function handleLogout() {
  ElMessageBox.confirm('确定要退出当前登录账号吗？', '退出提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('Tour-Token')
    localStorage.removeItem('Tour-User')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

/** 金额格式化：保留整数或两位小数 */
function formatMoney(val) {
  if (val === null || val === undefined || val === '') return '0'
  return Number(val).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

/** 日期格式化：兼容 "2026-10-01" / "2026-10-01T00:00:00" / 时间戳字符串 */
function formatDate(val) {
  if (!val) return '待定'
  return String(val).substring(0, 10)
}
</script>

<style scoped>
/* 页面整体：延续清新浅色风格 */
.chat-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef7ff 0%, #f6fbf8 30%, #f4f8fb 100%);
}

/* 顶部导航栏（与规划页保持一致） */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 12px rgba(80, 140, 200, 0.08);
  backdrop-filter: blur(8px);
}
.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  height: 60px;
  margin: 0 auto;
  padding: 0 24px;
}
.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.brand-logo { font-size: 26px; }
.brand-text { font-size: 17px; font-weight: 600; color: #2c6e8f; }
.nav-user { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  background: linear-gradient(135deg, #4facfe, #38b2a3);
  color: #fff;
  font-weight: 600;
}
.user-avatar-text {
  background: linear-gradient(135deg, #4facfe, #38b2a3);
  color: #fff;
  font-size: 13px;
}
.user-name {
  font-size: 14px;
  color: #445;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主体：左右两栏 */
.main-area {
  display: flex;
  gap: 18px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px 28px;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 两栏通用面板 */
.panel-card {
  background: rgba(255, 255, 255, 0.97);
  border-radius: 14px;
  box-shadow: 0 6px 24px rgba(80, 140, 200, 0.1);
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 左侧行程面板：占55%，内容可滚动 */
.itinerary-panel {
  flex: 0 0 55%;
  min-width: 0;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eef3f7;
  flex-shrink: 0;
}
.panel-title {
  font-size: 17px;
  font-weight: 700;
  color: #234;
}
.loading-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 60px 0;
  color: #5a8;
  font-size: 14px;
}

/* 行程内容滚动区 */
.panel-card > .plan-base,
.panel-card > .day-list {
  overflow-y: auto;
}
.plan-base {
  padding: 18px 20px 8px;
  flex-shrink: 0;
}
.plan-title {
  font-size: 19px;
  font-weight: 700;
  color: #1f5f7e;
  margin-bottom: 12px;
}
.base-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.base-prefs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #6a7d90;
}
.pref-tag { color: #2c6e8f; }
.plan-summary { margin-bottom: 14px; }

.day-list {
  padding: 6px 20px 20px;
  overflow-y: auto;
  flex: 1;
}
.day-card {
  border: 1px solid #e6f0f6;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
  background: #fbfdff;
}
.day-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.day-badge {
  background: linear-gradient(135deg, #4facfe, #38b2a3);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 10px;
}
.day-theme {
  font-size: 15px;
  font-weight: 600;
  color: #2c5f78;
  flex: 1;
}
.day-budget {
  font-size: 13px;
  color: #e67e22;
  font-weight: 600;
}
.day-section { margin-top: 10px; }
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #345;
  margin-bottom: 6px;
}
.section-text {
  font-size: 13px;
  color: #5a6d80;
  line-height: 1.7;
  background: #f4f9fc;
  border-radius: 8px;
  padding: 8px 10px;
}
.spot-list { display: flex; flex-direction: column; gap: 8px; }
.spot-item {
  background: #f4f9fc;
  border-radius: 8px;
  padding: 8px 10px;
}
.spot-main { display: flex; align-items: center; gap: 8px; }
.spot-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #4facfe;
  color: #fff;
  font-size: 12px;
  flex-shrink: 0;
}
.spot-name {
  font-size: 14px;
  font-weight: 600;
  color: #234;
}
.spot-tip {
  margin-top: 4px;
  margin-left: 28px;
  font-size: 12px;
  color: #7a8ea0;
  line-height: 1.6;
}
.empty-line {
  font-size: 12px;
  color: #9aa8b5;
  padding: 6px 2px;
}

/* 右侧对话面板：占45% */
.chat-panel {
  flex: 1;
  min-width: 0;
}
.chat-header {
  padding: 14px 20px;
  border-bottom: 1px solid #eef3f7;
  flex-shrink: 0;
}
.chat-title {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #234;
}
.chat-sub {
  font-size: 12px;
  color: #9aa8b5;
}

/* 消息列表 */
.message-box {
  flex: 1;
  overflow-y: auto;
  padding: 18px 18px 8px;
  background: linear-gradient(180deg, #fafcfe 0%, #f3f9f7 100%);
}
.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 16px;
}
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: #eaf4fb;
  flex-shrink: 0;
}
.msg-bubble {
  max-width: 78%;
  padding: 10px 13px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(90, 140, 180, 0.08);
}
.ai-bubble {
  background: #fff;
  color: #345;
  border-top-left-radius: 2px;
}
.user-bubble {
  background: linear-gradient(135deg, #4facfe, #3d9bd8);
  color: #fff;
  border-top-right-radius: 2px;
}

/* AI打字中动画 */
.typing {
  display: flex;
  align-items: center;
  gap: 6px;
}
.typing .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4facfe;
  animation: blink 1.2s infinite ease-in-out;
}
.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }
.typing-text {
  margin-left: 6px;
  font-size: 12px;
  color: #8aa;
}
@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}

/* 快捷提问 */
.quick-asks {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 18px;
  border-top: 1px solid #eef3f7;
  flex-shrink: 0;
}

/* 输入区 */
.input-area {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 10px 18px 16px;
  border-top: 1px solid #eef3f7;
  flex-shrink: 0;
}
.send-btn {
  height: 40px;
  padding: 0 22px;
  font-weight: 600;
  background: linear-gradient(90deg, #4facfe, #38b2a3);
  border: none;
}
.send-btn:hover { opacity: 0.92; }
</style>
