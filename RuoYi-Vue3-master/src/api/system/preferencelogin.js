import request from '@/utils/request'

// 查询用户旅游偏好列表
export function listPreferencelogin(query) {
  return request({
    url: '/system/preferencelogin/list',
    method: 'get',
    params: query
  })
}

// 查询用户旅游偏好详细
export function getPreferencelogin(id) {
  return request({
    url: '/system/preferencelogin/' + id,
    method: 'get'
  })
}

// 新增用户旅游偏好
export function addPreferencelogin(data) {
  return request({
    url: '/system/preferencelogin',
    method: 'post',
    data: data
  })
}

// 修改用户旅游偏好
export function updatePreferencelogin(data) {
  return request({
    url: '/system/preferencelogin',
    method: 'put',
    data: data
  })
}

// 删除用户旅游偏好
export function delPreferencelogin(id) {
  return request({
    url: '/system/preferencelogin/' + id,
    method: 'delete'
  })
}
