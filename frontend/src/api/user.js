import request from '@/utils/request'

export function getUserList(data) {
  return request.post('/api/users/list', data)
}

export function updateUserStatus(id, status) {
  return request.put(`/api/users/${id}/status?status=${status}`)
}

export function resetUserPassword(id) {
  return request.put(`/api/users/${id}/reset-password`)
}

export function getUserInfo() {
  return request.get('/api/users/info')
}

export function updateUserInfo(data) {
  return request.put('/api/users/info', data)
}

export function changePassword(data) {
  return request.put('/api/users/password', data)
}

export function getApplyList(params) {
  return request.post('/api/user-applies/list', params)
}

export function approveApply(id) {
  return request.put(`/api/user-applies/${id}/approve`)
}

export function rejectApply(id, reason) {
  return request.put(`/api/user-applies/${id}/reject?reason=${encodeURIComponent(reason)}`)
}

export function batchApprove(ids) {
  return request.put('/api/user-applies/batch-approve', ids)
}
