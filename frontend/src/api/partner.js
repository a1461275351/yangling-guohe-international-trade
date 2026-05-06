import request from '@/utils/request'

export function getPartnerList(data) {
  return request.post('/api/partners/list', data)
}

export function getPartnerById(id) {
  return request.get(`/api/partners/${id}`)
}

export function createPartner(data) {
  return request.post('/api/partners', data)
}

export function updatePartner(data) {
  return request.put('/api/partners', data)
}

export function deletePartner(id) {
  return request.delete(`/api/partners/${id}`)
}

export function updatePartnerStatus(id, status) {
  return request.put(`/api/partners/${id}/status?status=${status}`)
}

export function batchUpdatePartnerStatus(ids, status) {
  return request.put('/api/partners/batch-status', { ids, status })
}

export function getActivePartners(type) {
  return request.get(`/api/partners/active?type=${type}`)
}
