import { createRouter, createWebHistory } from 'vue-router'

// 公共路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    // 登录注册页（tab切换：登录/注册）
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录注册' }
  },
  {
    // AI行程规划首页（登录后进入）
    path: '/travel/plan',
    name: 'TravelPlan',
    component: () => import('@/views/travel/plan/index.vue'),
    meta: { title: 'AI行程规划', requiresAuth: true }
  },
  {
    // AI对话页面（行程结果+多轮对话）
    path: '/travel/chat',
    name: 'TravelChat',
    component: () => import('@/views/travel/chat/index.vue'),
    meta: { title: '行程对话', requiresAuth: true }
  },
  {
    // 我的行程历史（行程方案列表）
    path: '/travel/itinerary',
    name: 'TravelItinerary',
    component: () => import('@/views/travel/itinerary/index.vue'),
    meta: { title: '我的行程', requiresAuth: true }
  },
  {
    // 个人主页（用户资料 + 旅行统计 + 最近行程）
    path: '/travel/profile',
    name: 'TravelProfile',
    component: () => import('@/views/travel/profile/index.vue'),
    meta: { title: '个人主页', requiresAuth: true }
  }
]

// 创建路由实例（history 模式）
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 前置守卫：页面标题设置 + 登录态校验
router.beforeEach((to, from, next) => {
  // 动态设置浏览器标签页标题
  document.title = to.meta.title ? `${to.meta.title} - AI智能旅游规划助手` : 'AI智能旅游规划助手'
  // 需要登录的页面：本地无 token 时强制跳转登录页
  if (to.meta.requiresAuth && !localStorage.getItem('Tour-Token')) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
