import request from '@/utils/request'

// 查询对话会话列表
export function listSessionlogin(query) {
  return request({
    url: '/system/sessionlogin/list',
    method: 'get',
    params: query
  })
}

// 查询对话会话详细
export function getSessionlogin(id) {
  return request({
    url: '/system/sessionlogin/' + id,
    method: 'get'
  })
}

// 新增对话会话
export function addSessionlogin(data) {
  return request({
    url: '/system/sessionlogin',
    method: 'post',
    data: data
  })
}

// 修改对话会话
export function updateSessionlogin(data) {
  return request({
    url: '/system/sessionlogin',
    method: 'put',
    data: data
  })
}

// 删除对话会话
export function delSessionlogin(id) {
  return request({
    url: '/system/sessionlogin/' + id,
    method: 'delete'
  })
}
