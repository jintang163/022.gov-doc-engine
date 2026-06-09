import request from '@/utils/request'
import type { PageResult } from '@/types/template'
import type {
  DocPermissionVO,
  DocPermissionDTO,
  DocAuditLogVO,
  DocAuditLogQueryDTO,
  DocIntegrityVO,
  DocIntegrityDTO,
  DocIntegrityVerifyDTO
} from '@/types/security'

export const getPermissionPage = (params: { pageNum: number; pageSize: number; permissionType?: string; permissionCode?: string }) => {
  return request<PageResult<DocPermissionVO>>({
    url: '/doc/permission/page',
    method: 'get',
    params
  })
}

export const getPermissionList = () => {
  return request<DocPermissionVO[]>({
    url: '/doc/permission/list',
    method: 'get'
  })
}

export const getPermissionByCode = (code: string) => {
  return request<DocPermissionVO>({
    url: `/doc/permission/code/${code}`,
    method: 'get'
  })
}

export const createPermission = (data: DocPermissionDTO) => {
  return request<DocPermissionVO>({
    url: '/doc/permission',
    method: 'post',
    data
  })
}

export const updatePermission = (id: number, data: DocPermissionDTO) => {
  return request<DocPermissionVO>({
    url: `/doc/permission/${id}`,
    method: 'put',
    data
  })
}

export const deletePermission = (id: number) => {
  return request<void>({
    url: `/doc/permission/${id}`,
    method: 'delete'
  })
}

export const checkPermission = (permissionCode: string) => {
  return request<boolean>({
    url: '/doc/permission/check',
    method: 'get',
    params: { permissionCode }
  })
}

export const checkSecurityLevel = (userId: string, securityLevel: string) => {
  return request<boolean>({
    url: '/doc/permission/check-security-level',
    method: 'get',
    params: { userId, securityLevel }
  })
}

export const getAuditLogPage = (params: DocAuditLogQueryDTO) => {
  return request<PageResult<DocAuditLogVO>>({
    url: '/doc/audit-log/page',
    method: 'get',
    params
  })
}

export const getIntegrityByDocId = (docId: number) => {
  return request<DocIntegrityVO>({
    url: `/doc/integrity/doc/${docId}`,
    method: 'get'
  })
}

export const getIntegrityHistory = (docId: number) => {
  return request<DocIntegrityVO[]>({
    url: `/doc/integrity/doc/${docId}/history`,
    method: 'get'
  })
}

export const createIntegrityRecord = (data: DocIntegrityDTO) => {
  return request<DocIntegrityVO>({
    url: '/doc/integrity',
    method: 'post',
    data
  })
}

export const verifyIntegrity = (data: DocIntegrityVerifyDTO) => {
  return request<DocIntegrityVO>({
    url: '/doc/integrity/verify',
    method: 'post',
    data
  })
}

export const computeHash = (content: string) => {
  return request<string>({
    url: '/doc/integrity/compute-hash',
    method: 'post',
    data: content
  })
}
