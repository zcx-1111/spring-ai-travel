import request from '@/utils/request'

// 查询用户旅游偏好列表
export function listPreference(query) {
  return request({
    url: '/system/preference/list',
    method: 'get',
    params: query
  })
}

// 查询用户旅游偏好详细
export function getPreference(id) {
  return request({
    url: '/system/preference/' + id,
    method: 'get'
  })
}

// 新增用户旅游偏好
export function addPreference(data) {
  return request({
    url: '/system/preference',
    method: 'post',
    data: data
  })
}

// 修改用户旅游偏好
export function updatePreference(data) {
  return request({
    url: '/system/preference',
    method: 'put',
    data: data
  })
}

// 删除用户旅游偏好
export function delPreference(id) {
  return request({
    url: '/system/preference/' + id,
    method: 'delete'
  })
}
