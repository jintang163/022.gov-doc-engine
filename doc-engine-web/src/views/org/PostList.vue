<template>
  <div class="post-list">
    <a-card title="岗位管理" :bordered="false">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item label="所属单位">
          <a-select
            v-model:value="queryForm.unitId"
            placeholder="请选择所属单位"
            allow-clear
            :options="unitSelectOptions"
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="岗位编码">
          <a-input v-model:value="queryForm.postCode" placeholder="请输入岗位编码" allow-clear />
        </a-form-item>
        <a-form-item label="岗位名称">
          <a-input v-model:value="queryForm.postName" placeholder="请输入岗位名称" allow-clear />
        </a-form-item>
        <a-form-item label="岗位类型">
          <a-select
            v-model:value="queryForm.postType"
            placeholder="请选择岗位类型"
            allow-clear
            :options="postTypeSelectOptions"
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
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="请输入关键词" allow-clear />
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
          <template v-if="column.key === 'postType'">
            {{ getPostTypeLabel(record.postType) }}
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
                title="确定要删除该岗位吗？"
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
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="岗位编码" name="postCode">
              <a-input v-model:value="formData.postCode" placeholder="请输入岗位编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="岗位名称" name="postName">
              <a-input v-model:value="formData.postName" placeholder="请输入岗位名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="岗位类型" name="postType">
              <a-select v-model:value="formData.postType" placeholder="请选择岗位类型" :options="postTypeSelectOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="岗位级别" name="postLevel">
              <a-input-number v-model:value="formData.postLevel" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所属单位" name="unitId">
              <a-select v-model:value="formData.unitId" placeholder="请选择所属单位" :options="unitSelectOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
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
import { getUnitTree, getPostPage, savePost, updatePost, deletePost } from '@/api/org'
import type { SysUnitVO, SysPostVO, SysPostSaveDTO, SysPostQueryDTO } from '@/types/org'
import { postTypeOptions, orgStatusOptions } from '@/types/org'

const loading = ref(false)
const saving = ref(false)
const dataSource = ref<SysPostVO[]>([])
const unitTreeData = ref<SysUnitVO[]>([])
const modalVisible = ref(false)
const formRef = ref<FormInstance>()

const queryForm = reactive<SysPostQueryDTO & { pageNum?: number; pageSize?: number }>({
  unitId: undefined,
  postCode: undefined,
  postName: undefined,
  postType: undefined,
  status: undefined,
  keyword: undefined,
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

const formData = reactive<SysPostSaveDTO>({
  postCode: '',
  postName: '',
  postType: undefined,
  postLevel: undefined,
  unitId: undefined,
  deptId: undefined,
  sortOrder: 0,
  remark: undefined
})

const rules = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
}

const isEdit = computed(() => !!formData.id)
const modalTitle = computed(() => isEdit.value ? '编辑岗位' : '新增岗位')

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

const postTypeSelectOptions = postTypeOptions.map(opt => ({ label: opt.label, value: opt.value }))
const statusSelectOptions = orgStatusOptions.map(opt => ({ label: opt.label, value: opt.value }))

const columns = [
  { title: '岗位编码', dataIndex: 'postCode', key: 'postCode', width: 120 },
  { title: '岗位名称', dataIndex: 'postName', key: 'postName', width: 150 },
  { title: '岗位类型', dataIndex: 'postType', key: 'postType', width: 120 },
  { title: '岗位级别', dataIndex: 'postLevel', key: 'postLevel', width: 100 },
  { title: '所属单位', dataIndex: 'unitName', key: 'unitName', width: 150 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const getPostTypeLabel = (type: string) => {
  return postTypeOptions.find(opt => opt.value === type)?.label || type
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
    const res = await getPostPage(queryForm)
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

const handleSearch = () => {
  queryForm.pageNum = 1
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryForm.unitId = undefined
  queryForm.postCode = undefined
  queryForm.postName = undefined
  queryForm.postType = undefined
  queryForm.status = undefined
  queryForm.keyword = undefined
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
  formData.postCode = ''
  formData.postName = ''
  formData.postType = undefined
  formData.postLevel = undefined
  formData.unitId = undefined
  formData.deptId = undefined
  formData.sortOrder = 0
  formData.remark = undefined
}

const handleAdd = () => {
  resetFormData()
  modalVisible.value = true
}

const handleEdit = (record: SysPostVO) => {
  formData.id = record.id
  formData.postCode = record.postCode
  formData.postName = record.postName
  formData.postType = record.postType
  formData.postLevel = record.postLevel
  formData.unitId = record.unitId
  formData.deptId = record.deptId
  formData.sortOrder = record.sortOrder
  formData.remark = record.remark
  modalVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    if (isEdit.value) {
      const res = await updatePost(formData)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await savePost(formData)
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
    const res = await deletePost(id)
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
.post-list {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
