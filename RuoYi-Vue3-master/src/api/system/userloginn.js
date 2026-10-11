import request from '@/utils/request'

// 查询游客用户列表
export function listUserloginn(query) {
  return request({
    url: '/system/userloginn/list',
    method: 'get',
    params: query
  })
}

// 查询游客用户详细
export function getUserloginn(id) {
  return request({
    url: '/system/userloginn/' + id,
    method: 'get'
  })
}

// 新增游客用户
export function addUserloginn(data) {
  return request({
    url: '/system/userloginn',
    method: 'post',
    data: data
  })
}

// 修改游客用户
export function updateUserloginn(data) {
  return request({
    url: '/system/userloginn',
    method: 'put',
    data: data
  })
}

// 删除游客用户
export function delUserloginn(id) {
  return request({
    url: '/system/userloginn/' + id,
    method: 'delete'
  })
}
