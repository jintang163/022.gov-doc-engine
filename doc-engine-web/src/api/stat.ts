import request from '@/utils/request'
import type { StatOverviewVO, StatDocTypeVO, StatDocStatusVO, StatProcessVO, StatTrendVO, StatUnitVO, StatQueryDTO, StatDeptDraftVO, StatNodeDwellVO, StatCountersignCycleVO, StatTimelinessTrendVO, StatRejectionOverviewVO, StatRejectionWordVO, StatRejectionReasonVO, StatEfficiencyVO, EfficiencyRankQueryDTO, PageVO, SupervisionSuggestionVO, SupervisionSuggestionQueryDTO } from '@/types/stat'
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

export const getStatRejectionOverview = (params?: StatQueryDTO) => {
  return request<Result<StatRejectionOverviewVO>>({
    url: '/stat/rejection-overview',
    method: 'get',
    params
  })
}

export const getStatRejectionWords = (params?: StatQueryDTO) => {
  return request<Result<StatRejectionWordVO[]>>({
    url: '/stat/rejection-words',
    method: 'get',
    params
  })
}

export const getStatRejectionReasons = (params?: StatQueryDTO) => {
  return request<Result<StatRejectionReasonVO[]>>({
    url: '/stat/rejection-reasons',
    method: 'get',
    params
  })
}

export const getDeptEfficiencyRank = (params?: EfficiencyRankQueryDTO) => {
  return request<Result<PageVO<StatEfficiencyVO>>>({
    url: '/stat/efficiency/dept-rank',
    method: 'get',
    params
  })
}

export const getPersonEfficiencyRank = (params?: EfficiencyRankQueryDTO) => {
  return request<Result<PageVO<StatEfficiencyVO>>>({
    url: '/stat/efficiency/person-rank',
    method: 'get',
    params
  })
}

export const exportEfficiencyRank = (params: EfficiencyRankQueryDTO & { rankType?: 'dept' | 'person' }) => {
  return request<Blob>({
    url: '/stat/efficiency/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export const calculateEfficiency = (data: EfficiencyRankQueryDTO) => {
  return request<Result<boolean>>({
    url: '/stat/efficiency/calculate',
    method: 'post',
    data
  })
}

export const getSupervisionSuggestionPage = (params?: SupervisionSuggestionQueryDTO) => {
  return request<Result<PageVO<SupervisionSuggestionVO>>>({
    url: '/stat/supervision-suggestion/page',
    method: 'get',
    params
  })
}

export const refreshSupervisionSuggestions = () => {
  return request<Result<number>>({
    url: '/stat/supervision-suggestion/refresh',
    method: 'post'
  })
}

export const handleSupervisionSuggestion = (id: number, action: 'supervise' | 'ignore', urgeContent?: string) => {
  return request<Result<SupervisionSuggestionVO>>({
    url: `/stat/supervision-suggestion/${id}/handle`,
    method: 'post',
    params: { action, urgeContent }
  })
}

export const batchHandleSupervisionSuggestions = (ids: number[], action: 'supervise' | 'ignore') => {
  return request<Result<number>>({
    url: '/stat/supervision-suggestion/batch-handle',
    method: 'post',
    data: { ids, action }
  })
}
