import request from '@/utils/request'
import type {
  DocSealVO,
  DocSealCreateDTO,
  DocSealUpdateDTO,
  DocSealQueryDTO,
  DocSealGrantVO,
  DocSealGrantDTO,
  DocSignatureVO,
  DocSignatureSignDTO,
  DocSignatureVerifyDTO,
  DocSignatureVerifyResultVO,
  DocSignatureLogVO,
  DocSignatureLogQueryDTO
} from '@/types/signature'
import type { PageResult } from '@/types/template'

// ==================== 印章管理接口 ====================

export const getSealPage = (params: DocSealQueryDTO) => {
  return request<PageResult<DocSealVO>>({
    url: '/doc/seal/page',
    method: 'get',
    params
  })
}

export const getSealDetail = (id: number) => {
  return request<DocSealVO>({
    url: `/doc/seal/${id}`,
    method: 'get'
  })
}

export const createSeal = (data: DocSealCreateDTO) => {
  return request<number>({
    url: '/doc/seal',
    method: 'post',
    data
  })
}

export const updateSeal = (data: DocSealUpdateDTO) => {
  return request<void>({
    url: '/doc/seal',
    method: 'put',
    data
  })
}

export const deleteSeal = (id: number) => {
  return request<void>({
    url: `/doc/seal/${id}`,
    method: 'delete'
  })
}

export const uploadSealImage = (sealId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request<string>({
    url: `/doc/seal/upload-image/${sealId}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getSealImageUrl = (sealId: number) => {
  return `/api/doc/seal/image/${sealId}`
}

export const grantSeal = (data: DocSealGrantDTO) => {
  return request<void>({
    url: '/doc/seal/grant',
    method: 'post',
    data
  })
}

export const revokeGrant = (grantId: number) => {
  return request<void>({
    url: `/doc/seal/revoke/${grantId}`,
    method: 'post'
  })
}

export const getSealGrants = (sealId: number) => {
  return request<DocSealGrantVO[]>({
    url: `/doc/seal/grants/${sealId}`,
    method: 'get'
  })
}

export const listAvailableSeals = () => {
  return request<DocSealVO[]>({
    url: '/doc/seal/available',
    method: 'get'
  })
}

// ==================== 签章功能接口 ====================

export const signDocument = (data: DocSignatureSignDTO) => {
  return request<DocSignatureVO>({
    url: '/doc/signature/sign',
    method: 'post',
    data
  })
}

export const verifySignature = (data: DocSignatureVerifyDTO) => {
  return request<DocSignatureVerifyResultVO>({
    url: '/doc/signature/verify',
    method: 'post',
    data
  })
}

export const verifySignatureById = (signatureId: number) => {
  return request<DocSignatureVerifyResultVO>({
    url: `/doc/signature/verify/${signatureId}`,
    method: 'get'
  })
}

export const verifyDocumentSignatures = (documentId: number) => {
  return request<DocSignatureVerifyResultVO[]>({
    url: `/doc/signature/verify/document/${documentId}`,
    method: 'get'
  })
}

export const getSignaturePage = (params: { documentId?: number; pageNum?: number; pageSize?: number }) => {
  return request<PageResult<DocSignatureVO>>({
    url: '/doc/signature/page',
    method: 'get',
    params
  })
}

export const getSignatureDetail = (id: number) => {
  return request<DocSignatureVO>({
    url: `/doc/signature/${id}`,
    method: 'get'
  })
}

export const getDocumentSignatures = (documentId: number) => {
  return request<DocSignatureVO[]>({
    url: `/doc/signature/document/${documentId}`,
    method: 'get'
  })
}

export const revokeSignature = (signatureId: number, reason?: string) => {
  return request<boolean>({
    url: `/doc/signature/revoke/${signatureId}`,
    method: 'post',
    params: { reason }
  })
}

export const downloadSignedDocument = (signatureId: number) => {
  return request<Blob>({
    url: `/doc/signature/download/${signatureId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export const getPreviewUrl = (signatureId: number) => {
  return `/api/doc/signature/preview/${signatureId}`
}

// ==================== 签章日志接口 ====================

export const getSignatureLogPage = (params: DocSignatureLogQueryDTO) => {
  return request<PageResult<DocSignatureLogVO>>({
    url: '/doc/signature/log/page',
    method: 'get',
    params
  })
}

export const getSignatureLogDetail = (id: number) => {
  return request<DocSignatureLogVO>({
    url: `/doc/signature/log/${id}`,
    method: 'get'
  })
}

// ==================== 辅助函数 ====================

export const getSealTypeLabel = (type: string) => {
  const options = [
    { label: '单位章', value: 'UNIT' },
    { label: '部门章', value: 'DEPT' },
    { label: '签名章', value: 'SIGNATURE' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.label : '未知'
}

export const getSealTypeColor = (type: string) => {
  const options = [
    { label: '单位章', value: 'UNIT', color: 'blue' },
    { label: '部门章', value: 'DEPT', color: 'green' },
    { label: '签名章', value: 'SIGNATURE', color: 'purple' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.color : 'default'
}

export const getSealStatusLabel = (status: number) => {
  const options = [
    { label: '禁用', value: 0 },
    { label: '启用', value: 1 },
    { label: '过期', value: 2 }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

export const getSealStatusColor = (status: number) => {
  const options = [
    { label: '禁用', value: 0, color: 'default' },
    { label: '启用', value: 1, color: 'success' },
    { label: '过期', value: 2, color: 'error' }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

export const getSignatureTypeLabel = (type: string) => {
  const options = [
    { label: '落款章', value: 'SIGNATURE' },
    { label: '骑缝章', value: 'RIDING' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.label : '未知'
}

export const getSignatureTypeColor = (type: string) => {
  const options = [
    { label: '落款章', value: 'SIGNATURE', color: 'blue' },
    { label: '骑缝章', value: 'RIDING', color: 'orange' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.color : 'default'
}

export const getVerifyStatusLabel = (status: number) => {
  const options = [
    { label: '未验证', value: 0 },
    { label: '验证通过', value: 1 },
    { label: '验证失败', value: 2 },
    { label: '文档已篡改', value: 3 }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

export const getVerifyStatusColor = (status: number) => {
  const options = [
    { label: '未验证', value: 0, color: 'default' },
    { label: '验证通过', value: 1, color: 'success' },
    { label: '验证失败', value: 2, color: 'error' },
    { label: '文档已篡改', value: 3, color: 'warning' }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

export const getGrantTypeLabel = (type: string) => {
  const options = [
    { label: '用户', value: 'USER' },
    { label: '部门', value: 'DEPT' },
    { label: '角色', value: 'ROLE' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.label : '未知'
}

export const getGrantStatusLabel = (status: number) => {
  const options = [
    { label: '已撤销', value: 0 },
    { label: '有效', value: 1 },
    { label: '过期', value: 2 }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

export const getGrantStatusColor = (status: number) => {
  const options = [
    { label: '已撤销', value: 0, color: 'default' },
    { label: '有效', value: 1, color: 'success' },
    { label: '过期', value: 2, color: 'error' }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

export const getOperationTypeLabel = (type: string) => {
  const options = [
    { label: '上传', value: 'UPLOAD' },
    { label: '分配', value: 'ASSIGN' },
    { label: '撤销', value: 'REVOKE' },
    { label: '签章', value: 'SIGN' },
    { label: '验章', value: 'VERIFY' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.label : '未知'
}

export const getOperationTypeColor = (type: string) => {
  const options = [
    { label: '上传', value: 'UPLOAD', color: 'blue' },
    { label: '分配', value: 'ASSIGN', color: 'green' },
    { label: '撤销', value: 'REVOKE', color: 'orange' },
    { label: '签章', value: 'SIGN', color: 'purple' },
    { label: '验章', value: 'VERIFY', color: 'cyan' }
  ]
  const item = options.find((opt) => opt.value === type)
  return item ? item.color : 'default'
}

export const getLogStatusLabel = (status: number) => {
  const options = [
    { label: '失败', value: 0 },
    { label: '成功', value: 1 }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

export const getLogStatusColor = (status: number) => {
  const options = [
    { label: '失败', value: 0, color: 'error' },
    { label: '成功', value: 1, color: 'success' }
  ]
  const item = options.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

export const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
