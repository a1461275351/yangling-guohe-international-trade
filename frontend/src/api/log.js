import request from '@/utils/request'

export function getLogList(data) {
  return request({ url: '/api/logs/list', method: 'post', data })
}
