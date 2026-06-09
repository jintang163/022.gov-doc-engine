<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">收文登记</h2>
      <a-button type="primary" @click="handleRegister">
        <template #icon><PlusOutlined /></template>
        新增登记
      </a-button>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="搜索标题/编号/来文单位/来文字号"
            allow-clear
            @press-enter="handleSearch"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-col>
        <a-col :span="3">
          <a-select
            v-model:value="queryForm.source"
            placeholder="来源"
            :options="incomingSourceOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="3">
          <a-select
            v-model:value="queryForm.docType"
            placeholder="文种"
            :options="incomingDocTypeOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="3">
          <a-select
            v-model:value="queryForm.securityLevel"
            placeholder="密级"
            :options="incomingSecurityLevelOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="3">
          <a-select
            v-model:value="queryForm.urgencyLevel"
            placeholder="紧急程度"
            :options="incomingUrgencyLevelOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="3">
          <a-select
            v-model:value="queryForm.status"
            placeholder="状态"
            :options="incomingStatusOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="3">
          <a-range-picker
            v-model:value="dateRange"
            style="width: 100%"
            @change="handleDateRangeChange"
          />
        </a-col>
      </a-row>
      <a-row :gutter="16" style="margin-top: 12px">
        <a-col :span="24" style="text-align: right">
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card :bordered="false">
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
            <a-tooltip :title="record.docTitle">
              {{ record.docTitle }}
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'urgencyLevel'">
            <a-tag :color="getUrgencyColor(record.urgencyLevel)">
              {{ record.urgencyLevel }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleView(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button
                v-if="record.status === 'registered'"
                type="link"
                size="small"
                @click="handleDraftOpinion(record)"
              >
                <template #icon><FormOutlined /></template>
                拟办
              </a-button>
              <a-button
                v-if="record.status === 'proposed' || record.status === 'registered'"
                type="link"
                size="small"
                @click="handleAssign(record)"
              >
                <template #icon><SendOutlined /></template>
                分办
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
import { PlusOutlined, SearchOutlined, EyeOutlined, FormOutlined, SendOutlined } from '@ant-design/icons-vue'
import { getIncomingPage } from '@/api/incoming'
import type { DocIncomingVO, DocIncomingQueryDTO } from '@/types/incoming'
import { incomingStatusOptions, incomingSourceOptions, incomingDocTypeOptions, incomingSecurityLevelOptions, incomingUrgencyLevelOptions } from '@/types/incoming'
import type { Dayjs } from 'dayjs'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<DocIncomingVO[]>([])
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const queryForm = reactive({
  keyword: undefined as string | undefined,
  source: undefined as string | undefined,
  docType: undefined as string | undefined,
  securityLevel: undefined as string | undefined,
  urgencyLevel: undefined as string | undefined,
  status: undefined as string | undefined,
  receivedDateStart: undefined as string | undefined,
  receivedDateEnd: undefined as string | undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const getStatusLabel = (status: string) => {
  const item = incomingStatusOptions.find((s) => s.value === status)
  return item?.label || status
}

const getStatusColor = (status: string) => {
  const item = incomingStatusOptions.find((s) => s.value === status)
  return item?.color || 'default'
}

const getUrgencyColor = (level: string) => {
  const map: Record<string, string> = {
    '特急': 'red',
    '加急': 'orange',
    '普通': 'default'
  }
  return map[level] || 'default'
}

const columns = [
  { title: '收文编号', dataIndex: 'incomingNo', key: 'incomingNo', width: 150 },
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', width: 200, ellipsis: true },
  { title: '来文单位', dataIndex: 'sourceUnit', key: 'sourceUnit', width: 140 },
  { title: '来文字号', dataIndex: 'sourceDocNumber', key: 'sourceDocNumber', width: 140 },
  { title: '文种', dataIndex: 'docType', key: 'docType', width: 80 },
  { title: '密级', dataIndex: 'securityLevel', key: 'securityLevel', width: 80 },
  { title: '紧急程度', dataIndex: 'urgencyLevel', key: 'urgencyLevel', width: 100 },
  { title: '来源', dataIndex: 'sourceName', key: 'sourceName', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '收文日期', dataIndex: 'receivedDate', key: 'receivedDate', width: 120 },
  { title: '登记人', dataIndex: 'registrantName', key: 'registrantName', width: 90 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const fetchTableData = async () => {
  loading.value = true
  try {
    const params: DocIncomingQueryDTO = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    if (queryForm.keyword) params.keyword = queryForm.keyword
    if (queryForm.source) params.source = queryForm.source
    if (queryForm.docType) params.docType = queryForm.docType
    if (queryForm.securityLevel) params.securityLevel = queryForm.securityLevel
    if (queryForm.urgencyLevel) params.urgencyLevel = queryForm.urgencyLevel
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.receivedDateStart) params.receivedDateStart = queryForm.receivedDateStart
    if (queryForm.receivedDateEnd) params.receivedDateEnd = queryForm.receivedDateEnd
    const result = await getIncomingPage(params)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('加载收文列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchTableData()
}

const handleReset = () => {
  queryForm.keyword = undefined
  queryForm.source = undefined
  queryForm.docType = undefined
  queryForm.securityLevel = undefined
  queryForm.urgencyLevel = undefined
  queryForm.status = undefined
  queryForm.receivedDateStart = undefined
  queryForm.receivedDateEnd = undefined
  dateRange.value = null
  handleSearch()
}

const handleDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates.length === 2) {
    queryForm.receivedDateStart = dates[0].format('YYYY-MM-DD')
    queryForm.receivedDateEnd = dates[1].format('YYYY-MM-DD')
  } else {
    queryForm.receivedDateStart = undefined
    queryForm.receivedDateEnd = undefined
  }
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const handleRegister = () => {
  router.push('/incoming/register')
}

const handleView = (record: DocIncomingVO) => {
  router.push(`/incoming/${record.id}`)
}

const handleDraftOpinion = (record: DocIncomingVO) => {
  router.push(`/incoming/${record.id}`)
}

const handleAssign = (record: DocIncomingVO) => {
  router.push(`/incoming/${record.id}`)
}

onMounted(() => {
  fetchTableData()
})
</script>
