import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 前台游客专用 axios 封装
 * 1. 统一前缀 /api/tour，所有接口走该前缀
 * 2. token 存储在 localStorage（key: Tour-Token）
 * 3. 统一处理错误提示与登录过期跳转
 */
const service = axios.create({
  // 接口公共前缀
  baseURL: '/api/tour',
  // 请求超时时间：AI行程生成/对话调整涉及大模型调用，需预留较长时间，
  // 关闭推理后单次约10~30秒，这里统一设为120秒避免前端超时
  timeout: 120000
})

// 请求拦截器：自动携带前台游客 token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('Tour-Token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器：统一处理业务状态码
service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = res.data.msg || '系统未知错误'
  // 登录过期/未登录：清除本地登录信息并跳转登录页
  if (code === 401) {
    localStorage.removeItem('Tour-Token')
    localStorage.removeItem('Tour-User')
    ElMessage.error('登录已过期，请重新登录')
    router.push('/login')
    return Promise.reject(new Error(msg))
  }
  if (code !== 200) {
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
  // 直接返回响应体，页面中拿到的就是 { code, msg, data }
  return res.data
}, error => {
  ElMessage.error(error.message || '网络请求异常，请稍后重试')
  return Promise.reject(error)
})

export default service
