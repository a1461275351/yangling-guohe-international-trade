import request from '@/utils/request'

export function getGoodsList(data) {
  return request.post('/api/goods/list', data)
}

export function getGoodsById(id) {
  return request.get(`/api/goods/${id}`)
}

export function createGoods(data) {
  return request.post('/api/goods', data)
}

export function updateGoods(data) {
  return request.put('/api/goods', data)
}

export function deleteGoods(id) {
  return request.delete(`/api/goods/${id}`)
}

export function batchDeleteGoods(ids) {
  return request.delete('/api/goods/batch', { data: ids })
}

export function getGoodsSelection() {
  return request.get('/api/goods/selection')
}
