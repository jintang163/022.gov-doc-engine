export interface DocIncomingVO {
  id: number
  incomingNo: string
  source: string
  sourceName: string
  sourceUnit: string
  sourceDocNumber: string
  docTitle: string
  docType: string
  securityLevel: string
  urgencyLevel: string
  receivedDate: string
  receivedMethod: string
  receivedMethodName: string
  copies: number
  pages: number
  docContent: string
  attachmentInfo: string
  keyword: string
  abstractText: string
  status: string
  statusName: string
  registrantId: string
  registrantName: string
  registrantDeptId: string
  registrantDeptName: string
  unitCode: string
  unitName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface DocIncomingDTO {
  docTitle: string
  source?: string
  sourceUnit?: string
  sourceDocNumber?: string
  docType?: string
  securityLevel?: string
  urgencyLevel?: string
  receivedDate: string
  receivedMethod?: string
  copies?: number
  pages?: number
  docContent?: string
  attachmentInfo?: string
  keyword?: string
  abstractText?: string
  remark?: string
}

export interface DocIncomingQueryDTO {
  keyword?: string
  source?: string
  sourceUnit?: string
  docType?: string
  securityLevel?: string
  urgencyLevel?: string
  status?: string
  receivedDateStart?: string
  receivedDateEnd?: string
  pageNum?: number
  pageSize?: number
}

export interface DocHandlingVO {
  id: number
  incomingId: number
  handlingNo: string
  handlingType: string
  handlingTypeName: string
  opinion: string
  handlerId: string
  handlerName: string
  handlerDeptId: string
  handlerDeptName: string
  targetDeptId: string
  targetDeptName: string
  targetUserId: string
  targetUserName: string
  deadline: string
  handlingTime: string
  feedbackContent: string
  feedbackTime: string
  feedbackAttachment: string
  status: string
  statusName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  incoming?: DocIncomingVO
}

export interface DocHandlingDTO {
  incomingId: number
  handlingType: string
  opinion?: string
  targetDeptId?: string
  targetDeptName?: string
  targetUserId?: string
  targetUserName?: string
  deadline?: string
  remark?: string
}

export interface DocHandlingFeedbackDTO {
  handlingId: number
  feedbackContent?: string
  feedbackAttachment?: string
}

export interface DocHandlingQueryDTO {
  keyword?: string
  incomingId?: number
  handlingType?: string
  handlerId?: string
  handlerDeptId?: string
  targetDeptId?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export const incomingStatusOptions = [
  { label: '已登记', value: 'registered', color: 'default' },
  { label: '已拟办', value: 'proposed', color: 'processing' },
  { label: '承办中', value: 'handling', color: 'warning' },
  { label: '已办结', value: 'completed', color: 'success' },
  { label: '已转发', value: 'transferred', color: 'blue' }
]

export const handlingStatusOptions = [
  { label: '待处理', value: 'pending', color: 'warning' },
  { label: '处理中', value: 'processing', color: 'processing' },
  { label: '已完成', value: 'completed', color: 'success' }
]

export const handlingTypeOptions = [
  { label: '拟办意见', value: 'draft_opinion' },
  { label: '转承办', value: 'assign' },
  { label: '反馈', value: 'feedback' }
]

export const incomingSourceOptions = [
  { label: '手动录入', value: 'manual' },
  { label: '接口接入', value: 'api' }
]

export const receivedMethodOptions = [
  { label: '邮件', value: 'mail' },
  { label: '专递', value: 'courier' },
  { label: '传真', value: 'fax' },
  { label: '电子', value: 'email' },
  { label: '当面', value: 'in_person' }
]

export const incomingDocTypeOptions = [
  { label: '通知', value: '通知' },
  { label: '函', value: '函' },
  { label: '批复', value: '批复' },
  { label: '批示', value: '批示' },
  { label: '命令', value: '命令' },
  { label: '决定', value: '决定' },
  { label: '公告', value: '公告' },
  { label: '报告', value: '报告' },
  { label: '请示', value: '请示' },
  { label: '其他', value: '其他' }
]

export const incomingSecurityLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '秘密', value: '秘密' },
  { label: '机密', value: '机密' },
  { label: '绝密', value: '绝密' }
]

export const incomingUrgencyLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '加急', value: '加急' },
  { label: '特急', value: '特急' },
  { label: '限时', value: '限时' }
]

export interface DeptRecommendVO {
  deptId: string
  deptName: string
  matchScore: number
  matchScoreText: string
  matchCount: number
  lastHandleTime: string
  matchReason: string
}
