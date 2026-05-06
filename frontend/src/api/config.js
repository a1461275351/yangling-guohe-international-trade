import request from '@/utils/request'

export function getConfigItemList(data) {
  return request.post('/api/config/items/list', data)
}

export function createConfigItem(data) {
  return request.post('/api/config/items', data)
}

export function updateConfigItem(data) {
  return request.put('/api/config/items', data)
}

export function deleteConfigItem(id) {
  return request.delete(`/api/config/items/${id}`)
}

export function updateConfigItemStatus(id, status) {
  return request.put(`/api/config/items/${id}/status?status=${status}`)
}

export function getConfigValueList(data) {
  return request.post('/api/config/values/list', data)
}

export function createConfigValue(data) {
  return request.post('/api/config/values', data)
}

export function updateConfigValue(data) {
  return request.put('/api/config/values', data)
}

export function deleteConfigValue(id) {
  return request.delete(`/api/config/values/${id}`)
}

export function updateConfigValueStatus(id, status) {
  return request.put(`/api/config/values/${id}/status?status=${status}`)
}

export function batchUpdateValueStatus(ids, status) {
  return request.put('/api/config/values/batch-status', { ids, status })
}

export function getActiveValuesByItemCode(code) {
  return request.get(`/api/config/values/by-item-code?code=${code}`)
}
