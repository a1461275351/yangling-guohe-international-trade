import request from '@/utils/request'

export function getFileList(data) {
  return request.post('/api/files/list', data)
}

export function uploadFile(file, businessType) {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) formData.append('businessType', businessType)
  return request.post('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteFile(id) {
  return request.delete(`/api/files/${id}`)
}

export function batchDeleteFiles(ids) {
  return request.delete('/api/files/batch', { data: ids })
}

export function renameFile(id, name) {
  return request.put(`/api/files/${id}/rename?name=${encodeURIComponent(name)}`)
}

export function getDownloadUrl(id) {
  return `/api/files/download/${id}`
}
