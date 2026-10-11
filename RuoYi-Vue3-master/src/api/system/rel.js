import request from '@/utils/request'

// 查询景点-标签关联列表
export function listRel(query) {
  return request({
    url: '/system/rel/list',
    method: 'get',
    params: query
  })
}

// 查询景点-标签关联详细
export function getRel(id) {
  return request({
    url: '/system/rel/' + id,
    method: 'get'
  })
}

// 新增景点-标签关联
export function addRel(data) {
  return request({
    url: '/system/rel',
    method: 'post',
    data: data
  })
}

// 修改景点-标签关联
export function updateRel(data) {
  return request({
    url: '/system/rel',
    method: 'put',
    data: data
  })
}

// 删除景点-标签关联
export function delRel(id) {
  return request({
    url: '/system/rel/' + id,
    method: 'delete'
  })
}
