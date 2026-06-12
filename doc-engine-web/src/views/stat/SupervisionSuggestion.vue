<template>
  <div class="supervision-suggestion">
    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="关键字">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="文号/标题"
            allow-clear
            style="width: 200px"
            @press-enter="handleSearch"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="预警类型">
          <a-select
            v-model:value="queryForm.suggestionType"
            placeholder="全部"
            allow-clear
            style="width: 140px"
            :options="supervisionSuggestionTypeOptions"
          />
        </a-form-item>
        <a-form-item label="风险等级">
          <a-select
            v-model:value="queryForm.riskLevel"
            placeholder="全部"
            allow-clear
            style="width: 120px"
            :options="supervisionRiskLevelOptions"
          />
        </a-form-item>
        <a-form-item label="责任部门">
          <a-select
            v-model:value="queryForm.responsibleDeptId"
            placeholder="全部"
            allow-clear
            style="width: 160px"
            :options="deptOptions"
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
            <a-button @click="handleRefresh" :loading="refreshing">
              <template #icon><SyncOutlined /></template>
              刷新预警
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="预警总数" :value="overview.total || 0" :value-style="{ color: '#1890ff' }">
            <template #prefix><AlertOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="高风险" :value="overview.highRisk || 0" :value-style="{ color: '#ff4d4f' }">
            <template #prefix><WarningOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="待处理" :value="overview.pending || 0" :value-style="{ color: '#fa8c16' }">
            <template #prefix><ClockCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="今日新增" :value="overview.todayNew || 0" :value-style="{ color: '#52c41a' }" />
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false">
      <div style="margin-bottom: 16px">
        <a-space>
          <a-button
            type="primary"
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchSupervise"
          >
            <template #icon><BellOutlined /></template>
            批量督办
          </a-button>
          <a-button
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchIgnore"
          >
            <template #icon><StopOutlined /></template>
            批量忽略
          </a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        row-key="id"
        @change="handlePageChange"
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.key === 'suggestionType'">
            <a-tag :color="getSuggestionTypeColor(record.suggestionType)">
              {{ record.suggestionTypeName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'riskLevel'">
            <a-tag :color="getRiskColor(record.riskLevel)">
              {{ record.riskLevelName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'predictedOverdueDays'">
            <span :style="{ color: record.predictedOverdueDays > 0 ? '#ff4d4f' : record.predictedOverdueDays > -2 ? '#fa8c16' : '#52c41a' }">
              {{ record.predictedOverdueDays > 0 ? '已超时' + record.predictedOverdueDays + '天' : record.predictedOverdueDays === 0 ? '今日到期' : '剩余' + Math.abs(record.predictedOverdueDays) + '天' }}
            </span>
          </template>
          <template v-else-if="column.key === 'remainingDays'">
            <span :style="{ color: record.remainingDays <= 1 ? '#ff4d4f' : record.remainingDays <= 3 ? '#fa8c16' : 'inherit' }">
              {{ record.remainingDays }} 天
            </span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'suggestedContent'">
            <span v-if="record.suggestedContent" style="color: #595959">
              {{ record.suggestedContent }}
            </span>
            <span v-else style="color: #bfbfbf">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                type="link"
                size="small"
                @click="goToDoc(record.incomingId)"
              >
                查看公文
              </a-button>
              <a-button
                v-if="record.status === 'pending'"
                type="link"
                size="small"
                @click="handleSingleSupervise(record)"
              >
                督办
              </a-button>
              <a-button
                v-if="record.status === 'pending'"
                type="link"
                size="small"
                @click="handleSingleIgnore(record)"
              >
                忽略
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="superviseModalVisible"
      title="确认督办"
      @ok="confirmSupervise"
      @cancel="superviseModalVisible = false"
    >
      <p>确定对选中的 {{ currentSuperviseIds.length }} 条预警记录生成督办单吗？</p>
      <a-textarea
        v-model:value="superviseUrgeContent"
        :rows="4"
        placeholder="可输入督办说明（可选）"
        style="margin-top: 12px"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  SyncOutlined,
  AlertOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  BellOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import {
  getSupervisionSuggestionPage,
  refreshSupervisionSuggestions,
  handleSupervisionSuggestion,
  batchHandleSupervisionSuggestions
} from '@/api/stat'
import type {
  SupervisionSuggestionVO,
  SupervisionSuggestionQueryDTO
} from '@/types/stat'
import {
  supervisionSuggestionTypeOptions,
  supervisionRiskLevelOptions,
  supervisionSuggestionStatusOptions
} from '@/types/stat'

const router = useRouter()

const queryForm = reactive<SupervisionSuggestionQueryDTO>({
  keyword: undefined,
  suggestionType: undefined,
  riskLevel: undefined,
  responsibleDeptId: undefined,
  pageNum: 1,
  pageSize: 10
})

const deptOptions = [
  { label: '综合办公室', value: 'D001' },
  { label: '秘书处', value: 'D002' },
  { label: '政策法规处', value: 'D003' },
  { label: '业务一处', value: 'D004' },
  { label: '业务二处', value: 'D005' },
  { label: '财政审计科', value: '1002' },
  { label: '政策法规科', value: '1003' },
  { label: '信息技术科', value: '1005' }
]

const loading = ref(false)
const refreshing = ref(false)
const dataSource = ref<SupervisionSuggestionVO[]>([])
const selectedRowKeys = ref<number[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const overview = reactive({
  total: 0,
  highRisk: 0,
  pending: 0,
  todayNew: 0
})

const superviseModalVisible = ref(false)
const currentSuperviseIds = ref<number[]>([])
const superviseUrgeContent = ref('')

const columns = [
  { title: '序号', key: 'index', width: 70, align: 'center' as const },
  { title: '预警编号', dataIndex: 'suggestionNo', key: 'suggestionNo', width: 140 },
  { title: '预警类型', dataIndex: 'suggestionType', key: 'suggestionType', width: 110 },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel', width: 100 },
  {
    title: '公文信息',
    dataIndex: 'docTitle',
    key: 'docTitle',
    width: 240,
    ellipsis: true,
    customRender: ({ record }: { record: SupervisionSuggestionVO }) => ({
      children: record.docTitle,
      attrs: { title: `${record.sourceDocNumber}\n${record.docTitle}` }
    })
  },
  { title: '来文字号', dataIndex: 'sourceDocNumber', key: 'sourceDocNumber', width: 140 },
  { title: '当前节点', dataIndex: 'currentNodeName', key: 'currentNodeName', width: 100 },
  { title: '责任部门', dataIndex: 'responsibleDeptName', key: 'responsibleDeptName', width: 110 },
  { title: '预估超时', dataIndex: 'predictedOverdueDays', key: 'predictedOverdueDays', width: 110 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 90, align: 'center' as const },
  { title: '建议督办方式', dataIndex: 'suggestedUrgeTypeName', key: 'suggestedUrgeTypeName', width: 110 },
  { title: '督办建议', dataIndex: 'suggestedContent', key: 'suggestedContent', width: 180, ellipsis: true },
  { title: '已催办', dataIndex: 'urgeCount', key: 'urgeCount', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '生成时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

function getSuggestionTypeColor(type: string) {
  const opt = supervisionSuggestionTypeOptions.find(o => o.value === type)
  return opt?.color || 'default'
}

function getRiskColor(level: string) {
  const opt = supervisionRiskLevelOptions.find(o => o.value === level)
  return opt?.color || 'default'
}

function getStatusColor(status: string) {
  const opt = supervisionSuggestionStatusOptions.find(o => o.value === status)
  return opt?.color || 'default'
}

async function loadData() {
  loading.value = true
  try {
    queryForm.pageNum = pagination.current
    queryForm.pageSize = pagination.pageSize
    const res = await getSupervisionSuggestionPage(queryForm)
    if (res.code === 200 && res.data) {
      dataSource.value = res.data.records || []
      pagination.total = res.data.total || 0
      calcOverview(res.data.records || [])
    }
  } finally {
    loading.value = false
  }
}

function calcOverview(list: SupervisionSuggestionVO[]) {
  overview.total = pagination.total
  overview.highRisk = list.filter(r => r.riskLevel === 'high').length
  overview.pending = list.filter(r => r.status === 'pending').length
  const today = new Date().toISOString().slice(0, 10)
  overview.todayNew = list.filter(r => r.createTime && r.createTime.startsWith(today)).length
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  queryForm.keyword = undefined
  queryForm.suggestionType = undefined
  queryForm.riskLevel = undefined
  queryForm.responsibleDeptId = undefined
  pagination.current = 1
  loadData()
}

async function handleRefresh() {
  refreshing.value = true
  try {
    const res = await refreshSupervisionSuggestions()
    if (res.code === 200) {
      message.success(`刷新完成，共识别 ${res.data || 0} 条预警`)
      loadData()
    }
  } finally {
    refreshing.value = false
  }
}

function handlePageChange(p: any) {
  pagination.current = p.current
  pagination.pageSize = p.pageSize
  loadData()
}

function onSelectChange(keys: any[]) {
  selectedRowKeys.value = keys as number[]
}

function goToDoc(incomingId: number) {
  router.push(`/incoming/${incomingId}`)
}

function handleSingleSupervise(record: SupervisionSuggestionVO) {
  currentSuperviseIds.value = [record.id]
  superviseModalVisible.value = true
}

function handleBatchSupervise() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要督办的记录')
    return
  }
  currentSuperviseIds.value = [...selectedRowKeys.value]
  superviseModalVisible.value = true
}

async function confirmSupervise() {
  try {
    if (currentSuperviseIds.value.length === 1) {
      const res = await handleSupervisionSuggestion(
        currentSuperviseIds.value[0],
        'supervise',
        superviseUrgeContent.value || undefined
      )
      if (res.code === 200) {
        message.success('督办单已生成')
      }
    } else {
      const res = await batchHandleSupervisionSuggestions(currentSuperviseIds.value, 'supervise')
      if (res.code === 200) {
        message.success(`已批量督办 ${res.data || 0} 条记录`)
      }
    }
    superviseModalVisible.value = false
    superviseUrgeContent.value = ''
    currentSuperviseIds.value = []
    loadData()
  } catch (e: any) {
    message.error(e.message || '督办失败')
  }
}

async function handleSingleIgnore(record: SupervisionSuggestionVO) {
  try {
    const res = await handleSupervisionSuggestion(record.id, 'ignore')
    if (res.code === 200) {
      message.success('已忽略')
      loadData()
    }
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

async function handleBatchIgnore() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要忽略的记录')
    return
  }
  try {
    const res = await batchHandleSupervisionSuggestions(selectedRowKeys.value, 'ignore')
    if (res.code === 200) {
      message.success(`已批量忽略 ${res.data || 0} 条记录`)
      loadData()
    }
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less">
.supervision-suggestion {
  padding: 16px;

  .stat-card {
    text-align: center;
  }
}
</style>
