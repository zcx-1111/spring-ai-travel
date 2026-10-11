import request from '@/utils/request'

// 查询景点标签列表
export function listTaglogin(query) {
  return request({
    url: '/system/taglogin/list',
    method: 'get',
    params: query
  })
}

// 查询景点标签详细
export function getTaglogin(id) {
  return request({
    url: '/system/taglogin/' + id,
    method: 'get'
  })
}

// 新增景点标签
export function addTaglogin(data) {
  return request({
    url: '/system/taglogin',
    method: 'post',
    data: data
  })
}

// 修改景点标签
export function updateTaglogin(data) {
  return request({
    url: '/system/taglogin',
    method: 'put',
    data: data
  })
}

// 删除景点标签
export function delTaglogin(id) {
  return request({
    url: '/system/taglogin/' + id,
    method: 'delete'
  })
}
