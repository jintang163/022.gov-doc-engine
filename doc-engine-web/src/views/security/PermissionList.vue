<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限配置</h2>
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增权限
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="权限类型">
          <a-select
            v-model:value="searchForm.permissionType"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
            :options="permissionTypeOptions"
          />
        </a-form-item>
        <a-form-item label="权限编码">
          <a-input
            v-model:value="searchForm.permissionCode"
            placeholder="请输入权限编码"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="loading">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handlePageChange"
        bordered
        row-key="id"
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'permissionTypeName'">
            <a-tag :color="record.permissionType === 'action' ? 'blue' : 'green'">
              {{ record.permissionTypeName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled === 1"
              @change="(checked: boolean) => handleEnabledChange(record, checked)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该权限吗？"
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
      :title="isEdit ? '编辑权限' : '新增权限'"
      :width="640"
      :confirm-loading="confirmLoading"
      @ok="handleModalOk"
      @cancel="modalVisible = false"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="权限编码" name="permissionCode">
              <a-input
                v-model:value="formData.permissionCode"
                placeholder="请输入权限编码"
                :disabled="isEdit"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="权限名称" name="permissionName">
              <a-input v-model:value="formData.permissionName" placeholder="请输入权限名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="权限类型" name="permissionType">
              <a-select
                v-model:value="formData.permissionType"
                placeholder="请选择权限类型"
                :options="permissionTypeOptions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密级限制" name="securityLevel">
              <a-select
                v-model:value="formData.securityLevel"
                placeholder="请选择密级限制"
                allow-clear
                :options="securityLevelOptions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="允许角色" name="allowedRoles">
              <a-input v-model:value="formData.allowedRoles" placeholder="请输入允许角色，多个用逗号分隔" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="允许用户" name="allowedUsers">
              <a-input v-model:value="formData.allowedUsers" placeholder="请输入允许用户，多个用逗号分隔" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="允许部门" name="allowedDepts">
              <a-input v-model:value="formData.allowedDepts" placeholder="请输入允许部门，多个用逗号分隔" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序" name="sortOrder">
              <a-input-number v-model:value="formData.sortOrder" :min="0" style="width: 100%;" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="enabled">
              <a-switch v-model:checked="enabledChecked" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="描述" name="description">
              <a-textarea v-model:value="formData.description" placeholder="请输入描述" :rows="3" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getPermissionPage, createPermission, updatePermission, deletePermission } from '@/api/security'
import type { DocPermissionVO, DocPermissionDTO } from '@/types/security'
import { permissionTypeOptions, securityLevelOptions } from '@/types/security'
import type { FormInstance } from 'ant-design-vue'

const loading = ref(false)
const confirmLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number>(0)
const formRef = ref<FormInstance>()
const enabledChecked = ref(true)

const dataSource = ref<DocPermissionVO[]>([])

const searchForm = reactive({
  permissionType: undefined as string | undefined,
  permissionCode: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const formData = reactive<DocPermissionDTO>({
  permissionCode: '',
  permissionName: '',
  permissionType: 'action',
  securityLevel: undefined,
  allowedRoles: undefined,
  allowedUsers: undefined,
  allowedDepts: undefined,
  description: undefined,
  enabled: 1,
  sortOrder: 0
})

const formRules = {
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

const columns = [
  { title: '权限编码', dataIndex: 'permissionCode', key: 'permissionCode', width: 160 },
  { title: '权限名称', dataIndex: 'permissionName', key: 'permissionName', width: 160 },
  { title: '权限类型', dataIndex: 'permissionTypeName', key: 'permissionTypeName', width: 100 },
  { title: '密级限制', dataIndex: 'securityLevel', key: 'securityLevel', width: 100 },
  { title: '允许角色', dataIndex: 'allowedRoles', key: 'allowedRoles', width: 150, ellipsis: true },
  { title: '允许用户', dataIndex: 'allowedUsers', key: 'allowedUsers', width: 150, ellipsis: true },
  { title: '允许部门', dataIndex: 'allowedDepts', key: 'allowedDepts', width: 150, ellipsis: true },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 80 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const result = await getPermissionPage(searchForm)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.pageNum = 1
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.permissionType = undefined
  searchForm.permissionCode = undefined
  handleSearch()
}

const handlePageChange = (paginationInfo: { current: number; pageSize: number }) => {
  searchForm.pageNum = paginationInfo.current
  searchForm.pageSize = paginationInfo.pageSize
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  loadData()
}

const resetForm = () => {
  Object.assign(formData, {
    permissionCode: '',
    permissionName: '',
    permissionType: 'action',
    securityLevel: undefined,
    allowedRoles: undefined,
    allowedUsers: undefined,
    allowedDepts: undefined,
    description: undefined,
    enabled: 1,
    sortOrder: 0
  })
  enabledChecked.value = true
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  editingId.value = 0
  modalVisible.value = true
}

const handleEdit = (record: DocPermissionVO) => {
  resetForm()
  isEdit.value = true
  editingId.value = record.id
  Object.assign(formData, {
    permissionCode: record.permissionCode,
    permissionName: record.permissionName,
    permissionType: record.permissionType,
    securityLevel: record.securityLevel || undefined,
    allowedRoles: record.allowedRoles || undefined,
    allowedUsers: record.allowedUsers || undefined,
    allowedDepts: record.allowedDepts || undefined,
    description: record.description || undefined,
    enabled: record.enabled,
    sortOrder: record.sortOrder
  })
  enabledChecked.value = record.enabled === 1
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()
    confirmLoading.value = true
    formData.enabled = enabledChecked.value ? 1 : 0
    if (isEdit.value) {
      await updatePermission(editingId.value, formData)
      message.success('更新成功')
    } else {
      await createPermission(formData)
      message.success('创建成功')
    }
    modalVisible.value = false
    loadData()
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    confirmLoading.value = false
  }
}

const handleEnabledChange = async (record: DocPermissionVO, checked: boolean) => {
  const dto: DocPermissionDTO = {
    permissionCode: record.permissionCode,
    permissionName: record.permissionName,
    permissionType: record.permissionType,
    securityLevel: record.securityLevel || undefined,
    allowedRoles: record.allowedRoles || undefined,
    allowedUsers: record.allowedUsers || undefined,
    allowedDepts: record.allowedDepts || undefined,
    description: record.description || undefined,
    enabled: checked ? 1 : 0,
    sortOrder: record.sortOrder
  }
  try {
    await updatePermission(record.id, dto)
    message.success('状态更新成功')
    loadData()
  } catch (error) {
    message.error('状态更新失败')
  }
}

const handleDelete = async (id: number) => {
  loading.value = true
  try {
    await deletePermission(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="less" scoped>
.page-container {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
  }
}
</style>
