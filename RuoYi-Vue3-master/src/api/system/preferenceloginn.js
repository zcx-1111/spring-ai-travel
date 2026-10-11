import request from '@/utils/request'

// 查询用户旅游偏好列表
export function listPreferenceloginn(query) {
  return request({
    url: '/system/preferenceloginn/list',
    method: 'get',
    params: query
  })
}

// 查询用户旅游偏好详细
export function getPreferenceloginn(id) {
  return request({
    url: '/system/preferenceloginn/' + id,
    method: 'get'
  })
}

// 新增用户旅游偏好
export function addPreferenceloginn(data) {
  return request({
    url: '/system/preferenceloginn',
    method: 'post',
    data: data
  })
}

// 修改用户旅游偏好
export function updatePreferenceloginn(data) {
  return request({
    url: '/system/preferenceloginn',
    method: 'put',
    data: data
  })
}

// 删除用户旅游偏好
export function delPreferenceloginn(id) {
  return request({
    url: '/system/preferenceloginn/' + id,
    method: 'delete'
  })
}
