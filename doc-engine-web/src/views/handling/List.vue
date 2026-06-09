<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">承办办理</h2>
    </div>

    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部处理" />
        <a-tab-pane key="my" tab="我的承办" />
      </a-tabs>

      <a-form :model="queryForm" layout="inline" class="search-bar">
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="item in handlingStatusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理类型">
          <a-select
            v-model:value="queryForm.handlingType"
            placeholder="请选择处理类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="item in handlingTypeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'docTitle'">
            {{ record.incoming?.docTitle || '-' }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleViewDetail(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button
                v-if="record.handlingType === 'assign' && record.status === 'pending'"
                type="link"
                size="small"
                @click="handleOpenFeedback(record)"
              >
                <template #icon><CommentOutlined /></template>
                反馈
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="feedbackModalVisible"
      title="提交反馈"
      @ok="handleFeedbackSubmit"
      :confirm-loading="feedbackLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="反馈内容">
          <a-textarea
            v-model:value="feedbackForm.feedbackContent"
            :rows="4"
            placeholder="请输入反馈内容"
          />
        </a-form-item>
        <a-form-item label="反馈附件">
          <a-input
            v-model:value="feedbackForm.feedbackAttachment"
            placeholder="请输入附件信息"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, EyeOutlined, CommentOutlined } from '@ant-design/icons-vue'
import { getHandlingPage, getMyHandlings, submitFeedback } from '@/api/incoming'
import type { DocHandlingVO, DocHandlingFeedbackDTO, DocHandlingQueryDTO } from '@/types/incoming'
import { handlingStatusOptions, handlingTypeOptions } from '@/types/incoming'

const router = useRouter()

const loading = ref(false)
const activeTab = ref('all')
const dataSource = ref<DocHandlingVO[]>([])

const feedbackModalVisible = ref(false)
const feedbackLoading = ref(false)

const queryForm = reactive<DocHandlingQueryDTO>({
  status: undefined,
  handlingType: undefined,
  pageNum: 1,
  pageSize: 10
})

const feedbackForm = reactive<DocHandlingFeedbackDTO>({
  handlingId: 0,
  feedbackContent: undefined,
  feedbackAttachment: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const getStatusLabel = (status: string) => {
  const item = handlingStatusOptions.find((s) => s.value === status)
  return item?.label || status
}

const getStatusColor = (status: string) => {
  const item = handlingStatusOptions.find((s) => s.value === status)
  return item?.color || 'default'
}

const columns = [
  { title: '处理编号', dataIndex: 'handlingNo', key: 'handlingNo', width: 160 },
  { title: '公文标题', key: 'docTitle', ellipsis: true },
  { title: '处理类型', dataIndex: 'handlingTypeName', key: 'handlingTypeName', width: 100 },
  { title: '处理人', dataIndex: 'handlerName', key: 'handlerName', width: 100 },
  { title: '目标部门', dataIndex: 'targetDeptName', key: 'targetDeptName', width: 120 },
  { title: '目标人员', dataIndex: 'targetUserName', key: 'targetUserName', width: 100 },
  { title: '状态', key: 'status', width: 100, align: 'center' as const },
  { title: '处理时间', dataIndex: 'handlingTime', key: 'handlingTime', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
]

const fetchTableData = async () => {
  loading.value = true
  try {
    const params: DocHandlingQueryDTO = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    let result: any
    if (activeTab.value === 'my') {
      result = await getMyHandlings(params)
    } else {
      result = await getHandlingPage(params)
    }

    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('获取处理列表失败')
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
  queryForm.status = undefined
  queryForm.handlingType = undefined
  handleQuery()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const handleViewDetail = (record: DocHandlingVO) => {
  router.push(`/handling/detail/${record.id}`)
}

const handleOpenFeedback = (record: DocHandlingVO) => {
  feedbackForm.handlingId = record.id
  feedbackForm.feedbackContent = undefined
  feedbackForm.feedbackAttachment = undefined
  feedbackModalVisible.value = true
}

const handleFeedbackSubmit = async () => {
  feedbackLoading.value = true
  try {
    await submitFeedback(feedbackForm)
    message.success('反馈提交成功')
    feedbackModalVisible.value = false
    fetchTableData()
  } catch (error) {
    message.error('反馈提交失败')
  } finally {
    feedbackLoading.value = false
  }
}

onMounted(() => {
  fetchTableData()
})
</script>

<style scoped lang="less">
</style>
