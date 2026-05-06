import request from '@/utils/request'

export function getTemplateList(data) {
  return request.post('/api/templates/list', data)
}

export function getTemplateById(id) {
  return request.get(`/api/templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/api/templates', data)
}

export function updateTemplate(data) {
  return request.put('/api/templates', data)
}

export function deleteTemplate(id) {
  return request.delete(`/api/templates/${id}`)
}

export function getTemplatesByType(type) {
  return request.get(`/api/templates/by-type?type=${type}`)
}

/**
 * 上传模板文件
 */
export function uploadTemplate(formData) {
  return request.post('/api/templates/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 更新模板文件（重新上传）
 */
export function updateTemplateFile(id, formData) {
  return request.post(`/api/templates/upload/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取ONLYOFFICE编辑器配置
 */
export function getOnlyOfficeConfig(id, mode = 'edit') {
  return request.get(`/api/templates/onlyoffice/config/${id}?mode=${mode}`)
}

/**
 * 下载模板文件
 */
export function downloadTemplate(id) {
  return request.get(`/api/templates/download/${id}`, {
    responseType: 'blob'
  })
}
