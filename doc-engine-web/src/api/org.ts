import request from '@/utils/request'
import type { SysUnitVO, SysUnitSaveDTO, SysDeptVO, SysDeptSaveDTO, SysPostVO, SysPostSaveDTO, SysPostQueryDTO, SysUserVO, SysUserSaveDTO, SysUserQueryDTO } from '@/types/org'
import type { PageResult, Result } from '@/types/template'

export const getUnitTree = () => {
  return request<Result<SysUnitVO[]>>({
    url: '/sys/unit/tree',
    method: 'get'
  })
}

export const getUnitDetail = (id: number) => {
  return request<Result<SysUnitVO>>({
    url: `/sys/unit/${id}`,
    method: 'get'
  })
}

export const saveUnit = (data: SysUnitSaveDTO) => {
  return request<Result<number>>({
    url: '/sys/unit',
    method: 'post',
    data
  })
}

export const updateUnit = (data: SysUnitSaveDTO) => {
  return request<Result<void>>({
    url: '/sys/unit',
    method: 'put',
    data
  })
}

export const deleteUnit = (id: number) => {
  return request<Result<void>>({
    url: `/sys/unit/${id}`,
    method: 'delete'
  })
}

export const getUnitChildren = (parentId: number) => {
  return request<Result<SysUnitVO[]>>({
    url: `/sys/unit/children/${parentId}`,
    method: 'get'
  })
}

export const getDeptTree = (unitId: number) => {
  return request<Result<SysDeptVO[]>>({
    url: '/sys/dept/tree',
    method: 'get',
    params: { unitId }
  })
}

export const getDeptDetail = (id: number) => {
  return request<Result<SysDeptVO>>({
    url: `/sys/dept/${id}`,
    method: 'get'
  })
}

export const saveDept = (data: SysDeptSaveDTO) => {
  return request<Result<number>>({
    url: '/sys/dept',
    method: 'post',
    data
  })
}

export const updateDept = (data: SysDeptSaveDTO) => {
  return request<Result<void>>({
    url: '/sys/dept',
    method: 'put',
    data
  })
}

export const deleteDept = (id: number) => {
  return request<Result<void>>({
    url: `/sys/dept/${id}`,
    method: 'delete'
  })
}

export const getPostPage = (params: SysPostQueryDTO & { pageNum?: number; pageSize?: number }) => {
  return request<PageResult<SysPostVO>>({
    url: '/sys/post/page',
    method: 'get',
    params
  })
}

export const getPostDetail = (id: number) => {
  return request<Result<SysPostVO>>({
    url: `/sys/post/${id}`,
    method: 'get'
  })
}

export const savePost = (data: SysPostSaveDTO) => {
  return request<Result<number>>({
    url: '/sys/post',
    method: 'post',
    data
  })
}

export const updatePost = (data: SysPostSaveDTO) => {
  return request<Result<void>>({
    url: '/sys/post',
    method: 'put',
    data
  })
}

export const deletePost = (id: number) => {
  return request<Result<void>>({
    url: `/sys/post/${id}`,
    method: 'delete'
  })
}

export const listPosts = (unitId?: number) => {
  return request<Result<SysPostVO[]>>({
    url: '/sys/post/list',
    method: 'get',
    params: { unitId }
  })
}

export const getUserPage = (params: SysUserQueryDTO) => {
  return request<PageResult<SysUserVO>>({
    url: '/sys/user/page',
    method: 'get',
    params
  })
}

export const getUserDetail = (id: number) => {
  return request<Result<SysUserVO>>({
    url: `/sys/user/${id}`,
    method: 'get'
  })
}

export const saveUser = (data: SysUserSaveDTO) => {
  return request<Result<number>>({
    url: '/sys/user',
    method: 'post',
    data
  })
}

export const updateUser = (data: SysUserSaveDTO) => {
  return request<Result<void>>({
    url: '/sys/user',
    method: 'put',
    data
  })
}

export const deleteUser = (id: number) => {
  return request<Result<void>>({
    url: `/sys/user/${id}`,
    method: 'delete'
  })
}

export const listUsersByDept = (deptId: number) => {
  return request<Result<SysUserVO[]>>({
    url: `/sys/user/dept/${deptId}`,
    method: 'get'
  })
}

export const listUsersByPost = (postId: number) => {
  return request<Result<SysUserVO[]>>({
    url: `/sys/user/post/${postId}`,
    method: 'get'
  })
}

export const listUsersByUnit = (unitId: number) => {
  return request<Result<SysUserVO[]>>({
    url: `/sys/user/unit/${unitId}`,
    method: 'get'
  })
}

export const searchUsers = (keyword: string) => {
  return request<Result<SysUserVO[]>>({
    url: '/sys/user/search',
    method: 'get',
    params: { keyword }
  })
}
