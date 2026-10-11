import request from '@/utils/request'

// 查询对话消息列表
export function listMessagelogin(query) {
  return request({
    url: '/system/messagelogin/list',
    method: 'get',
    params: query
  })
}

// 查询对话消息详细
export function getMessagelogin(id) {
  return request({
    url: '/system/messagelogin/' + id,
    method: 'get'
  })
}

// 新增对话消息
export function addMessagelogin(data) {
  return request({
    url: '/system/messagelogin',
    method: 'post',
    data: data
  })
}

// 修改对话消息
export function updateMessagelogin(data) {
  return request({
    url: '/system/messagelogin',
    method: 'put',
    data: data
  })
}

// 删除对话消息
export function delMessagelogin(id) {
  return request({
    url: '/system/messagelogin/' + id,
    method: 'delete'
  })
}
