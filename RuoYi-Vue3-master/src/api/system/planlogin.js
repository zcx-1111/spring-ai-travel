import request from '@/utils/request'

// 查询行程方案主列表
export function listPlanlogin(query) {
  return request({
    url: '/system/planlogin/list',
    method: 'get',
    params: query
  })
}

// 查询行程方案主详细
export function getPlanlogin(id) {
  return request({
    url: '/system/planlogin/' + id,
    method: 'get'
  })
}

// 新增行程方案主
export function addPlanlogin(data) {
  return request({
    url: '/system/planlogin',
    method: 'post',
    data: data
  })
}

// 修改行程方案主
export function updatePlanlogin(data) {
  return request({
    url: '/system/planlogin',
    method: 'put',
    data: data
  })
}

// 删除行程方案主
export function delPlanlogin(id) {
  return request({
    url: '/system/planlogin/' + id,
    method: 'delete'
  })
}
