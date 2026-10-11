import request from '@/utils/request'

// 查询景点-标签关联列表
export function listRellogin(query) {
  return request({
    url: '/system/rellogin/list',
    method: 'get',
    params: query
  })
}

// 查询景点-标签关联详细
export function getRellogin(id) {
  return request({
    url: '/system/rellogin/' + id,
    method: 'get'
  })
}

// 新增景点-标签关联
export function addRellogin(data) {
  return request({
    url: '/system/rellogin',
    method: 'post',
    data: data
  })
}

// 修改景点-标签关联
export function updateRellogin(data) {
  return request({
    url: '/system/rellogin',
    method: 'put',
    data: data
  })
}

// 删除景点-标签关联
export function delRellogin(id) {
  return request({
    url: '/system/rellogin/' + id,
    method: 'delete'
  })
}
