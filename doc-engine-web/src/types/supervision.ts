export interface DocSupervisionVO {
  id: number
  supervisionNo: string
  incomingId: number
  handlingId?: number
  sourceDocNumber: string
  docTitle: string
  supervisionType: string
  supervisionTypeName: string
  responsibleUserId: string
  responsibleUserName: string
  responsibleDeptId: string
  responsibleDeptName: string
  leaderId?: string
  leaderName?: string
  overdueDays: number
  urgeCount: number
  deadline?: string
  supervisionContent?: string
  pushStatus: string
  pushStatusName: string
  pushTime?: string
  status: string
  statusName: string
  completeTime?: string
  remark?: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  incoming?: any
  urgeLogs?: DocUrgeLogVO[]
}

export interface DocSupervisionDTO {
  incomingId: number
  handlingId?: number
  supervisionType: string
  responsibleUserId?: string
  responsibleUserName?: string
  responsibleDeptId?: string
  responsibleDeptName?: string
  leaderId?: string
  leaderName?: string
  supervisionContent?: string
  remark?: string
}

export interface DocSupervisionQueryDTO {
  keyword?: string
  incomingId?: number
  supervisionType?: string
  responsibleUserId?: string
  responsibleDeptId?: string
  leaderId?: string
  pushStatus?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface DocUrgeLogVO {
  id: number
  incomingId: number
  handlingId?: number
  urgeNo: string
  urgeType: string
  urgeTypeName?: string
  urgedUserId: string
  urgedUserName: string
  urgedDeptId: string
  urgedDeptName: string
  urgeContent: string
  status: string
  statusName: string
  acknowledgeTime?: string
  remark?: string
  createBy: string
  createTime: string
}

export interface DocUrgeDTO {
  incomingId: number
  handlingId?: number
  urgeType?: string
  urgeContent?: string
  remark?: string
}

export const supervisionTypeOptions = [
  { label: '超时督办', value: 'timeout', color: 'red' },
  { label: '催办超标督办', value: 'urge_overdue', color: 'orange' }
]

export const supervisionStatusOptions = [
  { label: '已生成', value: 'generated', color: 'default' },
  { label: '已通知', value: 'notified', color: 'processing' },
  { label: '处理中', value: 'processing', color: 'warning' },
  { label: '已完成', value: 'completed', color: 'success' },
  { label: '已关闭', value: 'closed', color: 'default' }
]

export const pushStatusOptions = [
  { label: '待推送', value: 'pending', color: 'default' },
  { label: '已推送', value: 'pushed', color: 'success' },
  { label: '推送失败', value: 'failed', color: 'error' }
]

export const urgeStatusOptions = [
  { label: '已发送', value: 'sent', color: 'processing' },
  { label: '已确认', value: 'acknowledged', color: 'success' }
]

export const urgeTypeOptions = [
  { label: '系统催办', value: 'system' },
  { label: '人工催办', value: 'manual' }
]
