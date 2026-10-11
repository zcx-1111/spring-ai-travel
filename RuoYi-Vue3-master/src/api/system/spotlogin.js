import request from '@/utils/request'

// 查询每日行程景点关联列表
export function listSpotlogin(query) {
  return request({
    url: '/system/spotlogin/list',
    method: 'get',
    params: query
  })
}

// 查询每日行程景点关联详细
export function getSpotlogin(id) {
  return request({
    url: '/system/spotlogin/' + id,
    method: 'get'
  })
}

// 新增每日行程景点关联
export function addSpotlogin(data) {
  return request({
    url: '/system/spotlogin',
    method: 'post',
    data: data
  })
}

// 修改每日行程景点关联
export function updateSpotlogin(data) {
  return request({
    url: '/system/spotlogin',
    method: 'put',
    data: data
  })
}

// 删除每日行程景点关联
export function delSpotlogin(id) {
  return request({
    url: '/system/spotlogin/' + id,
    method: 'delete'
  })
}
