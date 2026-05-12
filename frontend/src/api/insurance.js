import request from '@/utils/request'

export function getInsuranceList(data) {
  return request({ url: '/api/insurances/list', method: 'post', data })
}

export function getInsuranceById(id) {
  return request({ url: `/api/insurances/${id}`, method: 'get' })
}

export function createInsurance(data) {
  return request({ url: '/api/insurances', method: 'post', data })
}

export function updateInsurance(data) {
  return request({ url: '/api/insurances', method: 'put', data })
}

export function deleteInsurance(id) {
  return request({ url: `/api/insurances/${id}`, method: 'delete' })
}
