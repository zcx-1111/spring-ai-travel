<template>
  <div class="app-container home">
    <!-- ===== 标题横幅 ===== -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">AI 智能旅游规划系统</h1>
        <p class="hero-subtitle">基于知识库的景点、美食、问答与智能推荐数据中心</p>
        <p class="hero-desc">
          覆盖全国 349 城市 29000+ 景点数据，为游客提供 AI 自动行程生成、多轮对话调整、景点知识库检索等服务
        </p>
      </div>
      <div class="hero-decoration">
        <span class="deco-circle deco-c1"></span>
        <span class="deco-circle deco-c2"></span>
        <span class="deco-circle deco-c3"></span>
      </div>
    </div>

    <!-- 加载中骨架 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 加载失败提示 -->
    <el-result
      v-else-if="loadError"
      icon="error"
      title="统计数据加载失败"
      sub-title="请检查后端服务是否正常启动，或稍后重试"
    >
      <template #extra>
        <el-button type="primary" @click="loadData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 数据大盘内容 -->
    <template v-else>
      <!-- ===== 第一行：四个统计数字卡片 ===== -->
      <div class="stat-grid">
        <div
          v-for="(card, i) in statCards"
          :key="i"
          class="stat-card"
          :style="{ '--accent': card.color, '--bg': card.bg }"
        >
          <div class="stat-card-icon" v-html="card.svg"></div>
          <div class="stat-card-info">
            <span class="stat-card-number">{{ card.value }}</span>
            <span class="stat-card-label">{{ card.label }}</span>
            <span class="stat-card-tip">实时统计</span>
          </div>
        </div>
      </div>

      <!-- ===== 第二行：省份TOP10 + 城市TOP8 ===== -->
      <div class="chart-grid">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-bar" style="background: #2196F3"></span>
            <span class="chart-title">各省份景点数量 TOP10</span>
          </div>
          <div ref="pieLevelRef" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-bar" style="background: #4DB6AC"></span>
            <span class="chart-title">城市景点数量 TOP8</span>
          </div>
          <div ref="barCityRef" class="chart-box"></div>
        </div>
      </div>

      <!-- ===== 第三行：7天问答趋势 + 提问分类 ===== -->
      <div class="chart-grid">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-bar" style="background: #FFB74D"></span>
            <span class="chart-title">最近 7 天问答访问量趋势</span>
          </div>
          <div ref="lineTrendRef" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-bar" style="background: #7E57C2"></span>
            <span class="chart-title">用户提问分类分布</span>
          </div>
          <div ref="pieCatRef" class="chart-box"></div>
        </div>
      </div>

      <!-- ===== 第四行：最近问答记录表格 ===== -->
      <div class="chart-card table-card-full">
        <div class="chart-header">
          <span class="chart-bar" style="background: #26A69A"></span>
          <span class="chart-title">最近 10 条问答记录</span>
        </div>
        <el-table :data="recentChats" size="default" style="width: 100%" class="clean-table">
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="question" label="提问内容" min-width="280" show-overflow-tooltip />
          <el-table-column prop="answer" label="AI 回答简要" min-width="380" show-overflow-tooltip />
          <el-table-column prop="askTime" label="提问时间" width="180" />
        </el-table>
      </div>
    </template>
  </div>
</template>

<script setup name="Index">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getHomeData } from '@/api/travel/stats'

/** 加载状态 */
const loading = ref(true)
/** 加载失败 */
const loadError = ref(false)

/** SVG 线性图标 */
const svgMap = `
<svg viewBox="0 0 24 24" fill="none" stroke="#2196F3" stroke-width="2" width="28" height="28">
  <path d="M9 20l-5.5-2v-14l5.5 2 6-2 5.5 2v14l-5.5-2z"/><path d="M9 6v12"/><path d="M15 4v12"/>
</svg>`
const svgPlan = `
<svg viewBox="0 0 24 24" fill="none" stroke="#4DB6AC" stroke-width="2" width="28" height="28">
  <rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4"/><path d="M16 2v4"/><path d="M8 12h8"/>
</svg>`
const svgChat = `
<svg viewBox="0 0 24 24" fill="none" stroke="#FFB74D" stroke-width="2" width="28" height="28">
  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
</svg>`
const svgUser = `
<svg viewBox="0 0 24 24" fill="none" stroke="#F06292" stroke-width="2" width="28" height="28">
  <circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8"/>
</svg>`

/** 四个统计卡片 */
const statCards = ref([
  { label: '景点总数', value: 0, svg: svgMap, color: '#2196F3', bg: 'rgba(33,150,243,0.08)' },
  { label: '行程方案数', value: 0, svg: svgPlan, color: '#4DB6AC', bg: 'rgba(77,182,172,0.08)' },
  { label: '问答交互数', value: 0, svg: svgChat, color: '#FFB74D', bg: 'rgba(255,183,77,0.08)' },
  { label: '注册游客数', value: 0, svg: svgUser, color: '#F06292', bg: 'rgba(240,98,146,0.08)' }
])

/** 最近问答列表 */
const recentChats = ref([])

/** echarts 图表实例引用 */
const pieLevelRef = ref(null)
const barCityRef = ref(null)
const lineTrendRef = ref(null)
const pieCatRef = ref(null)
let chartInstances = []

/** 主题配色 */
const THEME = {
  blue: '#2196F3',
  teal: '#4DB6AC',
  orange: '#FFB74D',
  purple: '#7E57C2',
  pink: '#F06292',
  green: '#66BB6A',
  cyan: '#26C6DA',
  grid: '#f0f2f5',
  text: '#37474F',
  textLight: '#90A4AE'
}

/**
 * 加载首页统计数据
 */
async function loadData() {
  loading.value = true
  loadError.value = false
  try {
    const res = await getHomeData()
    const data = res.data || {}
    // 填充统计卡片
    statCards.value[0].value = data.scenicTotal || 0
    statCards.value[1].value = data.planTotal || 0
    statCards.value[2].value = data.chatTotal || 0
    statCards.value[3].value = data.userTotal || 0
    // 填充问答表格
    recentChats.value = data.recentChats || []

    // 先关闭 loading，让图表容器渲染到 DOM
    loading.value = false
    await nextTick()
    initCharts(data)
  } catch (e) {
    console.error('[首页统计] 加载失败:', e)
    loading.value = false
    loadError.value = true
  }
}

/**
 * 初始化全部 echarts 图表
 */
function initCharts(data) {
  // 销毁旧实例
  chartInstances.forEach(c => c && c.dispose())
  chartInstances = []

  // 公共配置
  const commonAxis = {
    axisLine: { lineStyle: { color: THEME.textLight } },
    axisTick: { show: false },
    axisLabel: { color: THEME.textLight, fontSize: 11 },
    splitLine: { lineStyle: { color: THEME.grid, type: 'dashed' } }
  }

  // 1. 横向柱状图：各省份景点数量 TOP10
  if (pieLevelRef.value) {
    const chart = echarts.init(pieLevelRef.value)
    const provData = (data.provinceTopSpots || []).reverse()
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, textStyle: { fontSize: 12 } },
      grid: { left: '3%', right: '10%', bottom: '3%', top: '3%', containLabel: true },
      xAxis: { type: 'value', name: '景点数', ...commonAxis },
      yAxis: { type: 'category', data: provData.map(d => d.name), ...commonAxis, splitLine: { show: false } },
      series: [{
        type: 'bar',
        data: provData.map(d => d.value),
        barWidth: '55%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: THEME.teal },
            { offset: 1, color: THEME.blue }
          ]),
          borderRadius: [0, 6, 6, 0]
        },
        label: { show: true, position: 'right', fontSize: 11, color: THEME.text }
      }]
    })
    chartInstances.push(chart)
  }

  // 2. 柱状图：城市景点 TOP8
  if (barCityRef.value) {
    const chart = echarts.init(barCityRef.value)
    const cityData = data.cityTopSpots || []
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '6%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: cityData.map(d => d.name), ...commonAxis, axisLabel: { ...commonAxis.axisLabel, rotate: 30 } },
      yAxis: { type: 'value', name: '景点数', ...commonAxis },
      series: [{
        type: 'bar',
        data: cityData.map(d => d.value),
        barWidth: '45%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: THEME.teal },
            { offset: 1, color: THEME.cyan }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        label: { show: true, position: 'top', fontSize: 11, color: THEME.text }
      }]
    })
    chartInstances.push(chart)
  }

  // 3. 折线图：最近7天问答趋势
  if (lineTrendRef.value) {
    const chart = echarts.init(lineTrendRef.value)
    const trend = data.chatTrend7Days || []
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '5%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: { type: 'category', data: trend.map(d => d.date ? d.date.substring(5) : ''), ...commonAxis, boundary: false },
      yAxis: { type: 'value', name: '问答数', minInterval: 1, ...commonAxis },
      series: [{
        type: 'line',
        data: trend.map(d => d.count),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: THEME.orange },
        itemStyle: { color: THEME.orange, borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,183,77,0.25)' },
            { offset: 1, color: 'rgba(255,183,77,0.02)' }
          ])
        }
      }]
    })
    chartInstances.push(chart)
  }

  // 4. 饼图：提问分类分布
  if (pieCatRef.value) {
    const chart = echarts.init(pieCatRef.value)
    chart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 12, color: THEME.textLight } },
      series: [{
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '45%'],
        data: data.chatCategories || [],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}', fontSize: 11, color: THEME.text }
      }],
      color: [THEME.blue, THEME.teal, THEME.orange, THEME.purple, THEME.pink]
    })
    chartInstances.push(chart)
  }
}

/**
 * 窗口缩放时重绘图表
 */
function handleResize() {
  chartInstances.forEach(c => c && c.resize())
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(c => c && c.dispose())
})
</script>

<style scoped>
.home {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* ===== 标题横幅 ===== */
.hero-banner {
  position: relative;
  background: linear-gradient(135deg, #1F4E5F 0%, #2196F3 50%, #4DB6AC 100%);
  border-radius: 12px;
  padding: 28px 36px;
  margin-bottom: 20px;
  overflow: hidden;
}
.hero-content {
  position: relative;
  z-index: 2;
}
.hero-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}
.hero-subtitle {
  margin: 0 0 8px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}
.hero-desc {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
}
/* 装饰圆 */
.hero-decoration {
  position: absolute;
  right: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.deco-c1 {
  width: 180px;
  height: 180px;
  right: -40px;
  top: -60px;
}
.deco-c2 {
  width: 120px;
  height: 120px;
  right: 60px;
  bottom: -50px;
}
.deco-c3 {
  width: 80px;
  height: 80px;
  right: 200px;
  top: 20px;
}

/* ===== 加载骨架 ===== */
.loading-wrap {
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
}

/* ===== 统计卡片网格 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--accent);
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}
.stat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-card-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-card-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1.2;
}
.stat-card-label {
  font-size: 14px;
  color: #37474F;
  font-weight: 500;
}
.stat-card-tip {
  font-size: 11px;
  color: #B0BEC5;
}

/* ===== 图表网格 ===== */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.chart-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.chart-bar {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  flex-shrink: 0;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #37474F;
}
.chart-box {
  width: 100%;
  height: 320px;
}

/* ===== 表格卡片 ===== */
.table-card-full {
  margin-bottom: 20px;
}
:deep(.clean-table) {
  --el-table-border-color: #f0f2f5;
}
:deep(.clean-table th) {
  background: #fafbfc !important;
  color: #546E7A;
  font-weight: 600;
  font-size: 13px;
}
:deep(.clean-table td) {
  font-size: 13px;
  color: #455A64;
}

/* ===== 响应式适配 ===== */
@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-box {
    height: 260px;
  }
  .hero-banner {
    padding: 20px 20px;
  }
  .hero-title {
    font-size: 20px;
  }
  .stat-card {
    padding: 16px 14px;
    gap: 12px;
  }
  .stat-card-icon {
    width: 40px;
    height: 40px;
  }
  .stat-card-number {
    font-size: 22px;
  }
}
</style>
