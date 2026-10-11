<template>
  <div class="plan-page">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-inner">
        <!-- 左侧：Logo与标题 -->
        <div class="nav-brand" @click="router.push('/travel/plan')">
          <span class="brand-logo">🌍</span>
          <span class="brand-text">AI 智能旅游规划助手</span>
        </div>
        <!-- 右侧：我的行程 + 当前登录用户 + 退出登录 -->
        <div class="nav-user">
          <el-button type="success" size="small" plain :icon="Tickets" @click="router.push('/travel/itinerary')">
            我的行程
          </el-button>
          <el-button type="primary" size="small" plain :icon="User" @click="router.push('/travel/profile')">
            个人主页
          </el-button>
          <el-avatar :size="32" class="user-avatar">
            {{ userName.charAt(0) }}
          </el-avatar>
          <span class="user-name">{{ userName }}</span>
          <el-button type="danger" plain size="small" :icon="SwitchButton" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="main-area">
      <!-- 欢迎语 -->
      <section class="welcome">
        <h1 class="welcome-title">告诉我你的旅行想法 ✈️</h1>
        <p class="welcome-desc">填写下方出行信息，AI 将结合景点知识库为你智能生成专属行程方案</p>
      </section>

      <!-- 行程信息表单卡片 -->
      <el-card class="plan-card" shadow="hover">
        <el-form
          ref="planFormRef"
          :model="planForm"
          :rules="planRules"
          label-width="100px"
          label-position="right"
          class="plan-form"
        >
          <el-row :gutter="20">
            <!-- 目的地 -->
            <el-col :span="12">
              <el-form-item label="目的地" prop="destination">
                <el-input
                  v-model.trim="planForm.destination"
                  placeholder="请输入目的地，如：桂林、张家界"
                  :prefix-icon="Location"
                  clearable
                  maxlength="30"
                />
              </el-form-item>
            </el-col>

            <!-- 出行开始日期 -->
            <el-col :span="12">
              <el-form-item label="开始日期" prop="startDate">
                <el-date-picker
                  v-model="planForm.startDate"
                  type="date"
                  placeholder="请选择出行开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledPastDate"
                  class="full-width"
                />
              </el-form-item>
            </el-col>

            <!-- 出行天数 -->
            <el-col :span="12">
              <el-form-item label="出行天数" prop="days">
                <el-input-number
                  v-model="planForm.days"
                  :min="1"
                  :max="30"
                  :step="1"
                  controls-position="right"
                  class="full-width"
                />
                <span class="field-tip">天（1~30天）</span>
              </el-form-item>
            </el-col>

            <!-- 预算区间 -->
            <el-col :span="12">
              <el-form-item label="预算区间" prop="budget">
                <el-select v-model="planForm.budget" placeholder="请选择预算区间" class="full-width">
                  <el-option label="经济型（省钱为主）" value="经济型" />
                  <el-option label="中等（性价比优先）" value="中等" />
                  <el-option label="高端（舒适体验）" value="高端" />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 出行人群 -->
            <el-col :span="12">
              <el-form-item label="出行人群" prop="crowd">
                <el-select v-model="planForm.crowd" placeholder="请选择出行人群" class="full-width">
                  <el-option label="单人出行" value="单人" />
                  <el-option label="情侣出游" value="情侣" />
                  <el-option label="家庭亲子" value="家庭亲子" />
                  <el-option label="朋友结伴" value="朋友结伴" />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 出行偏好（依据知识库景点数据归纳的9类，可多选；卡片式选择更直观） -->
            <el-col :span="24">
              <el-form-item label="出行偏好">
                <el-checkbox-group v-model="planForm.preferences" class="pref-group">
                  <el-checkbox
                    v-for="item in preferenceOptions"
                    :key="item.label"
                    :value="item.label"
                    class="pref-checkbox"
                  >
                    <span class="pref-card">
                      <span class="pref-icon">{{ item.icon }}</span>
                      <span class="pref-info">
                        <span class="pref-label">{{ item.label }}</span>
                        <span class="pref-desc">{{ item.desc }}</span>
                      </span>
                    </span>
                  </el-checkbox>
                </el-checkbox-group>
                <div class="pref-tip">可多选，AI 将优先安排符合你偏好的景点（不选则由 AI 综合推荐）</div>
              </el-form-item>
            </el-col>

            <!-- 额外备注 -->
            <el-col :span="24">
              <el-form-item label="额外备注">
                <el-input
                  v-model.trim="planForm.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  placeholder="其他自定义要求，如：少走路、早起困难、不吃辣、想去网红打卡地等（选填）"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 操作按钮区 -->
          <el-form-item class="form-actions">
            <el-button
              type="primary"
              size="large"
              class="generate-btn"
              :loading="generating"
              @click="handleGenerate"
            >
              {{ generating ? 'AI 正在为你规划行程...' : '生成 AI 行程方案' }}
            </el-button>
            <el-button size="large" :disabled="generating" @click="handleReset">
              重置表单
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </main>

    <!-- AI生成中的全屏遮罩 -->
    <div v-if="generating" class="loading-mask">
      <div class="loading-box">
        <el-icon class="loading-icon is-loading"><Loading /></el-icon>
        <p class="loading-text">正在检索景点知识库并生成方案，请耐心等待…</p>
        <p class="loading-sub">AI 生成通常需要 10~30 秒，请勿关闭页面</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Loading, SwitchButton, Tickets, User } from '@element-plus/icons-vue'
import { generateTravelPlan, getItinerary } from '@/api/travel'

const router = useRouter()
const route = useRoute()

/**
 * 出行偏好候选项
 * 依据知识库 biz_scenic_spot 全量景点（约2.9万条）的名称/简介关键词归纳：
 * 自然风光（山川湖海/森林峡谷）、人文古迹（古镇寺庙/遗址楼阁）、
 * 博物展览（博物馆/纪念馆/艺术馆，库中约1800+）、
 * 主题乐园（游乐园/欢乐世界，对应分类 THEME_PARK）、
 * 亲子研学（动物园/海洋馆/植物园）、温泉度假（温泉康养）、
 * 美食探店（小吃街/夜市）、购物休闲（步行街/商圈广场）、小众冷门（非热门打卡地）
 * label 为提交给后端的值（保持自由文本，后端检索/Prompt/落库均兼容）
 */
const preferenceOptions = [
  { label: '自然风光', icon: '🏞️', desc: '名山大川 · 湖泊海滨 · 森林峡谷' },
  { label: '人文古迹', icon: '🏛️', desc: '古镇古城 · 寺庙祠陵 · 遗址楼阁' },
  { label: '博物展览', icon: '🏺', desc: '博物馆 · 纪念馆 · 艺术展馆' },
  { label: '主题乐园', icon: '🎢', desc: '游乐园 · 欢乐世界 · 水上乐园' },
  { label: '亲子研学', icon: '👨‍👩‍👧', desc: '动物园 · 海洋馆 · 植物园' },
  { label: '温泉度假', icon: '♨️', desc: '温泉康养 · 休闲放松' },
  { label: '美食探店', icon: '🍜', desc: '特色小吃 · 老字号 · 夜市' },
  { label: '购物休闲', icon: '🛍️', desc: '步行街 · 商圈 · 城市广场' },
  { label: '小众冷门', icon: '🎯', desc: '人少景美 · 非常规打卡地' }
]

/** 生成按钮 loading 状态（同时控制全屏遮罩） */
const generating = ref(false)

/** 行程规划表单数据 */
const planForm = reactive({
  destination: '',      // 目的地
  startDate: '',        // 出行开始日期 yyyy-MM-dd
  days: 3,              // 出行天数（默认3天）
  budget: '',           // 预算区间
  crowd: '',            // 出行人群
  preferences: [],      // 出行偏好（数组）
  remark: ''            // 额外备注
})

/** 表单实例引用 */
const planFormRef = ref(null)

/** 当前登录用户名（登录成功时存储在 localStorage 的 Tour-User 中） */
const userName = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('Tour-User') || '{}')
    return user.nickName || user.username || '游客'
  } catch (e) {
    return '游客'
  }
})

/**
 * 页面挂载：若从"我的行程"点击【复用行程】进入（URL带reuseId），
 * 则读取该行程原始出行参数并自动回填表单，用户微调后即可重新生成
 */
onMounted(() => {
  const reuseId = route.query.reuseId
  if (!reuseId) return
  getItinerary(reuseId)
    .then(res => {
      const info = res.data?.itinerary || {}
      // 回填基础字段
      planForm.destination = info.destination || ''
      planForm.days = info.days || 3
      // 预算文案映射：后端返回"经济/舒适/高端"，表单option值为"经济型/中等/高端"
      planForm.budget = info.budget === '经济' ? '经济型'
        : info.budget === '高端' ? '高端' : '中等'
      planForm.crowd = info.crowd || ''
      planForm.preferences = Array.isArray(info.preferences) ? [...info.preferences] : []
      planForm.remark = info.remark || ''
      // 开始日期：只回填尚未过期的日期，已过期则留空让用户重新选择
      if (info.startDate && String(info.startDate).substring(0, 10) >= todayStr()) {
        planForm.startDate = String(info.startDate).substring(0, 10)
      }
      ElMessage.success(`已复用「${info.destination}」的出行参数，可调整后重新生成`)
      // 清除URL上的复用参数，避免刷新时重复回填
      router.replace({ path: '/travel/plan' })
    })
    .catch(() => {
      // 错误提示由响应拦截器统一处理
    })
})

/** 获取今天的 yyyy-MM-dd 字符串（用于判断复用的日期是否已过期） */
function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

/**
 * 禁用今天之前的日期
 * el-date-picker 的 disabled-date 回调，参数为日期对象
 */
function disabledPastDate(date) {
  // 今天零点
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

/** 表单校验规则 */
const planRules = {
  destination: [
    { required: true, message: '请输入目的地', trigger: 'blur' },
    { min: 2, max: 30, message: '目的地长度为 2 到 30 个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择出行开始日期', trigger: 'change' }
  ],
  days: [
    { required: true, message: '请填写出行天数', trigger: 'blur' },
    {
      // 自定义范围校验（el-input-number虽有min/max，手动输入仍需校验）
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请填写出行天数'))
        } else if (value < 1 || value > 30) {
          callback(new Error('出行天数范围为 1~30 天'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  budget: [
    { required: true, message: '请选择预算区间', trigger: 'change' }
  ],
  crowd: [
    { required: true, message: '请选择出行人群', trigger: 'change' }
  ]
}

/**
 * 提交表单：调用AI接口生成行程方案
 * 成功后携带行程ID跳转到对话页面 /travel/chat
 */
function handleGenerate() {
  planFormRef.value.validate(valid => {
    // 校验不通过时 Element Plus 会自动在字段下方给出红色提示
    if (!valid) {
      ElMessage.warning('请完善表单中标红的必填信息')
      return
    }
    generating.value = true
    // 组装请求参数
    const params = {
      destination: planForm.destination,
      startDate: planForm.startDate,
      days: planForm.days,
      budget: planForm.budget,
      crowd: planForm.crowd,
      preferences: planForm.preferences,
      remark: planForm.remark
    }
    generateTravelPlan(params)
      .then(res => {
        // 兼容后端返回 planId 或 id 两种字段命名
        const planId = res.data?.planId ?? res.data?.id
        if (!planId) {
          ElMessage.error('行程生成成功但未返回行程ID，请联系后端检查返回数据')
          return
        }
        ElMessage.success('行程方案生成成功，正在前往对话页面')
        // 携带行程ID跳转到对话页面（第三步），参数名按接口契约为 itineraryId
        router.push({ path: '/travel/chat', query: { itineraryId: planId } })
      })
      .finally(() => {
        generating.value = false
      })
  })
}

/** 重置表单为初始状态 */
function handleReset() {
  planFormRef.value?.resetFields()
  planForm.preferences = []
  planForm.days = 3
  planForm.remark = ''
}

/**
 * 退出登录：清除本地登录信息并返回登录页
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
  }).catch(() => {
    // 用户取消，不做处理
  })
}
</script>

<style scoped>
/* 页面整体：浅蓝灰渐变背景，清新旅游风 */
.plan-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef7ff 0%, #f6fbf8 40%, #ffffff 100%);
}

/* 顶部导航栏 */
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
  max-width: 1100px;
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
.brand-text {
  font-size: 17px;
  font-weight: 600;
  color: #2c6e8f;
}
.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-avatar {
  background: linear-gradient(135deg, #4facfe, #38b2a3);
  color: #fff;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
  color: #445;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主体区域 */
.main-area {
  max-width: 1100px;
  margin: 0 auto;
  padding: 36px 24px 60px;
}

/* 欢迎语 */
.welcome { text-align: center; margin-bottom: 28px; }
.welcome-title {
  font-size: 26px;
  font-weight: 700;
  color: #234;
  margin-bottom: 8px;
}
.welcome-desc {
  font-size: 14px;
  color: #7a8ea0;
}

/* 表单卡片 */
.plan-card {
  border-radius: 14px;
  border: none;
  background: rgba(255, 255, 255, 0.96);
}
.plan-card :deep(.el-card__body) {
  padding: 30px 32px 10px;
}
.plan-form { max-width: 900px; margin: 0 auto; }
.full-width { width: 100%; }
.field-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #9aa8b5;
}

/* ===== 出行偏好卡片式选择器 ===== */
.pref-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 一行3张卡片，共3行 */
  gap: 12px;
  width: 100%;
}
/* 隐藏 el-checkbox 原生勾选框，整体卡片即点击区域 */
.pref-checkbox {
  height: auto;
  margin-right: 0;
  align-items: stretch;
  border-radius: 10px;
}
.pref-checkbox :deep(.el-checkbox__input) {
  display: none;
}
.pref-checkbox :deep(.el-checkbox__label) {
  padding: 0;
  white-space: normal; /* 允许说明文字换行 */
}
.pref-card {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid #e3edf5;
  border-radius: 10px;
  background: #fafdff;
  cursor: pointer;
  transition: all 0.2s ease;
  box-sizing: border-box;
}
.pref-card:hover {
  border-color: #a8d4f0;
  background: #f2f9ff;
}
.pref-icon {
  font-size: 24px;
  line-height: 1;
}
.pref-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.pref-label {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
.pref-desc {
  font-size: 12px;
  color: #94a3b3;
  line-height: 1.3;
}
/* 选中态：主题蓝绿渐变边框 + 淡蓝底 */
.pref-checkbox :deep(.el-checkbox__input.is-checked + .el-checkbox__label) .pref-card {
  border-color: #38b2a3;
  background: linear-gradient(135deg, #eef8ff 0%, #ecfaf6 100%);
  box-shadow: 0 2px 8px rgba(56, 178, 163, 0.18);
}
.pref-checkbox :deep(.el-checkbox__input.is-checked + .el-checkbox__label) .pref-label {
  color: #1d8a7e;
}
/* 小屏自适应：平板2列、手机1列 */
@media (max-width: 900px) {
  .pref-group { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .pref-group { grid-template-columns: 1fr; }
}
.pref-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #9aa8b5;
}

/* 操作按钮区 */
.form-actions {
  margin-top: 14px;
  text-align: center;
}
.generate-btn {
  min-width: 220px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-right: 14px;
  background: linear-gradient(90deg, #4facfe, #38b2a3);
  border: none;
}
.generate-btn:hover { opacity: 0.92; }

/* 生成中全屏遮罩 */
.loading-mask {
  position: fixed;
  inset: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(3px);
}
.loading-box {
  padding: 36px 48px;
  border-radius: 14px;
  background: #fff;
  text-align: center;
  box-shadow: 0 10px 36px rgba(60, 120, 180, 0.18);
}
.loading-icon {
  font-size: 40px;
  color: #4facfe;
}
.loading-text {
  margin-top: 14px;
  font-size: 15px;
  font-weight: 600;
  color: #345;
}
.loading-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #9aa8b5;
}
</style>
