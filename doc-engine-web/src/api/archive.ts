import request from '@/utils/request'
import type { PageResult } from '@/types/template'
import type {
  DocArchiveVO,
  DocArchiveDTO,
  DocArchiveQueryDTO,
  DocBorrowVO,
  DocBorrowApplyDTO,
  DocBorrowApproveDTO,
  DocBorrowQueryDTO
} from '@/types/archive'

export const getArchivePage = (params: DocArchiveQueryDTO) => {
  return request<PageResult<DocArchiveVO>>({
    url: '/doc/archive/page',
    method: 'get',
    params
  })
}

export const getArchiveDetail = (id: number) => {
  return request<DocArchiveVO>({
    url: `/doc/archive/${id}`,
    method: 'get'
  })
}

export const manualArchive = (data: DocArchiveDTO) => {
  return request<DocArchiveVO>({
    url: '/doc/archive/manual',
    method: 'post',
    data
  })
}

export const autoArchive = (docId: number) => {
  return request<void>({
    url: `/doc/archive/auto/${docId}`,
    method: 'post'
  })
}

export const getArchiveByYear = (archiveYear: number) => {
  return request<DocArchiveVO[]>({
    url: `/doc/archive/year/${archiveYear}`,
    method: 'get'
  })
}

export const getArchiveStats = () => {
  return request<Record<string, any>[]>({
    url: '/doc/archive/stats',
    method: 'get'
  })
}

export const searchByDocNumber = (docNumber: string) => {
  return request<DocArchiveVO>({
    url: '/doc/archive/search/doc-number',
    method: 'get',
    params: { docNumber }
  })
}

export const getBorrowPage = (params: DocBorrowQueryDTO) => {
  return request<PageResult<DocBorrowVO>>({
    url: '/doc/borrow/page',
    method: 'get',
    params
  })
}

export const getBorrowDetail = (id: number) => {
  return request<DocBorrowVO>({
    url: `/doc/borrow/${id}`,
    method: 'get'
  })
}

export const applyBorrow = (data: DocBorrowApplyDTO) => {
  return request<DocBorrowVO>({
    url: '/doc/borrow/apply',
    method: 'post',
    data
  })
}

export const approveBorrow = (data: DocBorrowApproveDTO) => {
  return request<void>({
    url: '/doc/borrow/approve',
    method: 'post',
    data
  })
}

export const returnBorrow = (borrowId: number) => {
  return request<void>({
    url: `/doc/borrow/return/${borrowId}`,
    method: 'post'
  })
}

export const getWatermarkedContent = (borrowId: number) => {
  return request<string>({
    url: `/doc/borrow/watermark-content/${borrowId}`,
    method: 'get'
  })
}

export const getMyBorrows = (params: DocBorrowQueryDTO) => {
  return request<PageResult<DocBorrowVO>>({
    url: '/doc/borrow/my-borrows',
    method: 'get',
    params
  })
}

export const getPendingApprovals = (params: DocBorrowQueryDTO) => {
  return request<PageResult<DocBorrowVO>>({
    url: '/doc/borrow/pending-approvals',
    method: 'get',
    params
  })
}
