import request from '@/utils/request'

/**
 * 调用后端AI生成行程方案
 * 接口地址：POST /api/travel/ai/generate
 * 注意：该接口前缀为 /api/travel（与登录注册的 /api/tour 不同），
 *       因此在本次请求中覆盖 baseURL，token 仍由请求拦截器自动携带。
 *
 * 请求参数：
 * {
 *   destination:  目的地城市，如 "桂林"
 *   startDate:    出行开始日期，格式 "yyyy-MM-dd"
 *   days:         出行天数（1~30）
 *   budget:       预算区间："经济型" / "中等" / "高端"
 *   crowd:        出行人群："单人" / "情侣" / "家庭亲子" / "朋友结伴"
 *   preferences:  出行偏好数组，如 ["自然风光", "美食探店"]
 *   remark:       额外备注（可空），如 "少走路、不早起"
 * }
 *
 * 返回约定：
 * { code: 200, msg: '生成成功', data: { planId: 123 } }
 * planId 为生成的行程方案主键，用于跳转对话页面查看方案
 */
export function generateTravelPlan(data) {
  return request({
    url: '/travel/ai/generate',
    // 覆盖默认的 /api/tour 前缀，最终请求路径 /api/travel/ai/generate
    baseURL: '/api',
    method: 'post',
    // AI生成耗时较长，超时时间放宽到90秒
    timeout: 90000,
    data
  })
}

/**
 * 获取行程完整详情（基础信息+每日行程+历史对话）
 * 接口地址：GET /api/travel/itinerary/get?itineraryId=xxx
 * 返回 data：{ itinerary, dayList, messages }
 */
export function getItinerary(itineraryId) {
  return request({
    url: '/travel/itinerary/get',
    baseURL: '/api',
    method: 'get',
    params: { itineraryId }
  })
}

/**
 * 多轮对话修改行程
 * 接口地址：POST /api/travel/ai/chat
 * 参数：{ itineraryId: 行程ID, userMessage: 用户提问 }
 * 返回 data：{ reply, itinerary, dayList, messages }
 */
export function chatWithAi(data) {
  return request({
    url: '/travel/ai/chat',
    baseURL: '/api',
    method: 'post',
    timeout: 60000,
    data
  })
}

/**
 * 分页查询我的行程方案列表
 * 接口地址：GET /api/travel/itinerary/list
 * 参数：pageNum 页码，pageSize 每页条数
 * 返回（若依分页结构）：{ code, rows: 行程卡片数组, total: 总条数 }
 */
export function listMyItineraries(params) {
  return request({
    url: '/travel/itinerary/list',
    baseURL: '/api',
    method: 'get',
    params
  })
}

/**
 * 删除行程方案（后端级联删除每日行程、景点关联与对话记录）
 * 接口地址：DELETE /api/travel/itinerary/remove?itineraryId=xxx
 */
export function removeItinerary(itineraryId) {
  return request({
    url: '/travel/itinerary/remove',
    baseURL: '/api',
    method: 'delete',
    params: { itineraryId }
  })
}
