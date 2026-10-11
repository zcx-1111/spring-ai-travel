import request from '@/utils/request'

// 查询行程每日详情列表
export function listDay(query) {
  return request({
    url: '/system/day/list',
    method: 'get',
    params: query
  })
}

// 查询行程每日详情详细
export function getDay(id) {
  return request({
    url: '/system/day/' + id,
    method: 'get'
  })
}

// 新增行程每日详情
export function addDay(data) {
  return request({
    url: '/system/day',
    method: 'post',
    data: data
  })
}

// 修改行程每日详情
export function updateDay(data) {
  return request({
    url: '/system/day',
    method: 'put',
    data: data
  })
}

// 删除行程每日详情
export function delDay(id) {
  return request({
    url: '/system/day/' + id,
    method: 'delete'
  })
}
