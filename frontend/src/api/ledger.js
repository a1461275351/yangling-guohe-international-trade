import request from '@/utils/request'

export function getLedgerList(data) {
  return request.post('/api/ledger/list', data)
}

export function getLedgerById(id) {
  return request.get(`/api/ledger/${id}`)
}

export function createLedger(data) {
  return request.post('/api/ledger', data)
}

export function updateLedger(data) {
  return request.put('/api/ledger', data)
}

export function deleteLedger(id) {
  return request.delete(`/api/ledger/${id}`)
}

export function copyLedger(id) {
  return request.post(`/api/ledger/${id}/copy`)
}

export function generateFromContract(contractId) {
  return request.post(`/api/ledger/generate-from-contract/${contractId}`)
}

export function generateFromOrder(orderId) {
  return request.post(`/api/ledger/generate-from-order/${orderId}`)
}

export function generateDeclaration(ledgerId) {
  return request.post(`/api/ledger/${ledgerId}/generate-declaration`)
}

export function getLedgerDeclarations(ledgerId) {
  return request.get(`/api/ledger/${ledgerId}/declarations`)
}
