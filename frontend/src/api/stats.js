import request from '@/utils/request'

export function getCertificationDashboard() {
  return request({ url: '/api/stats/certification-dashboard', method: 'get' })
}

export function exportEnterprises() {
  return request({ url: '/api/stats/export/enterprises', method: 'get', responseType: 'blob' })
}

export function exportTradeStats() {
  return request({ url: '/api/stats/export/trade-stats', method: 'get', responseType: 'blob' })
}

export function exportServiceSummary() {
  return request({ url: '/api/stats/export/service-summary', method: 'get', responseType: 'blob' })
}
