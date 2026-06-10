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
