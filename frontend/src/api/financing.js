import request from '@/utils/request'

export function getFinancingList(data) {
  return request({ url: '/api/financings/list', method: 'post', data })
}

export function getFinancingById(id) {
  return request({ url: `/api/financings/${id}`, method: 'get' })
}

export function createFinancing(data) {
  return request({ url: '/api/financings', method: 'post', data })
}

export function updateFinancing(data) {
  return request({ url: '/api/financings', method: 'put', data })
}

export function deleteFinancing(id) {
  return request({ url: `/api/financings/${id}`, method: 'delete' })
}
