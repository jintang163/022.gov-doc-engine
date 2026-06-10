<template>
  <div class="stat-dashboard">
    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="日期范围">
          <a-range-picker
            v-model:value="dateRange"
            :placeholder="['开始日期', '结束日期']"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </a-form-item>
        <a-form-item label="单位">
          <a-select
            v-model:value="queryForm.unitCode"
            placeholder="请选择单位"
            allow-clear
            style="width: 200px"
            :options="unitOptions"
          />
        </a-form-item>
        <a-form-item label="公文类型">
          <a-select
            v-model:value="queryForm.docType"
            placeholder="请选择公文类型"
            allow-clear
            style="width: 160px"
            :options="docTypeOptions"
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
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="公文总数" :value="overview.totalDocCount" value-style="color: #1890ff">
            <template #prefix><FileTextOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="草稿" :value="overview.draftCount" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="审核中" :value="overview.reviewingCount" value-style="color: #1890ff">
            <template #prefix><ClockCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="已签发" :value="overview.signedCount" value-style="color: #52c41a">
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="已归档" :value="overview.archivedCount" value-style="color: #52c41a" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="已废止" :value="overview.abolishedCount" value-style="color: #ff4d4f">
            <template #prefix><CloseCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="8">
        <a-card class="stat-card" :bordered="false" title="办结率">
          <div style="text-align: center">
            <a-progress type="circle" :percent="overview.completionRate" :width="120" />
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="stat-card" :bordered="false" title="平均处理时长">
          <a-statistic :value="overview.avgDurationText" value-style="font-size: 28px" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="stat-card" :bordered="false" title="近期办文量">
          <a-row>
            <a-col :span="8">
              <a-statistic title="今日" :value="overview.todayCount" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="本周" :value="overview.weekCount" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="本月" :value="overview.monthCount" />
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card class="chart-card" :bordered="false" title="公文类型分布">
          <div ref="docTypeChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="chart-card" :bordered="false" title="公文状态分布">
          <div ref="docStatusChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="16">
        <a-card class="chart-card" :bordered="false" title="公文趋势">
          <div ref="trendChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="chart-card" :bordered="false" title="流程效率">
          <a-table
            :columns="processColumns"
            :data-source="processData"
            :pagination="false"
            size="small"
            row-key="processName"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :span="16">
        <a-card class="chart-card" :bordered="false" title="各单位统计">
          <div ref="unitChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="chart-card" :bordered="false" title="办结率排行">
          <a-list :data-source="unitRankData" size="small">
            <template #renderItem="{ item, index }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <span :style="{ fontWeight: index < 3 ? 'bold' : 'normal' }">{{ item.unitName }}</span>
                  </template>
                  <template #description>
                    办结率: {{ item.completionRate }}%
                  </template>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: index < 3 ? '#1890ff' : '#d9d9d9' }" :size="28">
                      {{ index + 1 }}
                    </a-avatar>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import { getStatOverview, getStatDocType, getStatDocStatus, getStatProcess, getStatTrend, getStatUnit } from '@/api/stat'
import type { StatOverviewVO, StatDocTypeVO, StatDocStatusVO, StatProcessVO, StatTrendVO, StatUnitVO, StatQueryDTO } from '@/types/stat'
import { getUnitTree } from '@/api/org'
import type { SysUnitVO } from '@/types/org'
import type { Dayjs } from 'dayjs'

const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const queryForm = reactive<StatQueryDTO>({
  startDate: undefined,
  endDate: undefined,
  unitCode: undefined,
  docType: undefined
})

const unitOptions = ref<{ label: string; value: string }[]>([])

const docTypeOptions = [
  { label: '上行文', value: '上行文' },
  { label: '下行文', value: '下行文' },
  { label: '平行文', value: '平行文' },
  { label: '内部文', value: '内部文' }
]

const overview = reactive<StatOverviewVO>({
  totalDocCount: 0,
  draftCount: 0,
  reviewingCount: 0,
  signedCount: 0,
  archivedCount: 0,
  abolishedCount: 0,
  completionRate: 0,
  avgDurationMinutes: 0,
  avgDurationText: '-',
  todayCount: 0,
  weekCount: 0,
  monthCount: 0
})

const docTypeData = ref<StatDocTypeVO[]>([])
const docStatusData = ref<StatDocStatusVO[]>([])
const processData = ref<StatProcessVO[]>([])
const trendData = ref<StatTrendVO[]>([])
const unitData = ref<StatUnitVO[]>([])

const processColumns = [
  { title: '流程', dataIndex: 'processName', key: 'processName', ellipsis: true },
  { title: '总数', dataIndex: 'totalCount', key: 'totalCount', width: 60, align: 'center' as const },
  { title: '办结率', dataIndex: 'completionRate', key: 'completionRate', width: 80, align: 'center' as const,
    customRender: ({ text }: { text: number }) => `${text}%`
  },
  { title: '平均时长', dataIndex: 'avgDurationText', key: 'avgDurationText', width: 90, align: 'center' as const }
]

const unitRankData = ref<StatUnitVO[]>([])

const docTypeChartRef = ref<HTMLElement>()
const docStatusChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const unitChartRef = ref<HTMLElement>()

let docTypeChart: echarts.ECharts | null = null
let docStatusChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let unitChart: echarts.ECharts | null = null

const flattenUnits = (units: SysUnitVO[]): { label: string; value: string }[] => {
  const result: { label: string; value: string }[] = []
  const walk = (list: SysUnitVO[]) => {
    for (const u of list) {
      result.push({ label: u.unitName, value: u.unitCode })
      if (u.children?.length) walk(u.children)
    }
  }
  walk(units)
  return result
}

const loadUnitTree = async () => {
  try {
    const res = await getUnitTree()
    if (res.code === 200) {
      unitOptions.value = flattenUnits(res.data || [])
    } else {
      message.error(res.message || '加载单位树失败')
    }
  } catch {
    message.error('加载单位树失败')
  }
}

const loadOverview = async () => {
  try {
    const res = await getStatOverview(queryForm)
    if (res.code === 200) {
      Object.assign(overview, res.data)
    } else {
      message.error(res.message || '加载概览数据失败')
    }
  } catch {
    message.error('加载概览数据失败')
  }
}

const loadDocTypeData = async () => {
  try {
    const res = await getStatDocType(queryForm)
    if (res.code === 200) {
      docTypeData.value = res.data || []
    } else {
      message.error(res.message || '加载公文类型数据失败')
    }
  } catch {
    message.error('加载公文类型数据失败')
  }
}

const loadDocStatusData = async () => {
  try {
    const res = await getStatDocStatus(queryForm)
    if (res.code === 200) {
      docStatusData.value = res.data || []
    } else {
      message.error(res.message || '加载公文状态数据失败')
    }
  } catch {
    message.error('加载公文状态数据失败')
  }
}

const loadProcessData = async () => {
  try {
    const res = await getStatProcess(queryForm)
    if (res.code === 200) {
      processData.value = res.data || []
    } else {
      message.error(res.message || '加载流程效率数据失败')
    }
  } catch {
    message.error('加载流程效率数据失败')
  }
}

const loadTrendData = async () => {
  try {
    const res = await getStatTrend(queryForm)
    if (res.code === 200) {
      trendData.value = res.data || []
    } else {
      message.error(res.message || '加载趋势数据失败')
    }
  } catch {
    message.error('加载趋势数据失败')
  }
}

const loadUnitData = async () => {
  try {
    const res = await getStatUnit(queryForm)
    if (res.code === 200) {
      unitData.value = res.data || []
      unitRankData.value = [...(res.data || [])].sort((a, b) => b.completionRate - a.completionRate).slice(0, 10)
    } else {
      message.error(res.message || '加载单位统计数据失败')
    }
  } catch {
    message.error('加载单位统计数据失败')
  }
}

const initDocTypeChart = () => {
  if (!docTypeChartRef.value) return
  docTypeChart = echarts.init(docTypeChartRef.value)
  updateDocTypeChart()
}

const updateDocTypeChart = () => {
  if (!docTypeChart) return
  docTypeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['40%', '50%'],
      roseType: 'area',
      data: docTypeData.value.map(item => ({ name: item.docTypeName, value: item.count })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

const initDocStatusChart = () => {
  if (!docStatusChartRef.value) return
  docStatusChart = echarts.init(docStatusChartRef.value)
  updateDocStatusChart()
}

const updateDocStatusChart = () => {
  if (!docStatusChart) return
  const colorMap: Record<string, string> = {
    draft: '#d9d9d9',
    reviewing: '#1890ff',
    signed: '#52c41a',
    archived: '#87d068',
    abolished: '#ff4d4f'
  }
  docStatusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    color: ['#1890ff', '#52c41a', '#faad14', '#ff4d4f', '#d9d9d9', '#722ed1'],
    series: [{
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['40%', '50%'],
      roseType: 'area',
      data: docStatusData.value.map(item => ({
        name: item.statusName,
        value: item.count,
        itemStyle: { color: colorMap[item.status] }
      })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChart) return
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发文数', '办结数'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: trendData.value.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '发文数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        data: trendData.value.map(item => item.docCount)
      },
      {
        name: '办结数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        data: trendData.value.map(item => item.completedCount)
      }
    ]
  })
}

const initUnitChart = () => {
  if (!unitChartRef.value) return
  unitChart = echarts.init(unitChartRef.value)
  updateUnitChart()
}

const updateUnitChart = () => {
  if (!unitChart) return
  const sorted = [...unitData.value].sort((a, b) => a.docCount - b.docCount)
  unitChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map(item => item.unitName) },
    series: [{
      type: 'bar',
      data: sorted.map(item => ({
        value: item.docCount,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: '#36cfc9' }
          ])
        }
      })),
      barMaxWidth: 24,
      label: { show: true, position: 'right', formatter: '{c}' }
    }]
  })
}

const handleDateChange = (dates: [string, string] | null) => {
  if (dates) {
    queryForm.startDate = dates[0]
    queryForm.endDate = dates[1]
  } else {
    queryForm.startDate = undefined
    queryForm.endDate = undefined
  }
}

const handleSearch = () => {
  loadOverview()
  loadDocTypeData()
  loadDocStatusData()
  loadProcessData()
  loadTrendData()
  loadUnitData()
}

const handleReset = () => {
  dateRange.value = null
  queryForm.startDate = undefined
  queryForm.endDate = undefined
  queryForm.unitCode = undefined
  queryForm.docType = undefined
  handleSearch()
}

watch(docTypeData, () => updateDocTypeChart())
watch(docStatusData, () => updateDocStatusChart())
watch(trendData, () => updateTrendChart())
watch(unitData, () => updateUnitChart())

const handleResize = () => {
  docTypeChart?.resize()
  docStatusChart?.resize()
  trendChart?.resize()
  unitChart?.resize()
}

onMounted(async () => {
  await loadUnitTree()
  await Promise.all([
    loadOverview(),
    loadDocTypeData(),
    loadDocStatusData(),
    loadProcessData(),
    loadTrendData(),
    loadUnitData()
  ])
  await nextTick()
  initDocTypeChart()
  initDocStatusChart()
  initTrendChart()
  initUnitChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  docTypeChart?.dispose()
  docStatusChart?.dispose()
  trendChart?.dispose()
  unitChart?.dispose()
  docTypeChart = null
  docStatusChart = null
  trendChart = null
  unitChart = null
})
</script>

<style scoped lang="less">
.stat-dashboard {
  padding: 16px;

  .stat-card {
    text-align: center;
    margin-bottom: 16px;
  }

  .chart-card {
    margin-bottom: 16px;
  }

  .chart-container {
    height: 320px;
  }

  .overview-stat {
    :deep(.ant-statistic-title) {
      font-size: 14px;
    }

    :deep(.ant-statistic-content) {
      font-size: 28px;
    }
  }
}
</style>
