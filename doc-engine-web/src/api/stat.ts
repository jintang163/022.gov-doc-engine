import request from '@/utils/request'
import type { StatOverviewVO, StatDocTypeVO, StatDocStatusVO, StatProcessVO, StatTrendVO, StatUnitVO, StatQueryDTO, StatDeptDraftVO, StatNodeDwellVO, StatCountersignCycleVO, StatTimelinessTrendVO } from '@/types/stat'
import type { Result } from '@/types/template'

export const getStatOverview = (params?: StatQueryDTO) => {
  return request<Result<StatOverviewVO>>({
    url: '/stat/overview',
    method: 'get',
    params
  })
}

export const getStatDocType = (params?: StatQueryDTO) => {
  return request<Result<StatDocTypeVO[]>>({
    url: '/stat/doc-type',
    method: 'get',
    params
  })
}

export const getStatDocStatus = (params?: StatQueryDTO) => {
  return request<Result<StatDocStatusVO[]>>({
    url: '/stat/doc-status',
    method: 'get',
    params
  })
}

export const getStatProcess = (params?: StatQueryDTO) => {
  return request<Result<StatProcessVO[]>>({
    url: '/stat/process',
    method: 'get',
    params
  })
}

export const getStatTrend = (params?: StatQueryDTO) => {
  return request<Result<StatTrendVO[]>>({
    url: '/stat/trend',
    method: 'get',
    params
  })
}

export const getStatUnit = (params?: StatQueryDTO) => {
  return request<Result<StatUnitVO[]>>({
    url: '/stat/unit',
    method: 'get',
    params
  })
}

export const getStatDeptDraft = (params?: StatQueryDTO) => {
  return request<Result<StatDeptDraftVO[]>>({
    url: '/stat/dept-draft',
    method: 'get',
    params
  })
}

export const getStatNodeDwell = (params?: StatQueryDTO) => {
  return request<Result<StatNodeDwellVO[]>>({
    url: '/stat/node-dwell',
    method: 'get',
    params
  })
}

export const getStatCountersignCycle = (params?: StatQueryDTO) => {
  return request<Result<StatCountersignCycleVO[]>>({
    url: '/stat/countersign-cycle',
    method: 'get',
    params
  })
}

export const getStatTimelinessTrend = (params?: StatQueryDTO) => {
  return request<Result<StatTimelinessTrendVO[]>>({
    url: '/stat/timeliness-trend',
    method: 'get',
    params
  })
}
