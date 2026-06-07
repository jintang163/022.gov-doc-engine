export interface DocSealVO {
  id: number
  sealName: string
  sealType: string
  sealTypeName: string
  sealCode: string
  ownerUnitId: string
  ownerUnitName: string
  ownerDeptId: string
  ownerDeptName: string
  ownerUserId: string
  ownerUserName: string
  sealImagePath: string
  sealImageName: string
  sealWidth: number
  sealHeight: number
  certificateSerial: string
  certificateSubject: string
  certificateIssuer: string
  certificateValidFrom: string
  certificateValidTo: string
  privateKeyPath: string
  algorithm: string
  status: number
  statusName: string
  password: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocSealCreateDTO {
  sealName: string
  sealType: string
  sealCode?: string
  ownerUnitId?: string
  ownerUnitName?: string
  ownerDeptId?: string
  ownerDeptName?: string
  ownerUserId?: string
  ownerUserName?: string
  sealImagePath?: string
  sealImageName?: string
  sealWidth?: number
  sealHeight?: number
  certificateSerial?: string
  certificateSubject?: string
  certificateIssuer?: string
  certificateValidFrom?: string
  certificateValidTo?: string
  privateKeyPath?: string
  algorithm?: string
  status?: number
  password?: string
  remark?: string
}

export interface DocSealUpdateDTO {
  id: number
  sealName?: string
  sealType?: string
  sealCode?: string
  ownerUnitId?: eumber
  ownerUnitName?: string
  ownerDeptId?: eumber
  ownerDeptName?: string
  ownerUserId?: eumber
  ownerUserName?: string
  sealImagePath?: string
  sealImageName?: string
  sealWidth?: number
  sealHeight?: number
  certificateSerial?: string
  certificateSubject?: string
  certificateIssuer?: string
  certificateValidFrom?: string
  certificateValidTo?: string
  privateKeyPath?: string
  algorithm?: string
  status?: number
  password?: string
  remark?: string
}

export interface DocSealGrantDTO {
  id?: number
  sealId: number
  grantType: string
  grantTargetId: string
  grantTargetName?: string
  grantStartTime?: string
  grantEndTime?: string
  signLimit?: number
  status?: number
  remark?: string
}

export interface DocSealGrantVO {
  id: number
  sealId: number
  sealName: string
  grantType: string
  grantTypeName: string
  grantTargetId: string
  grantTargetName: string
  grantStartTime: string
  grantEndTime: string
  signLimit: number
  signCount: number
  status: number
  statusName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocSealQueryDTO {
  sealName?: string
  sealType?: string
  sealCode?: string
  ownerUnitId?: string
  ownerDeptId?: string
  ownerUserId?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export interface DocSignatureVO {
  id: number
  documentId: number
  documentTitle: string
  documentVersion: number
  sealId: number
  sealName: string
  sealType: string
  signatureType: string
  signatureTypeName: string
  pageNumber: number
  totalPages: number
  positionX: number
  positionY: number
  sealWidth: number
  sealHeight: number
  signatureReason: string
  signatureLocation: string
  signedFilePath: string
  signedFileName: string
  fileHash: string
  signatureValue: string
  certificateSerial: string
  signTime: string
  signerId: string
  signerName: string
  signerDeptId: string
  signerDeptName: string
  algorithm: string
  verifyStatus: number
  verifyStatusName: string
  verifyTime: string
  verifyCount: number
  isValid: number
  isValidName: string
  revokeReason: string
  revokeTime: string
  revokeBy: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocSignatureSignDTO {
  documentId: number
  sealId: number
  signatureType: string
  pageNumber?: number
  positionX: number
  positionY: number
  sealWidth?: number
  sealHeight?: number
  signatureReason?: string
  signatureLocation?: string
  password?: string
  remark?: string
}

export interface DocSignatureVerifyDTO {
  signatureId: number
  remark?: string
}

export interface DocSignatureVerifyResultVO {
  signatureId: number
  valid: boolean
  verifyStatus: number
  verifyStatusName: string
  message: string
  verifyTime: string
  signerName: string
  signerDeptName: string
  signTime: string
  certificateSerial: string
  algorithm: string
  fileHash: string
}

export interface DocSignatureLogVO {
  id: number
  operationType: string
  operationTypeName: string
  sealId: number
  sealName: string
  documentId: number
  documentTitle: string
  signatureId: number
  grantId: number
  operationDetail: string
  ipAddress: string
  userAgent: string
  status: number
  statusName: string
  errorMessage: string
  operateTime: string
  operatorId: string
  operatorName: string
  operatorDeptId: string
  operatorDeptName: string
  remark: string
  createTime: string
}

export interface DocSignatureLogQueryDTO {
  operationType?: string
  sealId?: number
  documentId?: number
  signatureId?: number
  operatorId?: string
  status?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export const sealTypeOptions = [
  { label: '单位章', value: 'UNIT', color: 'blue' },
  { label: '部门章', value: 'DEPT', color: 'green' },
  { label: '签名章', value: 'SIGNATURE', color: 'purple' }
]

export const signatureTypeOptions = [
  { label: '落款章', value: 'SIGNATURE', color: 'blue' },
  { label: '骑缝章', value: 'RIDING', color: 'orange' }
]

export const sealStatusOptions = [
  { label: '禁用', value: 0, color: 'default' },
  { label: '启用', value: 1, color: 'success' },
  { label: '过期', value: 2, color: 'error' }
]

export const grantTypeOptions = [
  { label: '用户', value: 'USER', color: 'blue' },
  { label: '部门', value: 'DEPT', color: 'green' },
  { label: '角色', value: 'ROLE', color: 'purple' }
]

export const grantStatusOptions = [
  { label: '已撤销', value: 0, color: 'default' },
  { label: '有效', value: 1, color: 'success' },
  { label: '过期', value: 2, color: 'error' }
]

export const verifyStatusOptions = [
  { label: '未验证', value: 0, color: 'default' },
  { label: '验证通过', value: 1, color: 'success' },
  { label: '验证失败', value: 2, color: 'error' },
  { label: '文档已篡改', value: 3, color: 'warning' }
]

export const operationTypeOptions = [
  { label: '上传', value: 'UPLOAD', color: 'blue' },
  { label: '分配', value: 'ASSIGN', color: 'green' },
  { label: '撤销', value: 'REVOKE', color: 'orange' },
  { label: '签章', value: 'SIGN', color: 'purple' },
  { label: '验章', value: 'VERIFY', color: 'cyan' }
]

export const logStatusOptions = [
  { label: '失败', value: 0, color: 'error' },
  { label: '成功', value: 1, color: 'success' }
]
