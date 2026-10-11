import request from '@/utils/request'

// 查询景点主列表
export function listSpot(query) {
  return request({
    url: '/system/spot/list',
    method: 'get',
    params: query
  })
}

// 查询景点主详细
export function getSpot(id) {
  return request({
    url: '/system/spot/' + id,
    method: 'get'
  })
}

// 新增景点主
export function addSpot(data) {
  return request({
    url: '/system/spot',
    method: 'post',
    data: data
  })
}

// 修改景点主
export function updateSpot(data) {
  return request({
    url: '/system/spot',
    method: 'put',
    data: data
  })
}

// 删除景点主
export function delSpot(id) {
  return request({
    url: '/system/spot/' + id,
    method: 'delete'
  })
}

// 导入CSV到知识库（文件上传+省份+城市）
export function importCsvSpot(data) {
  return request({
    url: '/system/spot/importCsv',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: data
  })
}
