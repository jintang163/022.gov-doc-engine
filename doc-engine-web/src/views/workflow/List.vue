<template>
  <div class="workflow-list">
    <a-card title="流程定义管理" :bordered="false">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item label="流程编码">
          <a-input
            v-model:value="queryForm.processKey"
            placeholder="请输入流程编码"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="流程名称">
          <a-input
            v-model:value="queryForm.processName"
            placeholder="请输入流程名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="流程类型">
          <a-select
            v-model:value="queryForm.processCategory"
            placeholder="请选择流程类型"
            allow-clear
            :options="processCategoryOptions"
          />
        </a-form-item>
        <a-form-item label="流程分类">
          <a-select
            v-model:value="queryForm.processCategory"
            placeholder="请选择流程分类"
            allow-clear
            :options="processCategoryOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            :options="statusOptions"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="keyword"
            placeholder="请输入关键词"
            allow-clear
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
              新建
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
          <template v-if="column.key === 'isCurrentVersion'">
            <a-tag :color="record.isCurrentVersion === 1 ? 'success' : 'default'">
              {{ record.isCurrentVersion === 1 ? '是' : '否' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handleEdit(record.id)"
              >
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleDesign(record.id)"
              >
                设计
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handlePreview(record.id)"
              >
                预览
              </a-button>
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handlePublish(record.id)"
              >
                发布
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleDisable(record.id)"
              >
                停用
              </a-button>
              <a-popconfirm
                v-if="record.status === 0"
                title="确定要删除该流程定义吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import {
  getProcessDefinitionPage,
  deleteProcessDefinition,
  publishProcessDefinition,
  suspendProcessDefinition
} from '@/api/workflow'
import type { WfProcessDefinitionVO, WfProcessDefinitionQueryDTO } from '@/types/workflow'

const router = useRouter()

const loading = ref(false)
const dataSource = ref<WfProcessDefinitionVO[]>([])
const keyword = ref<string | undefined>(undefined)

const queryForm = reactive<WfProcessDefinitionQueryDTO>({
  processKey: undefined,
  processName: undefined,
  processCategory: undefined,
  status: undefined,
  isCurrentVersion: undefined,
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

const processCategoryOptions = [
  { label: '审批流程', value: 'APPROVAL' },
  { label: '会签流程', value: 'COUNTERSIGN' },
  { label: '公文流转', value: 'DOCUMENT' },
  { label: '请假流程', value: 'LEAVE' },
  { label: '报销流程', value: 'EXPENSE' }
]

const statusOptions = [
  { label: '草稿', value: 0, color: 'default' },
  { label: '已发布', value: 1, color: 'success' },
  { label: '已停用', value: 2, color: 'warning' }
]

const columns = [
  {
    title: '流程编码',
    dataIndex: 'processKey',
    key: 'processKey',
    width: 150
  },
  {
    title: '流程名称',
    dataIndex: 'processName',
    key: 'processName',
    width: 200
  },
  {
    title: '流程类型',
    dataIndex: 'processCategory',
    key: 'processCategory',
    width: 120
  },
  {
    title: '流程分类',
    dataIndex: 'processCategoryName',
    key: 'processCategoryName',
    width: 120
  },
  {
    title: '版本号',
    dataIndex: 'version',
    key: 'version',
    width: 100
  },
  {
    title: '是否当前版本',
    dataIndex: 'isCurrentVersion',
    key: 'isCurrentVersion',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 380,
    fixed: 'right'
  }
]

const getStatusLabel = (status: number) => {
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

const getStatusColor = (status: number) => {
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (keyword.value) {
      params.processName = keyword.value
    }
    const res = await getProcessDefinitionPage(params)
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
  queryForm.processKey = undefined
  queryForm.processName = undefined
  queryForm.processCategory = undefined
  queryForm.status = undefined
  queryForm.isCurrentVersion = undefined
  keyword.value = undefined
  handleSearch()
}

const handleTableChange = (pag: TablePaginationConfig) => {
  queryForm.pageNum = pag.current
  queryForm.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  router.push('/workflow/design')
}

const handleEdit = (id: number) => {
  router.push(`/workflow/design/${id}`)
}

const handleDesign = (id: number) => {
  router.push(`/workflow/design/${id}`)
}

const handlePreview = (id: number) => {
  router.push(`/workflow/design/${id}`)
}

const handlePublish = async (id: number) => {
  loading.value = true
  try {
    const res = await publishProcessDefinition(id)
    if (res.code === 200) {
      message.success('发布成功')
      fetchData()
    } else {
      message.error(res.message || '发布失败')
    }
  } catch (error) {
    message.error('发布失败')
  } finally {
    loading.value = false
  }
}

const handleDisable = async (id: number) => {
  loading.value = true
  try {
    const res = await suspendProcessDefinition(id)
    if (res.code === 200) {
      message.success('停用成功')
      fetchData()
    } else {
      message.error(res.message || '停用失败')
    }
  } catch (error) {
    message.error('停用失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  loading.value = true
  try {
    const res = await deleteProcessDefinition(id)
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
  fetchData()
})
</script>

<style scoped lang="less">
.workflow-list {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
