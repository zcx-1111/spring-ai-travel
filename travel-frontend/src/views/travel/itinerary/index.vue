<template>
  <div class="history-page">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-inner">
        <!-- 左侧：Logo与标题 -->
        <div class="nav-brand" @click="router.push('/travel/plan')">
          <span class="brand-logo">🌍</span>
          <span class="brand-text">AI 智能旅游规划助手</span>
        </div>
        <!-- 右侧：返回规划页 + 当前用户 + 退出登录 -->
        <div class="nav-user">
          <el-button type="primary" size="small" plain :icon="Plus" @click="router.push('/travel/plan')">
            返回规划页
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

    <!-- 主体内容 -->
    <main class="main-area">
      <!-- 页面标题区 -->
      <section class="page-head">
        <h1 class="page-title">📋 我的行程方案</h1>
        <p class="page-desc">这里保存着你生成过的所有AI行程，可随时查看、复用或删除</p>
      </section>

      <!-- 加载中骨架 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="6" animated />
      </div>

      <!-- 空状态：暂无行程 -->
      <el-empty v-else-if="planList.length === 0" description="你还没有生成过行程方案" class="empty-box">
        <el-button type="primary" round :icon="Promotion" @click="router.push('/travel/plan')">
          去规划行程
        </el-button>
      </el-empty>

      <!-- 行程卡片列表 -->
      <template v-else>
        <div v-loading="loading" class="card-grid">
          <el-card
            v-for="item in planList"
            :key="item.itineraryId"
            class="plan-card"
            shadow="hover"
          >
            <!-- 卡片头部：目的地 + 方案来源角标 -->
            <div class="card-top">
              <div class="card-dest">
                <span class="dest-icon">📍</span>
                <span class="dest-name">{{ item.destination }}</span>
              </div>
              <el-tag
                :type="item.planSource === 2 ? 'success' : 'primary'"
                size="small"
                effect="light"
              >{{ item.planSource === 2 ? '对话优化过' : 'AI生成' }}</el-tag>
            </div>

            <!-- 卡片标题 -->
            <div class="card-title">{{ item.title || item.destination + '行程方案' }}</div>

            <!-- 行程要素标签 -->
            <div class="card-tags">
              <el-tag size="small" effect="plain" type="warning">⏱ {{ item.days }}天</el-tag>
              <el-tag size="small" effect="plain" type="danger">💰 {{ item.budget }}</el-tag>
              <el-tag size="small" effect="plain" type="info">👥 {{ item.crowd }}</el-tag>
              <el-tag size="small" effect="plain" type="success">🏛 {{ item.spotCount || 0 }}个景点</el-tag>
            </div>

            <!-- 出发日期与创建时间 -->
            <div class="card-meta">
              <div class="meta-line">🗓 出发日期：{{ formatDate(item.startDate) }}</div>
              <div class="meta-line create-time">🕒 生成时间：{{ formatDateTime(item.createTime) }}</div>
            </div>

            <!-- 卡片操作按钮 -->
            <div class="card-actions">
              <el-button type="primary" size="small" :icon="View" @click="handleView(item)">
                查看详情
              </el-button>
              <el-button type="success" size="small" plain :icon="CopyDocument" @click="handleReuse(item)">
                复用行程
              </el-button>
              <el-button type="danger" size="small" plain :icon="Delete" @click="handleDelete(item)">
                删除
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 分页组件：每页6条 -->
        <div class="pager-wrap">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[6, 12, 18]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="loadPlanList"
          />
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, SwitchButton, Promotion, View, CopyDocument, Delete, User
} from '@element-plus/icons-vue'
import { listMyItineraries, removeItinerary } from '@/api/travel'

const router = useRouter()

/** 列表加载状态 */
const loading = ref(false)
/** 行程卡片列表 */
const planList = ref([])
/** 当前页码（从1开始） */
const pageNum = ref(1)
/** 每页条数（需求固定6条） */
const pageSize = ref(6)
/** 总条数 */
const total = ref(0)

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
 * 页面挂载：加载第一页行程列表
 */
onMounted(() => {
  loadPlanList()
})

/**
 * 查询当前登录用户的行程列表（分页）
 */
function loadPlanList() {
  loading.value = true
  listMyItineraries({ pageNum: pageNum.value, pageSize: pageSize.value })
    .then(res => {
      // 若依分页结构：res.rows 当前页数据，res.total 总条数
      planList.value = res.rows || []
      total.value = res.total || 0
      // 若删除后当前页已空且不是第一页，回退一页重新查询
      if (planList.value.length === 0 && pageNum.value > 1) {
        pageNum.value -= 1
        loadPlanList()
      }
    })
    .catch(() => {
      // 响应拦截器已统一弹出错误提示
      planList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 切换每页条数时回到第一页
 */
function handleSizeChange() {
  pageNum.value = 1
  loadPlanList()
}

/**
 * 查看详情：携带行程ID跳转对话页面
 */
function handleView(item) {
  router.push({ path: '/travel/chat', query: { itineraryId: item.itineraryId } })
}

/**
 * 复用行程：跳转规划页并携带reuseId，规划页据此回填原始出行参数
 */
function handleReuse(item) {
  router.push({ path: '/travel/plan', query: { reuseId: item.itineraryId } })
}

/**
 * 删除行程：二次确认弹窗 -> 调用删除接口 -> 刷新列表
 */
function handleDelete(item) {
  ElMessageBox.confirm(
    `确定删除「${item.destination}${item.days}天」行程方案吗？删除后该行程及其对话记录将无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(() => {
    return removeItinerary(item.itineraryId)
  }).then(() => {
    ElMessage.success('行程删除成功')
    // 刷新当前页列表
    loadPlanList()
  }).catch(() => {
    // 用户取消或删除失败，不做额外处理（失败时拦截器已提示）
  })
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

/** 日期格式化 yyyy-MM-dd */
function formatDate(val) {
  if (!val) return '待定'
  return String(val).substring(0, 10)
}

/** 日期时间格式化 yyyy-MM-dd HH:mm */
function formatDateTime(val) {
  if (!val) return '-'
  return String(val).substring(0, 16)
}
</script>

<style scoped>
/* 页面整体：延续清新浅色旅游风格 */
.history-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef7ff 0%, #f6fbf8 35%, #f4f8fb 100%);
}

/* 顶部导航栏（与规划/对话页保持一致） */
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
  max-width: 1200px;
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
.user-name {
  font-size: 14px;
  color: #445;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主体区域 */
.main-area {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 24px 50px;
}
.page-head {
  text-align: center;
  margin-bottom: 26px;
}
.page-title {
  font-size: 25px;
  font-weight: 700;
  color: #234;
  margin-bottom: 8px;
}
.page-desc {
  font-size: 13px;
  color: #7a8ea0;
}

/* 加载与空状态 */
.loading-wrap {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 26px;
}
.empty-box {
  padding: 70px 0 30px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 14px;
}

/* 卡片网格：每行3张 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}
.plan-card {
  border-radius: 14px;
  border: none;
  transition: transform 0.2s ease;
}
.plan-card:hover {
  transform: translateY(-4px);
}
.plan-card :deep(.el-card__body) {
  padding: 18px 18px 16px;
}

/* 卡片头部 */
.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.card-dest {
  display: flex;
  align-items: center;
  gap: 4px;
}
.dest-icon { font-size: 18px; }
.dest-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f5f7e;
}
.card-title {
  font-size: 14px;
  color: #5a6d80;
  margin-bottom: 12px;
  min-height: 20px;
}

/* 标签区 */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

/* 元信息 */
.card-meta {
  background: #f4f9fc;
  border-radius: 8px;
  padding: 8px 10px;
  margin-bottom: 14px;
}
.meta-line {
  font-size: 12px;
  color: #5a6d80;
  line-height: 1.9;
}
.create-time { color: #9aa8b5; }

/* 操作按钮区 */
.card-actions {
  display: flex;
  gap: 6px;
}
.card-actions .el-button {
  flex: 1;
  margin-left: 0;
  padding: 0;
}

/* 分页 */
.pager-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式：窄屏降为2列/1列 */
@media (max-width: 992px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .card-grid { grid-template-columns: 1fr; }
}
</style>
