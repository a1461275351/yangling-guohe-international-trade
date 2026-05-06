import request from '@/utils/request'

export function getTenantList(data) {
  return request.post('/api/tenants/list', data)
}

export function getTenantById(id) {
  return request.get(`/api/tenants/${id}`)
}

export function createTenant(data) {
  return request.post('/api/tenants', data)
}

export function updateTenant(data) {
  return request.put('/api/tenants', data)
}

export function updateTenantStatus(id, status) {
  return request.put(`/api/tenants/${id}/status?status=${status}`)
}

export function deleteTenant(id) {
  return request.delete(`/api/tenants/${id}`)
}

export function getAllTenants() {
  return request.get('/api/tenants/all')
}
