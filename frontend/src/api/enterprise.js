import request from '@/utils/request'

export function getEnterpriseList(data) {
  return request({ url: '/api/enterprises/list', method: 'post', data })
}

export function getEnterpriseById(id) {
  return request({ url: `/api/enterprises/${id}`, method: 'get' })
}

export function createEnterprise(data) {
  return request({ url: '/api/enterprises', method: 'post', data })
}

export function updateEnterprise(data) {
  return request({ url: '/api/enterprises', method: 'put', data })
}

export function deleteEnterprise(id) {
  return request({ url: `/api/enterprises/${id}`, method: 'delete' })
}

export function updateEnterpriseStatus(id, status) {
  return request({ url: `/api/enterprises/${id}/status`, method: 'put', params: { status } })
}

export function getActiveEnterprises() {
  return request({ url: '/api/enterprises/active', method: 'get' })
}
