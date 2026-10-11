import request from '@/utils/request'

/**
 * 获取当前游客的全部行程列表（用于个人主页统计与最近行程展示）
 * 复用已有后端接口：GET /api/travel/itinerary/list
 * 请求一次拉取较多条数（100条），前端计算旅行统计数据
 *
 * 返回（若依分页结构）：{ code, rows: 行程卡片数组, total: 总条数 }
 * rows 中每条包含：itineraryId, title, destination, days, budget, crowd,
 *                  startDate, spotCount, totalCost, createTime, planSource
 */
export function getMyAllItineraries() {
  return request({
    url: '/travel/itinerary/list',
    // 该接口前缀为 /api/travel，需覆盖默认的 /api/tour
    baseURL: '/api',
    method: 'get',
    params: { pageNum: 1, pageSize: 100 }
  })
}
