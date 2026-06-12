import request from '@/utils/request'
import type { PageResult } from '@/types/template'
import type {
  DocIncomingVO,
  DocIncomingDTO,
  DocIncomingQueryDTO,
  DocHandlingVO,
  DocHandlingDTO,
  DocHandlingFeedbackDTO,
  DocHandlingQueryDTO,
  DeptRecommendVO
} from '@/types/incoming'

export const getIncomingPage = (params: DocIncomingQueryDTO) => {
  return request<PageResult<DocIncomingVO>>({
    url: '/doc/incoming/page',
    method: 'get',
    params
  })
}

export const getIncomingDetail = (id: number) => {
  return request<DocIncomingVO>({
    url: `/doc/incoming/${id}`,
    method: 'get'
  })
}

export const registerIncoming = (data: DocIncomingDTO) => {
  return request<DocIncomingVO>({
    url: '/doc/incoming/register',
    method: 'post',
    data
  })
}

export const registerIncomingFromApi = (data: DocIncomingDTO) => {
  return request<DocIncomingVO>({
    url: '/doc/incoming/api/register',
    method: 'post',
    data
  })
}

export const getHandlingPage = (params: DocHandlingQueryDTO) => {
  return request<PageResult<DocHandlingVO>>({
    url: '/doc/handling/page',
    method: 'get',
    params
  })
}

export const getHandlingDetail = (id: number) => {
  return request<DocHandlingVO>({
    url: `/doc/handling/${id}`,
    method: 'get'
  })
}

export const draftOpinion = (data: DocHandlingDTO) => {
  return request<DocHandlingVO>({
    url: '/doc/handling/draft-opinion',
    method: 'post',
    data
  })
}

export const assignHandling = (data: DocHandlingDTO) => {
  return request<DocHandlingVO>({
    url: '/doc/handling/assign',
    method: 'post',
    data
  })
}

export const submitFeedback = (data: DocHandlingFeedbackDTO) => {
  return request<DocHandlingVO>({
    url: '/doc/handling/feedback',
    method: 'post',
    data
  })
}

export const getMyHandlings = (params: DocHandlingQueryDTO) => {
  return request<PageResult<DocHandlingVO>>({
    url: '/doc/handling/my-handlings',
    method: 'get',
    params
  })
}

export const recommendDepts = (params: {
  docTitle?: string
  docType?: string
  keyword?: string
}) => {
  return request<DeptRecommendVO[]>({
    url: '/doc/recommend/dept',
    method: 'get',
    params
  })
}
