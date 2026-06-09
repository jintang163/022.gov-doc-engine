export interface DocPermissionVO {
  id: number
  permissionCode: string
  permissionName: string
  permissionType: string
  permissionTypeName: string
  securityLevel: string
  allowedRoles: string
  allowedUsers: string
  allowedDepts: string
  description: string
  enabled: number
  sortOrder: number
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocPermissionDTO {
  permissionCode: string
  permissionName: string
  permissionType: string
  securityLevel?: string
  allowedRoles?: string
  allowedUsers?: string
  allowedDepts?: string
  description?: string
  enabled?: number
  sortOrder?: number
}

export interface DocAuditLogVO {
  id: number
  logNo: string
  userId: string
  userName: string
  deptId: string
  deptName: string
  action: string
  actionName: string
  resourceType: string
  resourceTypeName: string
  resourceId: string
  resourceName: string
  securityLevel: string
  description: string
  oldValue: string
  newValue: string
  ipAddress: string
  userAgent: string
  requestUrl: string
  requestMethod: string
  costTime: number
  result: string
  resultName: string
  errorMessage: string
  createTime: string
}

export interface DocAuditLogQueryDTO {
  userId?: string
  action?: string
  resourceType?: string
  resourceId?: string
  securityLevel?: string
  result?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export interface DocIntegrityVO {
  id: number
  docId: number
  contentHash: string
  signatureHash: string
  hashAlgorithm: string
  version: number
  verifyStatus: string
  verifyStatusName: string
  verifyTime: string
  verifiedBy: string
  verifiedByName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocIntegrityDTO {
  docId: number
  contentHash?: string
  signatureHash?: string
}

export interface DocIntegrityVerifyDTO {
  docId: number
  currentContent?: string
}

export const permissionTypeOptions = [
  { label: '操作权限', value: 'action' },
  { label: '数据权限', value: 'data' }
]

export const auditActionOptions = [
  { label: '拟稿', value: 'draft' },
  { label: '修改', value: 'modify' },
  { label: '删除', value: 'delete' },
  { label: '签章', value: 'sign' },
  { label: '审批', value: 'approve' },
  { label: '查看', value: 'view' },
  { label: '归档', value: 'archive' },
  { label: '导出', value: 'export' },
  { label: '打印', value: 'print' }
]

export const resourceTypeOptions = [
  { label: '公文', value: 'document' },
  { label: '模板', value: 'template' },
  { label: '印章', value: 'seal' },
  { label: '签章', value: 'signature' },
  { label: '流程', value: 'workflow' },
  { label: '档案', value: 'archive' },
  { label: '借阅', value: 'borrow' },
  { label: '收文', value: 'incoming' },
  { label: '权限', value: 'permission' },
  { label: '完整性', value: 'integrity' }
]

export const securityLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '秘密', value: '秘密' },
  { label: '机密', value: '机密' },
  { label: '绝密', value: '绝密' }
]

export const integrityStatusOptions = [
  { label: '待验证', value: 'pending', color: 'warning' },
  { label: '已验证', value: 'verified', color: 'success' },
  { label: '已篡改', value: 'tampered', color: 'error' }
]

export const auditResultOptions = [
  { label: '成功', value: 'success', color: 'success' },
  { label: '失败', value: 'failure', color: 'error' }
]
