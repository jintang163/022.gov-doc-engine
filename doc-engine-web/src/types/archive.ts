export interface DocArchiveVO {
  id: number
  docId: number
  archiveNo: string
  archiveYear: number
  archiveType: string
  archiveDeptId: string
  archiveDeptName: string
  securityLevel: string
  archiveMethod: string
  archiveMethodName: string
  archiveLocation: string
  retentionPeriod: string
  retentionPeriodName: string
  archiveDate: string
  isLocked: number
  status: string
  statusName: string
  docTitle: string
  docNumber: string
  docType: string
  docContentSnapshot: string
  unitCode: string
  unitName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocArchiveDTO {
  docId: number
  archiveType?: string
  archiveLocation?: string
  retentionPeriod?: string
  remark?: string
}

export interface DocArchiveQueryDTO {
  keyword?: string
  archiveYear?: number
  archiveType?: string
  archiveDeptId?: string
  securityLevel?: string
  status?: string
  docNumber?: string
  docType?: string
  unitCode?: string
  pageNum?: number
  pageSize?: number
}

export interface DocBorrowVO {
  id: number
  archiveId: number
  docId: number
  borrowNo: string
  applicantId: string
  applicantName: string
  applicantDeptId: string
  applicantDeptName: string
  borrowReason: string
  borrowType: string
  borrowTypeName: string
  startDate: string
  endDate: string
  status: string
  statusName: string
  approverId: string
  approverName: string
  approveTime: string
  approveOpinion: string
  returnTime: string
  watermarkText: string
  viewCount: number
  lastViewTime: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  archive?: DocArchiveVO
}

export interface DocBorrowApplyDTO {
  archiveId: number
  borrowReason: string
  borrowType?: string
  startDate: string
  endDate: string
  remark?: string
}

export interface DocBorrowApproveDTO {
  borrowId: number
  approveResult: string
  approveOpinion?: string
}

export interface DocBorrowQueryDTO {
  keyword?: string
  status?: string
  applicantId?: string
  archiveId?: number
  docId?: number
  borrowType?: string
  pageNum?: number
  pageSize?: number
}

export const archiveStatusOptions = [
  { label: '已归档', value: 'archived', color: 'success' },
  { label: '已移交', value: 'transferred', color: 'processing' },
  { label: '已销毁', value: 'destroyed', color: 'error' }
]

export const archiveMethodOptions = [
  { label: '手动归档', value: 'manual' },
  { label: '自动归档', value: 'auto' }
]

export const retentionPeriodOptions = [
  { label: '永久', value: 'permanent' },
  { label: '长期(30年)', value: 'long' },
  { label: '短期(10年)', value: 'short' }
]

export const borrowStatusOptions = [
  { label: '待审批', value: 'pending', color: 'warning' },
  { label: '已批准', value: 'approved', color: 'success' },
  { label: '已驳回', value: 'rejected', color: 'error' },
  { label: '借阅中', value: 'active', color: 'processing' },
  { label: '已归还', value: 'returned', color: 'default' },
  { label: '已逾期', value: 'overdue', color: 'red' }
]

export const borrowTypeOptions = [
  { label: '在线借阅', value: 'online' },
  { label: '实体借阅', value: 'physical' }
]

export const archiveTypeOptions = [
  { label: '上行文', value: '上行文' },
  { label: '下行文', value: '下行文' },
  { label: '平行文', value: '平行文' },
  { label: '内部文', value: '内部文' },
  { label: '请示', value: '请示' },
  { label: '报告', value: '报告' },
  { label: '通知', value: '通知' },
  { label: '批复', value: '批复' },
  { label: '函', value: '函' },
  { label: '纪要', value: '纪要' }
]

export const archiveSecurityLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '秘密', value: '秘密' },
  { label: '机密', value: '机密' },
  { label: '绝密', value: '绝密' }
]
