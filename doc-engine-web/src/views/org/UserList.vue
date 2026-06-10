<template>
  <div class="user-list">
    <a-card title="人员管理" :bordered="false">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="请输入关键词" allow-clear />
        </a-form-item>
        <a-form-item label="所属单位">
          <a-select
            v-model:value="queryForm.unitId"
            placeholder="请选择所属单位"
            allow-clear
            :options="unitSelectOptions"
            style="width: 180px"
            @change="handleQueryUnitChange"
          />
        </a-form-item>
        <a-form-item label="所属部门">
          <a-select
            v-model:value="queryForm.deptId"
            placeholder="请选择所属部门"
            allow-clear
            :options="deptSelectOptions"
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="岗位">
          <a-select
            v-model:value="queryForm.postId"
            placeholder="请选择岗位"
            allow-clear
            :options="postSelectOptions"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            :options="statusSelectOptions"
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item label="性别">
          <a-select
            v-model:value="queryFormGender"
            placeholder="请选择性别"
            allow-clear
            :options="genderSelectOptions"
            style="width: 100px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'gender'">
            <a-tag :color="record.gender === 1 ? 'blue' : record.gender === 0 ? 'pink' : 'default'">
              {{ getGenderLabel(record.gender) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该人员吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="saving"
      @ok="handleSave"
      @cancel="handleModalCancel"
      width="700px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="人员编码" name="userCode">
              <a-input v-model:value="formData.userCode" placeholder="请输入人员编码" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="人员姓名" name="userName">
              <a-input v-model:value="formData.userName" placeholder="请输入人员姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="登录名" name="loginName">
              <a-input v-model:value="formData.loginName" placeholder="请输入登录名" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="密码" name="password" v-if="!isEdit">
              <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="性别" name="gender">
              <a-select v-model:value="formData.gender" placeholder="请选择性别" :options="genderSelectOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="电话" name="phone">
              <a-input v-model:value="formData.phone" placeholder="请输入电话" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="所属单位" name="unitId">
              <a-select
                v-model:value="formData.unitId"
                placeholder="请选择所属单位"
                :options="unitSelectOptions"
                allow-clear
                @change="handleFormUnitChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="所属部门" name="deptId">
              <a-select v-model:value="formData.deptId" placeholder="请选择所属部门" :options="formDeptSelectOptions" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="岗位" name="postId">
              <a-select v-model:value="formData.postId" placeholder="请选择岗位" :options="formPostSelectOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序" name="sortOrder">
              <a-input-number v-model:value="formData.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="备注" name="remark">
              <a-textarea v-model:value="formData.remark" placeholder="请输入备注" :rows="3" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, TablePaginationConfig } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import {
  getUnitTree,
  getDeptTree,
  getUserPage,
  saveUser,
  updateUser,
  deleteUser,
  listPosts
} from '@/api/org'
import type { SysUnitVO, SysDeptVO, SysUserVO, SysUserSaveDTO, SysUserQueryDTO } from '@/types/org'
import { genderOptions, orgStatusOptions } from '@/types/org'

const loading = ref(false)
const saving = ref(false)
const dataSource = ref<SysUserVO[]>([])
const unitTreeData = ref<SysUnitVO[]>([])
const queryDeptList = ref<SysDeptVO[]>([])
const formDeptList = ref<SysDeptVO[]>([])
const queryPostList = ref<any[]>([])
const formPostList = ref<any[]>([])
const modalVisible = ref(false)
const formRef = ref<FormInstance>()
const queryFormGender = ref<number | undefined>(undefined)

const queryForm = reactive<SysUserQueryDTO>({
  keyword: undefined,
  unitId: undefined,
  deptId: undefined,
  postId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const formData = reactive<SysUserSaveDTO>({
  userCode: '',
  userName: '',
  loginName: undefined,
  password: undefined,
  gender: undefined,
  phone: undefined,
  email: undefined,
  avatar: undefined,
  unitId: 0,
  deptId: undefined,
  postId: undefined,
  postName: undefined,
  sortOrder: 0,
  remark: undefined
})

const rules = {
  userCode: [{ required: true, message: '请输入人员编码', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入人员姓名', trigger: 'blur' }],
  unitId: [{ required: true, message: '请选择所属单位', trigger: 'change' }]
}

const isEdit = computed(() => !!formData.id)
const modalTitle = computed(() => isEdit.value ? '编辑人员' : '新增人员')

const flattenUnits = (nodes: SysUnitVO[]): { label: string; value: number }[] => {
  const result: { label: string; value: number }[] = []
  const walk = (items: SysUnitVO[]) => {
    for (const item of items) {
      result.push({ label: item.unitName, value: item.id })
      if (item.children?.length) walk(item.children)
    }
  }
  walk(nodes)
  return result
}

const unitSelectOptions = computed(() => flattenUnits(unitTreeData.value))
const deptSelectOptions = computed(() => queryDeptList.value.map(d => ({ label: d.deptName, value: d.id })))
const formDeptSelectOptions = computed(() => formDeptList.value.map(d => ({ label: d.deptName, value: d.id })))
const postSelectOptions = computed(() => queryPostList.value.map(p => ({ label: p.postName, value: p.id })))
const formPostSelectOptions = computed(() => formPostList.value.map(p => ({ label: p.postName, value: p.id })))
const genderSelectOptions = genderOptions.map(opt => ({ label: opt.label, value: opt.value }))
const statusSelectOptions = orgStatusOptions.map(opt => ({ label: opt.label, value: opt.value }))

const columns = [
  { title: '人员编码', dataIndex: 'userCode', key: 'userCode', width: 100 },
  { title: '人员姓名', dataIndex: 'userName', key: 'userName', width: 100 },
  { title: '性别', dataIndex: 'gender', key: 'gender', width: 70 },
  { title: '登录名', dataIndex: 'loginName', key: 'loginName', width: 100 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 120 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 150 },
  { title: '所属单位', dataIndex: 'unitName', key: 'unitName', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
  { title: '岗位', dataIndex: 'postName', key: 'postName', width: 100 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const getGenderLabel = (gender: number | null) => {
  return genderOptions.find(opt => opt.value === gender)?.label || '未知'
}

const getStatusLabel = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.label || '未知'
}

const getStatusColor = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.color || 'default'
}

const fetchUnitTree = async () => {
  try {
    const res = await getUnitTree()
    if (res.code === 200) {
      unitTreeData.value = res.data
    }
  } catch (error) {
    message.error('获取单位树失败')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    const res = await getUserPage(params)
    if (res.code === 200) {
      dataSource.value = res.data.list
      pagination.total = res.data.total
      pagination.current = res.data.pageNum
      pagination.pageSize = res.data.pageSize
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadQueryDepts = async (unitId: number) => {
  try {
    const res = await getDeptTree(unitId)
    if (res.code === 200) {
      queryDeptList.value = flattenDepts(res.data)
    }
  } catch (error) {
    queryDeptList.value = []
  }
}

const loadFormDepts = async (unitId: number) => {
  try {
    const res = await getDeptTree(unitId)
    if (res.code === 200) {
      formDeptList.value = flattenDepts(res.data)
    }
  } catch (error) {
    formDeptList.value = []
  }
}

const flattenDepts = (nodes: SysDeptVO[]): SysDeptVO[] => {
  const result: SysDeptVO[] = []
  const walk = (items: SysDeptVO[]) => {
    for (const item of items) {
      result.push(item)
      if (item.children?.length) walk(item.children)
    }
  }
  walk(nodes)
  return result
}

const loadQueryPosts = async (unitId?: number) => {
  try {
    const res = await listPosts(unitId)
    if (res.code === 200) {
      queryPostList.value = res.data
    }
  } catch (error) {
    queryPostList.value = []
  }
}

const loadFormPosts = async (unitId?: number) => {
  try {
    const res = await listPosts(unitId)
    if (res.code === 200) {
      formPostList.value = res.data
    }
  } catch (error) {
    formPostList.value = []
  }
}

const handleQueryUnitChange = (value: number | undefined) => {
  queryForm.deptId = undefined
  queryDeptList.value = []
  if (value) {
    loadQueryDepts(value)
    loadQueryPosts(value)
  } else {
    queryPostList.value = []
  }
}

const handleFormUnitChange = (value: number | undefined) => {
  formData.deptId = undefined
  formDeptList.value = []
  if (value) {
    loadFormDepts(value)
    loadFormPosts(value)
  } else {
    formPostList.value = []
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryForm.keyword = undefined
  queryForm.unitId = undefined
  queryForm.deptId = undefined
  queryForm.postId = undefined
  queryForm.status = undefined
  queryFormGender.value = undefined
  queryDeptList.value = []
  queryPostList.value = []
  handleSearch()
}

const handleTableChange = (pag: TablePaginationConfig) => {
  queryForm.pageNum = pag.current
  queryForm.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const resetFormData = () => {
  formData.id = undefined
  formData.userCode = ''
  formData.userName = ''
  formData.loginName = undefined
  formData.password = undefined
  formData.gender = undefined
  formData.phone = undefined
  formData.email = undefined
  formData.avatar = undefined
  formData.unitId = 0
  formData.deptId = undefined
  formData.postId = undefined
  formData.postName = undefined
  formData.sortOrder = 0
  formData.remark = undefined
  formDeptList.value = []
  formPostList.value = []
}

const handleAdd = () => {
  resetFormData()
  modalVisible.value = true
}

const handleEdit = (record: SysUserVO) => {
  formData.id = record.id
  formData.userCode = record.userCode
  formData.userName = record.userName
  formData.loginName = record.loginName
  formData.password = undefined
  formData.gender = record.gender
  formData.phone = record.phone
  formData.email = record.email
  formData.avatar = record.avatar
  formData.unitId = record.unitId
  formData.deptId = record.deptId
  formData.postId = record.postId
  formData.postName = record.postName
  formData.sortOrder = record.sortOrder
  formData.remark = record.remark
  if (record.unitId) {
    loadFormDepts(record.unitId)
    loadFormPosts(record.unitId)
  }
  modalVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    if (isEdit.value) {
      const res = await updateUser(formData)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await saveUser(formData)
      if (res.code === 200) {
        message.success('新增成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error(res.message || '新增失败')
      }
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    saving.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleDelete = async (id: number) => {
  loading.value = true
  try {
    const res = await deleteUser(id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUnitTree()
  fetchData()
})
</script>

<style scoped lang="less">
.user-list {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
