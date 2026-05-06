import request from '@/utils/request'

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function register(data) {
  return request.post('/api/auth/register', data)
}

export function resetPassword(data) {
  return request.post('/api/auth/reset-password', data)
}
