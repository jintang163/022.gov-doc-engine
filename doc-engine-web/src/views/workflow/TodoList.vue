<template>
  <div class="todo-list-page">
    <div class="page-header">
      <h2 class="page-title">我的待办</h2>
    </div>

    <a-row :gutter="16" class="stat-cards">
      <a-col :xs="24" :sm="12" :md="8">
        <a-card :bordered="false" class="stat-card todo">
          <div class="stat-content">
            <div class="stat-icon">
              <ClockCircleOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskCount.todoCount || 0 }}</div>
              <div class="stat-label">待办数量</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8">
        <a-card :bordered="false" class="stat-card pending">
          <div class="stat-content">
            <div class="stat-icon">
              <TeamOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskCount.pendingCount || 0 }}</div>
              <div class="stat-label">待签收</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8">
        <a-card :bordered="false" class="stat-card overdue">
          <div class="stat-content">
            <div class="stat-icon">
              <WarningOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskCount.overdueCount || 0 }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="main-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部待办" />
        <a-tab-pane key="my" tab="我的待办" />
        <a-tab-pane key="participated" tab="我参与的" />
      </a-tabs>

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
        <a-form-item label="优先级">
          <a-select
            v-model:value="queryForm.priority"
            placeholder="请选择优先级"
            allow-clear
            style="width: 120px"
          >
            <a-select-option
              v-for="item in priorityOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
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
          <template v-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ getPriorityLabel(record.priority) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getTaskStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'taskType'">
            <a-tag :color="getTaskTypeColor(record.taskType)">
              {{ record.taskTypeName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                type="link"
                size="small"
                @click="handleHandle(record)"
              >
                办理
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                查看
              </a-button>
              <a-button
                v-if="!record.assigneeId"
                type="link"
                size="small"
                @click="handleClaim(record)"
              >
                签收
              </a-button>
              <a-popconfirm
                v-if="record.assigneeId && isCurrentUser(record.assigneeId)"
                title="确定要取消签收吗？"
                @confirm="handleUnclaim(record)"
              >
                <a-button type="link" size="small" danger>
                  取消签收
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ClockCircleOutlined,
  TeamOutlined,
  WarningOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import type { TableProps } from 'ant-design-vue'
import {
  getTodoTaskPage,
  claimTask,
  unclaimTask,
  getTaskCount
} from '@/api/workflow'
import type {
  WfTaskVO,
  WfTaskQueryDTO,
  WfTaskCountVO
} from '@/types/workflow'
import {
  taskTypeOptions,
  priorityOptions,
  taskStatusOptions,
  getPriorityColor,
  getPriorityLabel,
  getTaskStatusColor,
  getTaskTypeColor
} from '@/types/workflow'

const router = useRouter()

const getCurrentUserId = (): string => {
  return localStorage.getItem('userId') || sessionStorage.getItem('userId') || ''
}

const currentUserId = getCurrentUserId()

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref<WfTaskVO[]>([])
const taskCount = ref<WfTaskCountVO>({
  todoCount: 0,
  doneCount: 0,
  myStartCount: 0,
  ccCount: 0,
  pendingCount: 0,
  overdueCount: 0,
  todayTodoCount: 0,
  todayDoneCount: 0
})

const queryForm = reactive<WfTaskQueryDTO & { keyword?: string }>({
  taskType: undefined,
  priority: undefined,
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
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100,
    align: 'center'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
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
    width: 200,
    fixed: 'right',
    align: 'center'
  }
]

const isCurrentUser = (userId: string) => {
  return userId === currentUserId
}

const fetchTaskCount = async () => {
  try {
    const res = await getTaskCount()
    if (res.code === 200) {
      if (typeof res.data === 'number') {
        taskCount.value.todoCount = res.data
      } else {
        taskCount.value = res.data
      }
    }
  } catch (error) {
    console.error('获取任务数量失败:', error)
  }
}

const calculateTaskStats = () => {
  let pendingCount = 0
  let overdueCount = 0
  tableData.value.forEach((task) => {
    if (!task.assigneeId) {
      pendingCount++
    }
    if (task.status === 'OVERDUE') {
      overdueCount++
    }
  })
  taskCount.value.pendingCount = pendingCount
  taskCount.value.overdueCount = overdueCount
}

const fetchTableData = async () => {
  loading.value = true
  try {
    const params: WfTaskQueryDTO = {
      ...queryForm,
      taskName: queryForm.keyword || queryForm.taskName,
      businessTitle: queryForm.keyword || queryForm.businessTitle,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    if (activeTab.value === 'my') {
      params.assigneeId = currentUserId
    } else if (activeTab.value === 'participated') {
      params.ownerId = currentUserId
    }

    const res = await getTodoTaskPage(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
      calculateTaskStats()
    } else {
      message.error(res.message || '获取待办任务列表失败')
    }
  } catch (error) {
    console.error('获取待办任务列表失败:', error)
    message.error('获取待办任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.current = 1
  fetchTableData()
}

const handleQuery = () => {
  pagination.current = 1
  fetchTableData()
}

const handleReset = () => {
  queryForm.taskType = undefined
  queryForm.priority = undefined
  queryForm.keyword = ''
  pagination.current = 1
  fetchTableData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const handleHandle = (record: WfTaskVO) => {
  router.push(`/workflow/task/${record.id}`)
}

const handleView = (record: WfTaskVO) => {
  router.push(`/workflow/task/${record.id}`)
}

const handleClaim = async (record: WfTaskVO) => {
  try {
    const res = await claimTask(record.id.toString())
    if (res.code === 200) {
      message.success('签收成功')
      fetchTableData()
      fetchTaskCount()
    } else {
      message.error(res.message || '签收失败')
    }
  } catch (error) {
    console.error('签收失败:', error)
    message.error('签收失败')
  }
}

const handleUnclaim = async (record: WfTaskVO) => {
  try {
    const res = await unclaimTask(record.id.toString())
    if (res.code === 200) {
      message.success('取消签收成功')
      fetchTableData()
      fetchTaskCount()
    } else {
      message.error(res.message || '取消签收失败')
    }
  } catch (error) {
    console.error('取消签收失败:', error)
    message.error('取消签收失败')
  }
}

onMounted(() => {
  fetchTaskCount()
  fetchTableData()
})
</script>

<style lang="less" scoped>
.todo-list-page {
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

  .stat-cards {
    margin-bottom: 16px;

    .stat-card {
      border-radius: 8px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #fff;
        }

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: 600;
            color: #262626;
            line-height: 1.2;
          }

          .stat-label {
            font-size: 14px;
            color: #8c8c8c;
            margin-top: 4px;
          }
        }
      }

      &.todo .stat-icon {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      }

      &.pending .stat-icon {
        background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      }

      &.overdue .stat-icon {
        background: linear-gradient(135deg, #f5222d 0%, #a8071a 100%);
      }
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
