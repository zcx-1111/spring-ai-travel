import request from '@/utils/request'

// 查询景点分类列表
export function listCategorylogin(query) {
  return request({
    url: '/system/categorylogin/list',
    method: 'get',
    params: query
  })
}

// 查询景点分类详细
export function getCategorylogin(id) {
  return request({
    url: '/system/categorylogin/' + id,
    method: 'get'
  })
}

// 新增景点分类
export function addCategorylogin(data) {
  return request({
    url: '/system/categorylogin',
    method: 'post',
    data: data
  })
}

// 修改景点分类
export function updateCategorylogin(data) {
  return request({
    url: '/system/categorylogin',
    method: 'put',
    data: data
  })
}

// 删除景点分类
export function delCategorylogin(id) {
  return request({
    url: '/system/categorylogin/' + id,
    method: 'delete'
  })
}
