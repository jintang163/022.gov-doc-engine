export interface StatQueryDTO {
  startDate?: string
  endDate?: string
  unitCode?: string
  docType?: string
  processType?: string
}

export interface StatOverviewVO {
  totalDocCount: number
  draftCount: number
  reviewingCount: number
  signedCount: number
  archivedCount: number
  abolishedCount: number
  completionRate: number
  avgDurationMinutes: number
  avgDurationText: string
  todayCount: number
  weekCount: number
  monthCount: number
}

export interface StatDocTypeVO {
  docType: string
  docTypeName: string
  count: number
  percentage: number
}

export interface StatDocStatusVO {
  status: string
  statusName: string
  count: number
  percentage: number
}

export interface StatProcessVO {
  processName: string
  totalCount: number
  completedCount: number
  runningCount: number
  terminatedCount: number
  completionRate: number
  avgDurationMinutes: number
  avgDurationText: string
}

export interface StatTrendVO {
  date: string
  docCount: number
  completedCount: number
  avgDurationMinutes: number
}

export interface StatUnitVO {
  unitCode: string
  unitName: string
  docCount: number
  completedCount: number
  completionRate: number
  avgDurationMinutes: number
}

export interface StatDeptDraftVO {
  deptId: string
  deptName: string
  docCount: number
  avgDraftMinutes: number
  avgDraftText: string
  minDraftMinutes: number
  minDraftText: string
  maxDraftMinutes: number
  maxDraftText: string
}

export interface StatNodeDwellVO {
  nodeId: string
  nodeName: string
  nodeType: string
  postId: string
  postName: string
  taskCount: number
  avgDwellMinutes: number
  avgDwellText: string
  minDwellMinutes: number
  minDwellText: string
  maxDwellMinutes: number
  maxDwellText: string
  withinRate: number
}

export interface StatCountersignCycleVO {
  docType: string
  docTypeName: string
  countersignCount: number
  avgCycleMinutes: number
  avgCycleText: string
  minCycleMinutes: number
  minCycleText: string
  maxCycleMinutes: number
  maxCycleText: string
  withinRate: number
}

export interface StatTimelinessTrendVO {
  date: string
  avgDraftMinutes: number
  avgDwellMinutes: number
  avgCountersignMinutes: number
  completionRate: number
}

export interface StatRejectionOverviewVO {
  totalRejectionCount: number
  totalReturnCount: number
  totalCount: number
  returnRate: number
}

export interface StatRejectionWordVO {
  word: string
  count: number
  weight: number
}

export interface StatRejectionReasonVO {
  reason: string
  count: number
  percentage: number
  deptId: string
  deptName: string
}

export interface EfficiencyRankQueryDTO {
  statMonth?: string
  unitCode?: string
  deptId?: string
  pageNum?: number
  pageSize?: number
}

export interface StatEfficiencyVO {
  id: number
  statMonth: string
  rankType: string
  targetId: string
  targetName: string
  deptId: string
  deptName: string
  unitCode: string
  unitName: string
  totalTask: number
  completedTask: number
  overdueTask: number
  completionRate: number
  avgDurationMinutes: number
  avgDurationText: string
  efficiencyScore: number
  rankNo: number
  rankLevel: string
}

export interface PageVO<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const rankLevelOptions = [
  { label: '卓越', value: '卓越', color: '#f5222d' },
  { label: '优秀', value: '优秀', color: '#fa8c16' },
  { label: '良好', value: '良好', color: '#1890ff' },
  { label: '达标', value: '达标', color: '#52c41a' }
]

export const rankTypeOptions = [
  { label: '部门排名', value: 'dept' },
  { label: '个人排名', value: '个人排名' }
]
