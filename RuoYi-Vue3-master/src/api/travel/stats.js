import request from '@/utils/request'

/**
 * 获取首页数据大盘全部统计数据
 * 接口地址：GET /travel/stats/getHomeData
 * 返回 data 结构：
 * {
 *   scenicTotal: 景点总数,
 *   planTotal: 行程方案数,
 *   chatTotal: 问答交互数,
 *   userTotal: 注册游客数,
 *   spotLevels: [{ name, value }] 景点等级分布,
 *   cityTopSpots: [{ name, value }] 城市景点TOP8,
 *   chatTrend7Days: [{ date, count }] 近7天问答趋势,
 *   chatCategories: [{ name, value }] 提问分类分布,
 *   recentChats: [{ question, answer, askTime }] 最近10条问答
 * }
 */
export function getHomeData() {
  return request({
    url: '/travel/stats/getHomeData',
    method: 'get',
    // 统计查询涉及多表聚合，放宽到 30 秒
    timeout: 30000
  })
}
