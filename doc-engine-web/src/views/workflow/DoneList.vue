<template>
  <div class="done-list-page">
    <div class="page-header">
      <h2 class="page-title">我的已办</h2>
    </div>

    <a-card :bordered="false" class="main-card">
      <a-form :model="queryForm" layout="inline" class="query-form">
        <a-form-item label="任务类型">
          <a-select
            v-model:value="queryForm.taskType"
            placeholder="请选择任务类型"
            allow-clear
            style="width: 160px"
          >
            <a-select-option
              v-for="item in taskTypeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审批结果">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择审批结果"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="COMPLETED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
            <a-select-option value="WITHDRAWN">已撤回</a-select-option>
            <a-select-option value="DELEGATED">已委派</a-select-option>
            <a-select-option value="TRANSFERRED">已转办</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="请输入任务名称/业务标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(record) => record.id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'approvalResult'">
            <a-tag :color="getApprovalResultColor(record.status)">
              {{ getApprovalResultLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'approvalTime'">
            {{ record.completeTime || '-' }}
          </template>
          <template v-else-if="column.key === 'duration'">
            {{ record.durationName || '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                查看详情
              </a-button>
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
import type { Dayjs } from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import type { TableProps } from 'ant-design-vue'
import {
  getDoneTaskPage
} from '@/api/workflow'
import type {
  WfTaskVO,
  WfTaskQueryDTO
} from '@/types/workflow'
import {
  taskTypeOptions,
  getTaskStatusColor
} from '@/types/workflow'

const router = useRouter()

const loading = ref(false)
const tableData = ref<WfTaskVO[]>([])
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const queryForm = reactive<WfTaskQueryDTO & { keyword?: string }>({
  taskType: undefined,
  status: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const columns: TableProps['columns'] = [
  {
    title: '任务编号',
    dataIndex: 'taskKey',
    key: 'taskKey',
    width: 140
  },
  {
    title: '任务名称',
    dataIndex: 'taskName',
    key: 'taskName',
    ellipsis: true
  },
  {
    title: '流程名称',
    dataIndex: 'processName',
    key: 'processName',
    ellipsis: true
  },
  {
    title: '业务标题',
    dataIndex: 'businessTitle',
    key: 'businessTitle',
    ellipsis: true
  },
  {
    title: '节点名称',
    dataIndex: 'nodeName',
    key: 'nodeName',
    ellipsis: true
  },
  {
    title: '审批结果',
    dataIndex: 'status',
    key: 'approvalResult',
    width: 120,
    align: 'center'
  },
  {
    title: '审批时间',
    dataIndex: 'completeTime',
    key: 'approvalTime',
    width: 180
  },
  {
    title: '耗时',
    dataIndex: 'durationName',
    key: 'duration',
    width: 120,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
    align: 'center'
  }
]

const getApprovalResultLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    COMPLETED: '已通过',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    WITHDRAWN: '已撤回',
    DELEGATED: '已委派',
    TRANSFERRED: '已转办',
    ADD_SIGNED: '已加签',
    CANCELED: '已取消'
  }
  return labelMap[status] || status
}

const getApprovalResultColor = (status: string) => {
  const colorMap: Record<string, string> = {
    COMPLETED: 'success',
    APPROVED: 'success',
    REJECTED: 'error',
    WITHDRAWN: 'warning',
    DELEGATED: 'orange',
    TRANSFERRED: 'purple',
    ADD_SIGNED: 'cyan',
    CANCELED: 'default'
  }
  return colorMap[status] || getTaskStatusColor(status)
}

const fetchTableData = async () => {
  loading.value = true
  try {
    const params: WfTaskQueryDTO = {
      ...queryForm,
      taskName: queryForm.keyword || queryForm.taskName,
      businessTitle: queryForm.keyword || queryForm.businessTitle,
      startTime: dateRange.value?.[0]?.format('YYYY-MM-DD') || undefined,
      endTime: dateRange.value?.[1]?.format('YYYY-MM-DD') || undefined,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    const res = await getDoneTaskPage(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    } else {
      message.error(res.message || '获取已办任务列表失败')
    }
  } catch (error) {
    console.error('获取已办任务列表失败:', error)
    message.error('获取已办任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.current = 1
  fetchTableData()
}

const handleReset = () => {
  queryForm.taskType = undefined
  queryForm.status = undefined
  queryForm.keyword = ''
  dateRange.value = null
  pagination.current = 1
  fetchTableData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const handleView = (record: WfTaskVO) => {
  router.push(`/workflow/task/${record.id}`)
}

onMounted(() => {
  fetchTableData()
})
</script>

<style lang="less" scoped>
.done-list-page {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;

  .page-header {
    margin-bottom: 24px;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #262626;
    }
  }

  .main-card {
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

    .query-form {
      margin-bottom: 16px;
      padding: 16px;
      background: #fafafa;
      border-radius: 4px;
    }
  }
}
</style>
