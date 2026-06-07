<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">签章日志</h2>
      <a-space>
        <a-button @click="handleExport" :loading="exporting">
          <template #icon><ExportOutlined /></template>
          导出日志
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="操作类型">
          <a-select
            v-model:value="searchForm.operationType"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option v-for="item in operationTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="印章名称">
          <a-input
            v-model:value="searchForm.sealName"
            placeholder="请输入印章名称"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item label="公文标题">
          <a-input
            v-model:value="searchForm.documentTitle"
            placeholder="请输入公文标题"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item label="操作人">
          <a-input
            v-model:value="searchForm.operator"
            placeholder="请输入操作人"
            allow-clear
            style="width: 150px;"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="全部"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            style="width: 280px;"
            value-format="YYYY-MM-DD"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="loading">
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
        :scroll="{ x: 1600 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationType'">
            <a-tag :color="getOperationTypeColor(record.operationType)">
              {{ record.operationTypeName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getLogStatusColor(record.status)">
              {{ getLogStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'errorMessage'">
            <span v-if="record.errorMessage" class="error-message" :title="record.errorMessage">
              {{ record.errorMessage }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
      <a-empty v-if="dataSource.length === 0" description="暂无签章日志" />
    </a-card>

    <a-modal
      v-model:open="detailModalVisible"
      title="日志详情"
      width="700px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered size="small" v-if="currentLog">
        <a-descriptions-item label="操作时间">{{ currentLog.operationTime }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOperationTypeColor(currentLog.operationType)">
            {{ currentLog.operationTypeName }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="印章名称">{{ currentLog.sealName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="公文标题">{{ currentLog.documentTitle || '-' }}</a-descriptions-item>
        <a-descriptions-item label="操作人">{{ currentLog.operator }}</a-descriptions-item>
        <a-descriptions-item label="操作人部门">{{ currentLog.operatorDept }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getLogStatusColor(currentLog.status)">
            {{ getLogStatusLabel(currentLog.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</a-descriptions-item>
        <a-descriptions-item label="错误信息" :span="2">
          <span v-if="currentLog.errorMessage" class="error-message">
            {{ currentLog.errorMessage }}
          </span>
          <span v-else class="text-muted">-</span>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ currentLog.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ExportOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getSignatureLogPage } from '@/api/signature'
import type {
  DocSignatureLogVO,
  DocSignatureLogQueryDTO
} from '@/types/signature'
import {
  operationTypeOptions,
  getLogStatusColor,
  getLogStatusLabel
} from '@/types/signature'

const loading = ref(false)
const exporting = ref(false)
const dataSource = ref<DocSignatureLogVO[]>([])
const detailModalVisible = ref(false)
const currentLog = ref<DocSignatureLogVO | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive<DocSignatureLogQueryDTO>({
  operationType: '',
  sealName: '',
  documentTitle: '',
  operator: '',
  status: undefined,
  startTime: '',
  endTime: '',
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

const columns = [
  { title: '操作时间', dataIndex: 'operationTime', key: 'operationTime', width: 180 },
  { title: '操作类型', dataIndex: 'operationType', key: 'operationType', width: 100 },
  { title: '印章名称', dataIndex: 'sealName', key: 'sealName', width: 150 },
  { title: '公文标题', dataIndex: 'documentTitle', key: 'documentTitle', width: 200, ellipsis: true },
  { title: '操作人', dataIndex: 'operator', key: 'operator', width: 100 },
  { title: '操作人部门', dataIndex: 'operatorDept', key: 'operatorDept', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 130 },
  { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage', width: 200, ellipsis: true },
  { title: '操作', key: 'action', width: 80, fixed: 'right' }
]

const getOperationTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    SIGN: 'blue',
    VERIFY: 'green',
    REVOKE: 'red',
    DOWNLOAD: 'purple',
    PREVIEW: 'cyan'
  }
  return colorMap[type] || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const result = await getSignatureLogPage(params)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('加载签章日志失败:', error)
    message.error('加载签章日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  searchForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.operationType = ''
  searchForm.sealName = ''
  searchForm.documentTitle = ''
  searchForm.operator = ''
  searchForm.status = undefined
  searchForm.startTime = ''
  searchForm.endTime = ''
  dateRange.value = null
  pagination.current = 1
  searchForm.pageNum = 1
  loadData()
}

const handlePageChange = (paginationInfo: { current: number; pageSize: number }) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  searchForm.pageNum = paginationInfo.current
  searchForm.pageSize = paginationInfo.pageSize
  loadData()
}

const handleViewDetail = (record: DocSignatureLogVO) => {
  currentLog.value = record
  detailModalVisible.value = true
}

const handleExport = () => {
  message.info('导出功能开发中')
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

.error-message {
  color: #ff4d4f;
}

.text-muted {
  color: #999;
}
</style>
