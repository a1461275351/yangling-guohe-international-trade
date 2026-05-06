import request from '@/utils/request'

export function getOrderList(data) {
  return request.post('/api/orders/list', data)
}

export function getOrderById(id) {
  return request.get(`/api/orders/${id}`)
}

export function createOrder(data) {
  return request.post('/api/orders', data)
}

export function updateOrder(id, data) {
  return request.put(`/api/orders/${id}`, data)
}

export function updateOrderStatus(id, status) {
  return request.put(`/api/orders/${id}/status?status=${status}`)
}

export function deleteOrder(id) {
  return request.delete(`/api/orders/${id}`)
}
