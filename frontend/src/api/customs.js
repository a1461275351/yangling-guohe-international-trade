import request from '@/utils/request'

export function getCustomsList(data) {
  return request.post('/api/customs/list', data)
}

export function getCustomsById(id) {
  return request.get(`/api/customs/${id}`)
}

export function importCustoms(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/customs/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
