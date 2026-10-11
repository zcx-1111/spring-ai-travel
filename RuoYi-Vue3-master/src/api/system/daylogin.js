import request from '@/utils/request'

// 查询行程每日详情列表
export function listDaylogin(query) {
  return request({
    url: '/system/daylogin/list',
    method: 'get',
    params: query
  })
}

// 查询行程每日详情详细
export function getDaylogin(id) {
  return request({
    url: '/system/daylogin/' + id,
    method: 'get'
  })
}

// 新增行程每日详情
export function addDaylogin(data) {
  return request({
    url: '/system/daylogin',
    method: 'post',
    data: data
  })
}

// 修改行程每日详情
export function updateDaylogin(data) {
  return request({
    url: '/system/daylogin',
    method: 'put',
    data: data
  })
}

// 删除行程每日详情
export function delDaylogin(id) {
  return request({
    url: '/system/daylogin/' + id,
    method: 'delete'
  })
}
