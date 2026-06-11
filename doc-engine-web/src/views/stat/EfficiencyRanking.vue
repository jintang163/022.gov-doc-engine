<template>
  <div>
    <a-card :bordered="false" class="page-header-card">
      <template #title>
        <span>📊 效能排行与报表</span>
      </template>
    </a-card>

    <a-card style="margin-top: 16px">
      <a-form :model="queryForm" layout="inline" @submit.prevent>
        <a-form-item label="统计月份">
          <a-month-picker v-model:value="queryForm.statMonth" value-format="YYYY-MM" placeholder="选择月份" allow-clear />
        </a-form-item>
        <a-form-item label="排行类型">
          <a-segmented v-model:value="queryForm.rankType" :options="rankTypeOptions" />
        </a-form-item>
        <a-form-item label="单位">
          <a-select v-model:value="queryForm.unitCode" placeholder="全部" allow-clear :options="unitOptions" style="width: 160px" />
        </a-form-item>
        <a-form-item v-if="queryForm.rankType === 'person'" label="部门">
          <a-select v-model:value="queryForm.deptId" placeholder="全部" allow-clear :options="deptOptions" style="width: 160px" />
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
            <a-button @click="handleRecalc">
              <template #icon><CalculatorOutlined /></template>
              重新计算
            </a-button>
            <a-button type="primary" @click="handleExport" :loading="exporting">
              <template #icon><DownloadOutlined /></template>
              导出Excel
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-statistic title="统计对象" :value="total" suffix="个" />
      </a-col>
      <a-col :span="6">
        <a-statistic title="平均办结率" :value="avgCompletionRate" suffix="%" :precision="2" />
      </a-col>
      <a-col :span="6">
        <a-statistic title="平均效率得分" :value="avgScore" :precision="2" />
      </a-col>
      <a-col :span="6">
        <a-statistic title="总超时" :value="totalOverdue" suffix="件" />
      </a-col>
    </a-row>

    <a-card style="margin-top: 16px">
      <a-tabs v-model:activeKey="queryForm.rankType" @change="(k: string) => { queryForm.rankType = k as 'dept' | 'person'; handleQuery() }">
        <a-tab-pane key="dept" tab="部门效能排名">
          <a-table
            :columns="deptColumns"
            :data-source="deptList"
            :pagination="paginationDept"
            :loading="loading"
            row-key="id"
            @change="(p: any) => { paginationDept.current = p.current; paginationDept.pageSize = p.pageSize; handleQuery() }"
            :scroll="{ x: 1100 }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rankNo'">
                <a-tag v-if="record.rankNo === 1" color="#f5222d" style="font-weight: 700; font-size: 14px">
                  🥇 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else-if="record.rankNo === 2" color="#fa8c16" style="font-weight: 600; font-size: 14px">
                  🥈 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else-if="record.rankNo === 3" color="#faad14" style="font-weight: 600; font-size: 14px">
                  🥉 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else color="default">第{{ record.rankNo }}名</a-tag>
              </template>
              <template v-else-if="column.key === 'rankLevel'">
                <a-tag :color="getLevelColor(record.rankLevel)">{{ record.rankLevel || '-' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'efficiencyScore'">
                <a-progress
                  type="dashboard"
                  :percent="Math.min(100, Math.round(record.efficiencyScore || 0))"
                  :width="80"
                  :stroke-color="getProgressColor(record.efficiencyScore || 0)"
                />
              </template>
              <template v-else-if="column.key === 'completionRate'">
                <a-progress
                  :percent="Math.round(record.completionRate || 0)"
                  :stroke-color="getProgressColor(record.completionRate || 0)"
                  size="small"
                />
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="person" tab="个人效能排名">
          <a-table
            :columns="personColumns"
            :data-source="personList"
            :pagination="paginationPerson"
            :loading="loading"
            row-key="id"
            @change="(p: any) => { paginationPerson.current = p.current; paginationPerson.pageSize = p.pageSize; handleQuery() }"
            :scroll="{ x: 1200 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'rankNo'">
                <a-tag v-if="record.rankNo === 1" color="#f5222d" style="font-weight: 700; font-size: 14px">
                  🥇 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else-if="record.rankNo === 2" color="#fa8c16" style="font-weight: 600; font-size: 14px">
                  🥈 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else-if="record.rankNo === 3" color="#faad14" style="font-weight: 600; font-size: 14px">
                  🥉 第{{ record.rankNo }}名
                </a-tag>
                <a-tag v-else color="default">第{{ record.rankNo }}名</a-tag>
              </template>
              <template v-else-if="column.key === 'rankLevel'">
                <a-tag :color="getLevelColor(record.rankLevel)">{{ record.rankLevel || '-' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'efficiencyScore'">
                <a-progress
                  type="dashboard"
                  :percent="Math.min(100, Math.round(record.efficiencyScore || 0))"
                  :width="80"
                  :stroke-color="getProgressColor(record.efficiencyScore || 0)"
                />
              </template>
              <template v-else-if="column.key === 'completionRate'">
                <a-progress
                  :percent="Math.round(record.completionRate || 0)"
                  :stroke-color="getProgressColor(record.completionRate || 0)"
                  size="small"
                />
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, CalculatorOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import type { StatEfficiencyVO } from '@/types/stat'
import {
  getDeptEfficiencyRank,
  getPersonEfficiencyRank,
  exportEfficiencyRank,
  calculateEfficiency
} from '@/api/stat'
import dayjs from 'dayjs'

interface QueryForm {
  statMonth: string | undefined
  rankType: 'dept' | 'person'
  unitCode: string | undefined
  deptId: string | undefined
}

const queryForm = reactive<QueryForm>({
  statMonth: dayjs().format('YYYY-MM'),
  rankType: 'dept',
  unitCode: undefined,
  deptId: undefined
})

const rankTypeOptions = [
  { label: '部门排名', value: 'dept' },
  { label: '个人排名', value: 'person' }
]

const unitOptions = [
  { label: '市政府办公厅', value: 'U001' },
  { label: '市发展改革委', value: 'U002' },
  { label: '市财政局', value: 'U003' },
  { label: '市公安局', value: 'U004' },
  { label: '市教育局', value: 'U005' }
]

const deptOptions = [
  { label: '综合办公室', value: 'D001' },
  { label: '秘书处', value: 'D002' },
  { label: '政策法规处', value: 'D003' },
  { label: '业务一处', value: 'D004' },
  { label: '业务二处', value: 'D005' },
  { label: '人事处', value: 'D006' },
  { label: '财务处', value: 'D007' }
]

const loading = ref(false)
const exporting = ref(false)

const deptList = ref<StatEfficiencyVO[]>([])
const personList = ref<StatEfficiencyVO[]>([])

const paginationDept = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const paginationPerson = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const deptColumns = [
  { title: '排名', dataIndex: 'rankNo', key: 'rankNo', width: 110, align: 'center' },
  { title: '等级', dataIndex: 'rankLevel', key: 'rankLevel', width: 90, align: 'center' },
  { title: '部门名称', dataIndex: 'targetName', key: 'targetName', width: 180, ellipsis: true },
  { title: '所属单位', dataIndex: 'unitName', key: 'unitName', width: 160, ellipsis: true },
  { title: '任务总数', dataIndex: 'totalTask', key: 'totalTask', width: 100, align: 'right' },
  { title: '已办结', dataIndex: 'completedTask', key: 'completedTask', width: 100, align: 'right' },
  { title: '超时件', dataIndex: 'overdueTask', key: 'overdueTask', width: 100, align: 'right' },
  { title: '办结率', dataIndex: 'completionRate', key: 'completionRate', width: 160 },
  { title: '平均时长', dataIndex: 'avgDurationText', key: 'avgDurationText', width: 120 },
  { title: '效率得分', dataIndex: 'efficiencyScore', key: 'efficiencyScore', width: 130, align: 'center' }
]

const personColumns = [
  { title: '排名', dataIndex: 'rankNo', key: 'rankNo', width: 110, align: 'center' },
  { title: '等级', dataIndex: 'rankLevel', key: 'rankLevel', width: 90, align: 'center' },
  { title: '姓名', dataIndex: 'targetName', key: 'targetName', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 160, ellipsis: true },
  { title: '所属单位', dataIndex: 'unitName', key: 'unitName', width: 160, ellipsis: true },
  { title: '任务总数', dataIndex: 'totalTask', key: 'totalTask', width: 100, align: 'right' },
  { title: '已办结', dataIndex: 'completedTask', key: 'completedTask', width: 100, align: 'right' },
  { title: '超时件', dataIndex: 'overdueTask', key: 'overdueTask', width: 100, align: 'right' },
  { title: '办结率', dataIndex: 'completionRate', key: 'completionRate', width: 160 },
  { title: '平均时长', dataIndex: 'avgDurationText', key: 'avgDurationText', width: 120 },
  { title: '效率得分', dataIndex: 'efficiencyScore', key: 'efficiencyScore', width: 130, align: 'center' }
]

const total = computed(() => (queryForm.rankType === 'dept' ? paginationDept.total : paginationPerson.total))

const currentList = computed(() => (queryForm.rankType === 'dept' ? deptList.value : personList.value))

const avgCompletionRate = computed(() => {
  const list = currentList.value
  if (!list.length) return 0
  const sum = list.reduce((s, r) => s + (r.completionRate || 0), 0)
  return Math.round((sum / list.length) * 100) / 100
})

const avgScore = computed(() => {
  const list = currentList.value
  if (!list.length) return 0
  const sum = list.reduce((s, r) => s + (r.efficiencyScore || 0), 0)
  return Math.round((sum / list.length) * 100) / 100
})

const totalOverdue = computed(() => {
  const list = currentList.value
  return list.reduce((s, r) => s + (r.overdueTask || 0), 0)
})

function getLevelColor(level: string) {
  if (level === '卓越') return '#f5222d'
  if (level === '优秀') return '#fa8c16'
  if (level === '良好') return '#1890ff'
  if (level === '达标') return '#52c41a'
  return 'default'
}

function getProgressColor(val: number) {
  if (val >= 90) return '#52c41a'
  if (val >= 70) return '#1890ff'
  if (val >= 50) return '#faad14'
  return '#ff4d4f'
}

async function handleQuery() {
  loading.value = true
  try {
    if (queryForm.rankType === 'dept') {
      const res = await getDeptEfficiencyRank({
        statMonth: queryForm.statMonth,
        unitCode: queryForm.unitCode,
        pageNum: paginationDept.current,
        pageSize: paginationDept.pageSize
      })
      deptList.value = res.data?.records || []
      paginationDept.total = res.data?.total || 0
    } else {
      const res = await getPersonEfficiencyRank({
        statMonth: queryForm.statMonth,
        unitCode: queryForm.unitCode,
        deptId: queryForm.deptId,
        pageNum: paginationPerson.current,
        pageSize: paginationPerson.pageSize
      })
      personList.value = res.data?.records || []
      paginationPerson.total = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.statMonth = dayjs().format('YYYY-MM')
  queryForm.rankType = 'dept'
  queryForm.unitCode = undefined
  queryForm.deptId = undefined
  paginationDept.current = 1
  paginationPerson.current = 1
  handleQuery()
}

async function handleRecalc() {
  if (!queryForm.statMonth) {
    message.warning('请先选择统计月份')
    return
  }
  try {
    const res = await calculateEfficiency({ statMonth: queryForm.statMonth })
    if (res.data) {
      message.success(queryForm.statMonth + ' 月效能数据已重新计算')
      handleQuery()
    } else {
      message.error('计算失败')
    }
  } catch (e: any) {
    message.error(e.message || '计算异常')
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const blob = await exportEfficiencyRank({
      statMonth: queryForm.statMonth,
      unitCode: queryForm.unitCode,
      deptId: queryForm.deptId,
      rankType: queryForm.rankType
    })
    const fileName = (queryForm.rankType === 'person' ? '个人' : '部门') + '效能排行_' + (queryForm.statMonth || '本月') + '.xlsx'
    const url = window.URL.createObjectURL(new Blob([blob as any]))
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error(e.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="less">
.page-header-card {
  .ant-card-head-title {
    font-size: 18px;
    font-weight: 600;
  }
}
</style>
