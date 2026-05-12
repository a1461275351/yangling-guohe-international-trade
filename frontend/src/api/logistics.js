import request from '@/utils/request'

export function getLogisticsList(data) {
  return request({ url: '/api/logistics/list', method: 'post', data })
}

export function getLogisticsById(id) {
  return request({ url: `/api/logistics/${id}`, method: 'get' })
}

export function createLogistics(data) {
  return request({ url: '/api/logistics', method: 'post', data })
}

export function updateLogistics(data) {
  return request({ url: '/api/logistics', method: 'put', data })
}

export function deleteLogistics(id) {
  return request({ url: `/api/logistics/${id}`, method: 'delete' })
}

export function updateLogisticsStatus(id, status) {
  return request({ url: `/api/logistics/${id}/status`, method: 'put', params: { status } })
}
