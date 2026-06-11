import request from '@/utils/request'
import type { PageResult } from '@/types/template'
import type {
  DocSupervisionVO,
  DocSupervisionDTO,
  DocSupervisionQueryDTO,
  DocUrgeLogVO,
  DocUrgeDTO
} from '@/types/supervision'

export const getSupervisionPage = (params: DocSupervisionQueryDTO) => {
  return request<PageResult<DocSupervisionVO>>({
    url: '/doc/supervision/page',
    method: 'get',
    params
  })
}

export const getSupervisionDetail = (id: number) => {
  return request<DocSupervisionVO>({
    url: `/doc/supervision/${id}`,
    method: 'get'
  })
}

export const identifyTimeoutDocs = () => {
  return request<DocSupervisionVO[]>({
    url: '/doc/supervision/identify-timeout',
    method: 'get'
  })
}

export const identifyUrgeOverdueDocs = () => {
  return request<DocSupervisionVO[]>({
    url: '/doc/supervision/identify-urge-overdue',
    method: 'get'
  })
}

export const generateSupervision = (data: DocSupervisionDTO) => {
  return request<DocSupervisionVO>({
    url: '/doc/supervision/generate',
    method: 'post',
    data
  })
}

export const batchGenerateSupervision = (data: DocSupervisionDTO[]) => {
  return request<DocSupervisionVO[]>({
    url: '/doc/supervision/batch-generate',
    method: 'post',
    data
  })
}

export const pushToLeader = (id: number) => {
  return request<DocSupervisionVO>({
    url: `/doc/supervision/${id}/push`,
    method: 'post'
  })
}

export const batchPushToLeader = (ids: number[]) => {
  return request<DocSupervisionVO[]>({
    url: '/doc/supervision/batch-push',
    method: 'post',
    data: ids
  })
}

export const updateSupervisionStatus = (id: number, status: string) => {
  return request<DocSupervisionVO>({
    url: `/doc/supervision/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const completeSupervision = (id: number) => {
  return request<DocSupervisionVO>({
    url: `/doc/supervision/${id}/complete`,
    method: 'put'
  })
}

export const getMySupervisions = (params: DocSupervisionQueryDTO) => {
  return request<PageResult<DocSupervisionVO>>({
    url: '/doc/supervision/my-supervisions',
    method: 'get',
    params
  })
}

export const createUrge = (data: DocUrgeDTO) => {
  return request<DocUrgeLogVO>({
    url: '/doc/supervision/urge',
    method: 'post',
    data
  })
}

export const urgeHandling = (id: number, urgeContent?: string) => {
  return request<DocUrgeLogVO>({
    url: `/doc/handling/${id}/urge`,
    method: 'post',
    params: { urgeContent }
  })
}

export const getUrgeLogsByHandling = (handlingId: number) => {
  return request<DocUrgeLogVO[]>({
    url: `/doc/handling/${handlingId}/urge-logs`,
    method: 'get'
  })
}

export const acknowledgeUrge = (id: number) => {
  return request<DocUrgeLogVO>({
    url: `/doc/supervision/urge/${id}/acknowledge`,
    method: 'put'
  })
}

export const getUrgeLogsByIncoming = (incomingId: number) => {
  return request<DocUrgeLogVO[]>({
    url: `/doc/supervision/urge/list/${incomingId}`,
    method: 'get'
  })
}
