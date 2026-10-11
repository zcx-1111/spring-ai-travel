<template>
  <div class="profile-page">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-inner">
        <!-- 左侧：Logo与标题 -->
        <div class="nav-brand" @click="router.push('/travel/plan')">
          <span class="brand-logo">🌍</span>
          <span class="brand-text">AI 智能旅游规划助手</span>
        </div>
        <!-- 右侧：导航按钮 + 用户 + 退出 -->
        <div class="nav-user">
          <el-button type="success" size="small" plain :icon="Tickets" @click="router.push('/travel/itinerary')">
            我的行程
          </el-button>
          <el-button type="primary" size="small" plain :icon="User" class="nav-profile-btn">
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
      <!-- ============ 个人资料横幅 ============ -->
      <section class="profile-banner">
        <div class="banner-bg"></div>
        <div class="banner-content">
          <!-- 用户头像 -->
          <div class="avatar-wrap">
            <el-avatar :size="90" class="big-avatar">
              {{ userName.charAt(0) }}
            </el-avatar>
          </div>
          <!-- 用户名与基本信息 -->
          <div class="user-info">
            <h2 class="user-nick-name">{{ userNickName }}</h2>
            <p class="user-account">@{{ userAccount }}</p>
            <div class="user-badges">
              <el-tag effect="dark" round size="small" type="success">🌍 旅行爱好者</el-tag>
              <el-tag effect="dark" round size="small" type="warning">✈️ AI行程用户</el-tag>
            </div>
          </div>
          <!-- 右侧快捷操作 -->
          <div class="banner-actions">
            <el-button type="primary" round :icon="Edit" @click="router.push('/travel/plan')">
              规划新行程
            </el-button>
          </div>
        </div>
      </section>

      <!-- ============ 旅行统计面板 ============ -->
      <section class="stats-section">
        <h3 class="section-title">📊 旅行统计</h3>
        <div v-loading="loading" class="stats-grid">
          <!-- 行程总数 -->
          <div class="stat-card stat-trips">
            <div class="stat-icon">🗺️</div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.totalTrips }}</span>
              <span class="stat-label">行程总数</span>
            </div>
          </div>
          <!-- 去过的城市 -->
          <div class="stat-card stat-cities">
            <div class="stat-icon">📍</div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.totalCities }}</span>
              <span class="stat-label">去过的城市</span>
            </div>
          </div>
          <!-- 旅行天数 -->
          <div class="stat-card stat-days">
            <div class="stat-icon">📅</div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.totalDays }}</span>
              <span class="stat-label">旅行天数</span>
            </div>
          </div>
          <!-- 游览景点数 -->
          <div class="stat-card stat-spots">
            <div class="stat-icon">🏛️</div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.totalSpots }}</span>
              <span class="stat-label">游览景点</span>
            </div>
          </div>
          <!-- 旅行总花费 -->
          <div class="stat-card stat-cost">
            <div class="stat-icon">💰</div>
            <div class="stat-body">
              <span class="stat-value">¥{{ formatMoney(stats.totalCost) }}</span>
              <span class="stat-label">人均总花费</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ============ 最近行程 ============ -->
      <section class="recent-section">
        <div class="section-header">
          <h3 class="section-title">🎒 最近行程</h3>
          <el-button
            v-if="recentTrips.length > 0"
            type="primary"
            text
            :icon="ArrowRight"
            @click="router.push('/travel/itinerary')"
          >
            查看全部
          </el-button>
        </div>

        <!-- 加载中骨架 -->
        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="4" animated />
        </div>

        <!-- 空状态：暂无行程 -->
        <el-empty
          v-else-if="recentTrips.length === 0"
          description="你还没有生成过行程方案"
          class="empty-box"
        >
          <el-button type="primary" round :icon="Promotion" @click="router.push('/travel/plan')">
            去规划行程
          </el-button>
        </el-empty>

        <!-- 行程卡片列表（最多展示4个） -->
        <div v-else class="card-grid">
          <el-card
            v-for="item in recentTrips"
            :key="item.itineraryId"
            class="trip-card"
            shadow="hover"
          >
            <!-- 卡片头部：目的地 + 来源角标 -->
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

            <!-- 出发日期 -->
            <div class="card-meta">
              <div class="meta-line">🗓 出发日期：{{ formatDate(item.startDate) }}</div>
              <div class="meta-line">🕒 人均花费：¥{{ formatMoney(item.totalCost) }}</div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button type="primary" size="small" :icon="View" @click="handleView(item)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, SwitchButton, Tickets, Edit, View, Promotion, ArrowRight
} from '@element-plus/icons-vue'
import { getMyAllItineraries } from '@/api/profile'

const router = useRouter()

/** 加载状态 */
const loading = ref(false)
/** 全部行程列表（用于统计与最近行程展示） */
const allTrips = ref([])

/** 当前登录用户信息（从 localStorage 读取） */
const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('Tour-User') || '{}')
  } catch (e) {
    return {}
  }
})

/** 用户昵称（优先昵称，其次用户名，兜底"游客"） */
const userName = computed(() => userInfo.value.nickName || userInfo.value.username || '游客')
const userNickName = computed(() => userInfo.value.nickName || userInfo.value.username || '游客')
const userAccount = computed(() => userInfo.value.username || 'unknown')

/** 旅行统计数据（从行程列表前端计算） */
const stats = computed(() => {
  const trips = allTrips.value
  // 统计不重复的目的地城市
  const citySet = new Set(trips.map(t => t.destination).filter(Boolean))
  return {
    totalTrips: trips.length,                              // 行程总数
    totalCities: citySet.size,                              // 去过的城市数
    totalDays: trips.reduce((sum, t) => sum + (t.days || 0), 0),        // 旅行总天数
    totalSpots: trips.reduce((sum, t) => sum + (t.spotCount || 0), 0),  // 游览景点总数
    totalCost: trips.reduce((sum, t) => sum + Number(t.totalCost || 0), 0) // 人均总花费
  }
})

/** 最近行程（取前4条，列表已按创建时间倒序） */
const recentTrips = computed(() => allTrips.value.slice(0, 4))

/**
 * 页面加载时拉取全部行程数据
 */
async function loadProfileData() {
  loading.value = true
  try {
    // request 拦截器返回完整响应体，分页接口结构为 { code, rows, total }
    const res = await getMyAllItineraries()
    allTrips.value = res.rows || []
  } catch (e) {
    // 错误提示已由响应拦截器处理，这里静默
  } finally {
    loading.value = false
  }
}

/**
 * 查看行程详情（跳转对话页面）
 */
function handleView(item) {
  router.push({ path: '/travel/chat', query: { id: item.itineraryId } })
}

/**
 * 退出登录：清除前台 token 与用户信息，跳转登录页
 */
function handleLogout() {
  localStorage.removeItem('Tour-Token')
  localStorage.removeItem('Tour-User')
  ElMessage.success('已安全退出')
  router.push('/login')
}

/**
 * 格式化日期：YYYY-MM-DD
 */
function formatDate(dateStr) {
  if (!dateStr) return '待定'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/**
 * 格式化金额：保留整数，千分位逗号
 */
function formatMoney(val) {
  if (!val && val !== 0) return '0'
  return Number(val).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

onMounted(() => {
  loadProfileData()
})
</script>

<style scoped>
/* ========== 页面整体 ========== */
.profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f5fffa 100%);
}

/* ========== 顶部导航栏 ========== */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.nav-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
}
.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.brand-logo { font-size: 26px; }
.brand-text {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}
.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nav-profile-btn { font-weight: 600; }
.user-avatar {
  background: linear-gradient(135deg, #4facfe, #38b2a3);
  color: #fff;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
  color: #4a5568;
  white-space: nowrap;
}

/* ========== 主体内容 ========== */
.main-area {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

/* ========== 个人资料横幅 ========== */
.profile-banner {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 28px;
}
.banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 30%, #4facfe 70%, #38b2a3 100%);
}
.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15), transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1), transparent 40%);
}
.banner-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 36px 32px;
}
.avatar-wrap { flex-shrink: 0; }
.big-avatar {
  background: rgba(255, 255, 255, 0.95);
  color: #4facfe;
  font-size: 36px;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
.user-info { flex: 1; }
.user-nick-name {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}
.user-account {
  margin: 0 0 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
.user-badges {
  display: flex;
  gap: 8px;
}
.banner-actions { flex-shrink: 0; }

/* ========== 通用区块标题 ========== */
.section-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* ========== 统计卡片 ========== */
.stats-section { margin-bottom: 32px; }
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
.stat-icon { font-size: 32px; line-height: 1; }
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
}
.stat-label {
  font-size: 13px;
  color: #94a3b8;
}
/* 各统计卡片主题色 */
.stat-trips { border-top: 3px solid #4facfe; }
.stat-cities { border-top: 3px solid #43e97b; }
.stat-days  { border-top: 3px solid #fa709a; }
.stat-spots { border-top: 3px solid #fee140; }
.stat-cost  { border-top: 3px solid #a78bfa; }

/* ========== 最近行程卡片 ========== */
.recent-section { margin-bottom: 32px; }
.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.trip-card { border-radius: 12px; }
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.card-dest {
  display: flex;
  align-items: center;
  gap: 4px;
}
.dest-icon { font-size: 18px; }
.dest-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}
.card-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 500;
  color: #4a5568;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.card-meta {
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
  margin-bottom: 10px;
}
.meta-line {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.8;
}
.card-actions {
  display: flex;
  gap: 8px;
}

/* ========== 空状态与加载 ========== */
.loading-wrap {
  padding: 20px;
  background: #fff;
  border-radius: 12px;
}
.empty-box { padding: 60px 0; }

/* ========== 响应式适配 ========== */
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
  .card-grid { grid-template-columns: 1fr; }
  .banner-content { flex-wrap: wrap; }
  .banner-actions { width: 100%; }
}
@media (max-width: 560px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .nav-user .user-name { display: none; }
  .nav-inner { padding: 0 12px; }
  .brand-text { display: none; }
}
</style>
