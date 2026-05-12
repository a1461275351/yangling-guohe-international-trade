import request from '@/utils/request'

export function getTaxRefundList(data) {
  return request({ url: '/api/tax-refunds/list', method: 'post', data })
}

export function getTaxRefundById(id) {
  return request({ url: `/api/tax-refunds/${id}`, method: 'get' })
}

export function createTaxRefund(data) {
  return request({ url: '/api/tax-refunds', method: 'post', data })
}

export function updateTaxRefund(data) {
  return request({ url: '/api/tax-refunds', method: 'put', data })
}

export function deleteTaxRefund(id) {
  return request({ url: `/api/tax-refunds/${id}`, method: 'delete' })
}

export function updateTaxRefundStatus(id, status) {
  return request({ url: `/api/tax-refunds/${id}/status`, method: 'put', params: { status } })
}
