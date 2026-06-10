import request from '@/utils/request'
import type { StatOverviewVO, StatDocTypeVO, StatDocStatusVO, StatProcessVO, StatTrendVO, StatUnitVO, StatQueryDTO } from '@/types/stat'
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
