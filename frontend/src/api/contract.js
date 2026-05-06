import request from '@/utils/request'

export function getContractList(data) {
  return request.post('/api/contracts/list', data)
}

export function getContractById(id) {
  return request.get(`/api/contracts/${id}`)
}

export function createContract(data) {
  return request.post('/api/contracts', data)
}

export function updateContract(data) {
  return request.put('/api/contracts', data)
}

export function updateContractStatus(id, status) {
  return request.put(`/api/contracts/${id}/status?status=${status}`)
}

export function deleteContract(id) {
  return request.delete(`/api/contracts/${id}`)
}

export function getExpiringContracts(params) {
  const days = typeof params === 'object' ? params.days : params
  return request.get(`/api/contracts/expiring?days=${days}`)
}

export function getExpiringStats() {
  return request.get('/api/contracts/expiring/stats')
}
