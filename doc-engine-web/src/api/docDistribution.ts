import request from '@/utils/request'
import type {
  DocDistributionVO,
  DocDistributionCreateDTO,
  DocStatusLogVO,
  PageResult
} from '@/types/workflow'

export const getDistributionPage = (params: {
  pageNum: number
  pageSize: number
  docId?: number
  status?: string
}) => {
  return request.get<PageResult<DocDistributionVO>>('/api/doc/distribution/page', { params })
}

export const getDistributionDetail = (id: number) => {
  return request.get<DocDistributionVO>(`/api/doc/distribution/${id}`)
}

export const getDistributionByDocId = (docId: number) => {
  return request.get<DocDistributionVO[]>(`/api/doc/distribution/doc/${docId}`)
}

export const distributeDocument = (data: DocDistributionCreateDTO) => {
  return request.post<DocDistributionVO>('/api/doc/distribution/distribute', data)
}

export const confirmReceive = (id: number, remark?: string) => {
  return request.post<void>(`/api/doc/distribution/confirm-receive/${id}`, null, {
    params: { remark }
  })
}

export const markPrinted = (id: number) => {
  return request.post<void>(`/api/doc/distribution/mark-printed/${id}`)
}

export const getStatusLogByDocId = (docId: number) => {
  return request.get<DocStatusLogVO[]>(`/api/doc/status-log/doc/${docId}`)
}

export const getLatestStatusLog = (docId: number) => {
  return request.get<DocStatusLogVO>(`/api/doc/status-log/latest/${docId}`)
}

export const getAllDocStatus = () => {
  return request.get<Array<{ code: string; name: string }>>('/api/doc/status/all')
}

export const getCurrentDocStatus = (docId: number) => {
  return request.get<string>(`/api/doc/status/current/${docId}`)
}

export const getAllowedTransitions = (docId: number) => {
  return request.get<Set<string>>(`/api/doc/status/allowed-transitions/${docId}`)
}

export const canTransition = (params: { docId: number; toStatus: string }) => {
  return request.get<boolean>('/api/doc/status/can-transition', { params })
}

export const transitionStatus = (params: {
  docId: number
  toStatus: string
  transitionReason?: string
  remark?: string
}) => {
  return request.post<void>('/api/doc/status/transition', null, { params })
}

export const abolishDocument = (docId: number, reason?: string, remark?: string) => {
  return request.post<void>(`/api/doc/status/abolish/${docId}`, null, {
    params: { reason, remark }
  })
}

export const archiveDocument = (docId: number, remark?: string) => {
  return request.post<void>(`/api/doc/status/archive/${docId}`, null, {
    params: { remark }
  })
}
