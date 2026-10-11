import request from '@/utils/request'

/**
 * 游客登录
 * 后端接口约定：POST /api/tour/auth/login
 * 参数：{ username, password }
 * 返回：{ code: 200, msg: '登录成功', data: { token, userInfo: { id, username, nickName, avatar } } }
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 游客注册
 * 后端接口约定：POST /api/tour/auth/register
 * 参数：{ username, password, phone }（密码由后端加密后写入 tour_user 表）
 * 返回：{ code: 200, msg: '注册成功' }
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}
