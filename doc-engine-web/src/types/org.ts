export interface SysUnitVO {
  id: number
  unitCode: string
  unitName: string
  unitType: string
  parentId: number | null
  shortName: string
  leader: string
  phone: string
  address: string
  sortOrder: number
  status: number
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  parentName: string
  children: SysUnitVO[]
  userCount: number
  deptCount: number
}

export interface SysUnitSaveDTO {
  id?: number
  unitCode: string
  unitName: string
  unitType?: string
  parentId?: number | null
  shortName?: string
  leader?: string
  phone?: string
  address?: string
  sortOrder?: number
  remark?: string
}

export interface SysUnitQueryDTO {
  unitCode?: string
  unitName?: string
  unitType?: string
  parentId?: number | null
  status?: number
  keyword?: string
}

export interface SysDeptVO {
  id: number
  unitId: number
  deptCode: string
  deptName: string
  parentId: number | null
  deptType: string
  leader: string
  phone: string
  email: string
  sortOrder: number
  status: number
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  unitName: string
  parentName: string
  children: SysDeptVO[]
  userCount: number
}

export interface SysDeptSaveDTO {
  id?: number
  unitId: number
  deptCode: string
  deptName: string
  parentId?: number | null
  deptType?: string
  leader?: string
  phone?: string
  email?: string
  sortOrder?: number
  remark?: string
}

export interface SysDeptQueryDTO {
  unitId?: number
  deptCode?: string
  deptName?: string
  parentId?: number | null
  status?: number
  keyword?: string
}

export interface SysPostVO {
  id: number
  unitId: number
  postCode: string
  postName: string
  postType: string
  postLevel: number
  deptId: number | null
  sortOrder: number
  status: number
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  unitName: string
  deptName: string
}

export interface SysPostSaveDTO {
  id?: number
  unitId?: number
  postCode: string
  postName: string
  postType?: string
  postLevel?: number
  deptId?: number | null
  sortOrder?: number
  remark?: string
}

export interface SysPostQueryDTO {
  unitId?: number
  deptId?: number
  postCode?: string
  postName?: string
  postType?: string
  status?: number
  keyword?: string
}

export interface SysUserVO {
  id: number
  userCode: string
  userName: string
  loginName: string
  gender: number | null
  phone: string
  email: string
  avatar: string
  unitId: number
  deptId: number | null
  postId: number | null
  postName: string
  sortOrder: number
  status: number
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  unitName: string
  deptName: string
}

export interface SysUserSaveDTO {
  id?: number
  userCode: string
  userName: string
  loginName?: string
  password?: string
  gender?: number | null
  phone?: string
  email?: string
  avatar?: string
  unitId: number
  deptId?: number | null
  postId?: number | null
  postName?: string
  sortOrder?: number
  remark?: string
}

export interface SysUserQueryDTO {
  userCode?: string
  userName?: string
  unitId?: number
  deptId?: number
  postId?: number
  status?: number
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export const unitTypeOptions = [
  { label: '机关', value: 'agency' },
  { label: '事业单位', value: 'institution' },
  { label: '企业', value: 'enterprise' }
]

export const postTypeOptions = [
  { label: '领导岗位', value: 'leader' },
  { label: '部门负责人', value: 'dept_leader' },
  { label: '普通岗位', value: 'staff' }
]

export const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 0 }
]

export const orgStatusOptions = [
  { label: '启用', value: 1, color: 'success' },
  { label: '禁用', value: 0, color: 'error' }
]
