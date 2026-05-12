import request from '@/utils/request'

export function getSettlementList(data) {
  return request({ url: '/api/settlements/list', method: 'post', data })
}

export function getSettlementById(id) {
  return request({ url: `/api/settlements/${id}`, method: 'get' })
}

export function createSettlement(data) {
  return request({ url: '/api/settlements', method: 'post', data })
}

export function updateSettlement(data) {
  return request({ url: '/api/settlements', method: 'put', data })
}

export function deleteSettlement(id) {
  return request({ url: `/api/settlements/${id}`, method: 'delete' })
}
