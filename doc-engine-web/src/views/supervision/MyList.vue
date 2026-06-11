<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">我的督办</h2>
      <a-space>
        <a-badge :count="pendingCount" :offset="[10, 2]" :number-style="{ backgroundColor: '#ff4d4f' }">
          <span style="color: #8c8c8c; font-size: 14px;">待处理</span>
        </a-badge>
      </a-space>
    </div>

    <a-card style="margin-bottom: 16px">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="文号/标题/督办单编号"
            allow-clear
            @press-enter="handleSearch"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.supervisionType"
            placeholder="督办类型"
            :options="supervisionTypeOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.status"
            placeholder="督办状态"
            :options="supervisionStatusOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="2">
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
        </a-col>
        <a-col :span="2">
          <a-button @click="handleReset">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handlePageChange"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'supervisionType'">
            <a-tag :color="getTypeColor(record.supervisionType)">
              {{ record.supervisionTypeName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'overdueDays'">
            <span :style="{ color: record.overdueDays > 0 ? '#ff4d4f' : '#52c41a' }">
              {{ record.overdueDays }} 天
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="goToDetail(record.id)">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getMySupervisions } from '@/api/supervision'
import type { DocSupervisionVO, DocSupervisionQueryDTO } from '@/types/supervision'
import {
  supervisionTypeOptions,
  supervisionStatusOptions
} from '@/types/supervision'

const router = useRouter()

const loading = ref(false)
const dataSource = ref<DocSupervisionVO[]>([])

const queryForm = reactive<DocSupervisionQueryDTO>({
  keyword: '',
  supervisionType: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const pendingCount = computed(() => {
  return dataSource.value.filter(item => item.status === 'notified' || item.status === 'processing').length
})

const columns = [
  { title: '督办单编号', dataIndex: 'supervisionNo', key: 'supervisionNo', width: 180 },
  { title: '来文字号', dataIndex: 'sourceDocNumber', key: 'sourceDocNumber', width: 180 },
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', ellipsis: true },
  { title: '督办类型', dataIndex: 'supervisionType', key: 'supervisionType', width: 120 },
  { title: '责任人', dataIndex: 'responsibleUserName', key: 'responsibleUserName', width: 100 },
  { title: '责任部门', dataIndex: 'responsibleDeptName', key: 'responsibleDeptName', width: 120 },
  { title: '超时天数', dataIndex: 'overdueDays', key: 'overdueDays', width: 100, align: 'center' },
  { title: '催办次数', dataIndex: 'urgeCount', key: 'urgeCount', width: 100, align: 'center' },
  { title: '督办状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '推送时间', dataIndex: 'pushTime', key: 'pushTime', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 120, fixed: 'right' }
]

const getTypeColor = (type: string) => {
  const option = supervisionTypeOptions.find(o => o.value === type)
  return option?.color || 'default'
}

const getStatusColor = (status: string) => {
  const option = supervisionStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMySupervisions(queryForm)
    dataSource.value = res.list
    pagination.total = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  } catch (error) {
    message.error('加载我的督办列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.supervisionType = undefined
  queryForm.status = undefined
  queryForm.pageNum = 1
  loadData()
}

const handlePageChange = (page: any) => {
  queryForm.pageNum = page.current
  queryForm.pageSize = page.pageSize
  loadData()
}

const goToDetail = (id: number) => {
  router.push(`/supervision/my/${id}`)
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
