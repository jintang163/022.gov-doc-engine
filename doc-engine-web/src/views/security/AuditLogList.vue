<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作审计日志</h2>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="操作类型">
          <a-select
            v-model:value="searchForm.action"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option v-for="item in auditActionOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="资源类型">
          <a-select
            v-model:value="searchForm.resourceType"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option v-for="item in resourceTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="结果">
          <a-select
            v-model:value="searchForm.result"
            placeholder="全部"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option v-for="item in auditResultOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            style="width: 280px;"
            value-format="YYYY-MM-DD"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="keyword"
            placeholder="操作人/资源ID"
            allow-clear
            style="width: 180px;"
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
        :scroll="{ x: 1800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'securityLevel'">
            <a-tag :color="getSecurityLevelColor(record.securityLevel)">
              {{ record.securityLevel }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'result'">
            <a-tag :color="record.result === 'success' ? 'green' : 'red'">
              {{ record.resultName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'costTime'">
            {{ record.costTime }} ms
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getAuditLogPage } from '@/api/security'
import type { DocAuditLogVO, DocAuditLogQueryDTO } from '@/types/security'
import { auditActionOptions, resourceTypeOptions, auditResultOptions } from '@/types/security'

const loading = ref(false)
const dataSource = ref<DocAuditLogVO[]>([])
const dateRange = ref<[string, string] | null>(null)
const keyword = ref('')

const searchForm = reactive<DocAuditLogQueryDTO>({
  action: '',
  resourceType: '',
  result: '',
  userId: '',
  resourceId: '',
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
  { title: '日志编号', dataIndex: 'logNo', key: 'logNo', width: 160 },
  { title: '操作人', dataIndex: 'userName', key: 'userName', width: 100 },
  { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
  { title: '操作类型', dataIndex: 'actionName', key: 'actionName', width: 100 },
  { title: '资源类型', dataIndex: 'resourceTypeName', key: 'resourceTypeName', width: 100 },
  { title: '资源ID', dataIndex: 'resourceId', key: 'resourceId', width: 100 },
  { title: '密级', dataIndex: 'securityLevel', key: 'securityLevel', width: 80 },
  { title: '操作描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 130 },
  { title: '耗时', dataIndex: 'costTime', key: 'costTime', width: 100 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 80 },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

const getSecurityLevelColor = (level: string): string => {
  const colorMap: Record<string, string> = {
    '普通': 'green',
    '秘密': 'blue',
    '机密': 'orange',
    '绝密': 'red'
  }
  return colorMap[level] || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const result = await getAuditLogPage(params)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (keyword.value) {
    searchForm.userId = keyword.value
    searchForm.resourceId = keyword.value
  } else {
    searchForm.userId = ''
    searchForm.resourceId = ''
  }
  pagination.current = 1
  searchForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.action = ''
  searchForm.resourceType = ''
  searchForm.result = ''
  searchForm.userId = ''
  searchForm.resourceId = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  dateRange.value = null
  keyword.value = ''
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
