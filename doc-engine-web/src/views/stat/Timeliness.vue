<template>
  <div class="timeliness-stat">
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
      <a-col :span="12">
        <a-card title="各部门平均拟稿时长" :bordered="false">
          <div ref="deptDraftChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="各部门拟稿时长明细" :bordered="false">
          <a-table
            :columns="deptDraftColumns"
            :data-source="deptDraftData"
            :pagination="false"
            size="small"
            :scroll="{ y: 320 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'avgDraftMinutes'">
                <span style="font-weight: 600; color: #1890ff">{{ record.avgDraftText }}</span>
              </template>
              <template v-if="column.key === 'range'">
                <span>{{ record.minDraftText }} ~ {{ record.maxDraftText }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card title="各岗位节点停留时长" :bordered="false">
          <div ref="nodeDwellChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="各岗位节点停留时长明细" :bordered="false">
          <a-table
            :columns="nodeDwellColumns"
            :data-source="nodeDwellData"
            :pagination="false"
            size="small"
            :scroll="{ y: 320 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'avgDwellMinutes'">
                <span style="font-weight: 600; color: #fa8c16">{{ record.avgDwellText }}</span>
              </template>
              <template v-if="column.key === 'withinRate'">
                <a-progress :percent="record.withinRate" :size="'small'" :status="record.withinRate >= 80 ? 'success' : record.withinRate >= 50 ? 'normal' : 'exception'" />
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card title="各公文类型会签完成周期" :bordered="false">
          <div ref="countersignCycleChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="各公文类型会签周期明细" :bordered="false">
          <a-table
            :columns="countersignCycleColumns"
            :data-source="countersignCycleData"
            :pagination="false"
            size="small"
            :scroll="{ y: 320 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'avgCycleMinutes'">
                <span style="font-weight: 600; color: #52c41a">{{ record.avgCycleText }}</span>
              </template>
              <template v-if="column.key === 'withinRate'">
                <a-progress :percent="record.withinRate" :size="'small'" :status="record.withinRate >= 80 ? 'success' : record.withinRate >= 50 ? 'normal' : 'exception'" />
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="24">
        <a-card title="办理时效趋势" :bordered="false">
          <div ref="trendChartRef" class="chart-container" style="height: 400px"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getStatDeptDraft, getStatNodeDwell, getStatCountersignCycle, getStatTimelinessTrend } from '@/api/stat'
import { getUnitTree } from '@/api/org'
import type { StatQueryDTO, StatDeptDraftVO, StatNodeDwellVO, StatCountersignCycleVO, StatTimelinessTrendVO } from '@/types/stat'
import type { SysUnitVO } from '@/types/org'

const dateRange = ref<string[]>([])
const queryForm = reactive<StatQueryDTO>({
  startDate: '',
  endDate: '',
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

const deptDraftData = ref<StatDeptDraftVO[]>([])
const nodeDwellData = ref<StatNodeDwellVO[]>([])
const countersignCycleData = ref<StatCountersignCycleVO[]>([])
const trendData = ref<StatTimelinessTrendVO[]>([])

const deptDraftChartRef = ref<HTMLElement>()
const nodeDwellChartRef = ref<HTMLElement>()
const countersignCycleChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

let deptDraftChart: echarts.ECharts | null = null
let nodeDwellChart: echarts.ECharts | null = null
let countersignCycleChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const deptDraftColumns = [
  { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
  { title: '公文数', dataIndex: 'docCount', key: 'docCount', width: 80, align: 'right' as const },
  { title: '平均拟稿时长', dataIndex: 'avgDraftMinutes', key: 'avgDraftMinutes', width: 120 },
  { title: '时长范围', key: 'range', width: 180 }
]

const nodeDwellColumns = [
  { title: '节点', dataIndex: 'nodeName', key: 'nodeName', width: 100 },
  { title: '岗位', dataIndex: 'postName', key: 'postName', width: 100 },
  { title: '任务数', dataIndex: 'taskCount', key: 'taskCount', width: 70, align: 'right' as const },
  { title: '平均停留', dataIndex: 'avgDwellMinutes', key: 'avgDwellMinutes', width: 110 },
  { title: '时效达标', dataIndex: 'withinRate', key: 'withinRate', width: 120 }
]

const countersignCycleColumns = [
  { title: '公文类型', dataIndex: 'docTypeName', key: 'docTypeName', width: 100 },
  { title: '会签次数', dataIndex: 'countersignCount', key: 'countersignCount', width: 80, align: 'right' as const },
  { title: '平均周期', dataIndex: 'avgCycleMinutes', key: 'avgCycleMinutes', width: 110 },
  { title: '周期范围', key: 'cycleRange', width: 180, customRender: ({ record }: any) => `${record.minCycleText} ~ ${record.maxCycleText}` },
  { title: '达标率', dataIndex: 'withinRate', key: 'withinRate', width: 120 }
]

function flattenUnits(units: SysUnitVO[], result: { label: string; value: string }[] = []) {
  for (const u of units) {
    result.push({ label: u.unitName, value: u.unitCode })
    if (u.children?.length) flattenUnits(u.children, result)
  }
  return result
}

const handleDateChange = (dates: string[]) => {
  if (dates && dates.length === 2) {
    queryForm.startDate = dates[0]
    queryForm.endDate = dates[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
}

const handleSearch = () => {
  loadAllData()
}

const handleReset = () => {
  dateRange.value = []
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.unitCode = undefined
  queryForm.docType = undefined
  loadAllData()
}

const loadUnits = async () => {
  try {
    const res = await getUnitTree()
    if (res.code === 200 && res.data) {
      unitOptions.value = flattenUnits(res.data)
    }
  } catch (e) {
    // ignore
  }
}

const loadDeptDraft = async () => {
  try {
    const res = await getStatDeptDraft(queryForm)
    if (res.code === 200) {
      deptDraftData.value = res.data || []
    }
  } catch (e) {
    message.error('加载部门拟稿统计失败')
  }
}

const loadNodeDwell = async () => {
  try {
    const res = await getStatNodeDwell(queryForm)
    if (res.code === 200) {
      nodeDwellData.value = res.data || []
    }
  } catch (e) {
    message.error('加载节点停留统计失败')
  }
}

const loadCountersignCycle = async () => {
  try {
    const res = await getStatCountersignCycle(queryForm)
    if (res.code === 200) {
      countersignCycleData.value = res.data || []
    }
  } catch (e) {
    message.error('加载会签周期统计失败')
  }
}

const loadTrend = async () => {
  try {
    const res = await getStatTimelinessTrend(queryForm)
    if (res.code === 200) {
      trendData.value = res.data || []
    }
  } catch (e) {
    message.error('加载时效趋势失败')
  }
}

const loadAllData = async () => {
  await Promise.all([
    loadDeptDraft(),
    loadNodeDwell(),
    loadCountersignCycle(),
    loadTrend()
  ])
}

const renderAllCharts = () => {
  renderDeptDraftChart()
  renderNodeDwellChart()
  renderCountersignCycleChart()
  renderTrendChart()
}

const initCharts = () => {
  if (deptDraftChartRef.value) {
    deptDraftChart = echarts.init(deptDraftChartRef.value)
  }
  if (nodeDwellChartRef.value) {
    nodeDwellChart = echarts.init(nodeDwellChartRef.value)
  }
  if (countersignCycleChartRef.value) {
    countersignCycleChart = echarts.init(countersignCycleChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
}

const renderDeptDraftChart = () => {
  if (!deptDraftChart) return
  const data = deptDraftData.value
  const names = data.map(d => d.deptName)
  const avgValues = data.map(d => d.avgDraftMinutes)
  const minValues = data.map(d => d.minDraftMinutes)
  const maxValues = data.map(d => d.maxDraftMinutes)

  deptDraftChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        let html = `<b>${params[0].name}</b><br/>`
        for (const p of params) {
          const hours = Math.floor(p.value / 60)
          const mins = p.value % 60
          html += `${p.marker} ${p.seriesName}: ${hours > 0 ? hours + '小时' : ''}${mins}分钟<br/>`
        }
        return html
      }
    },
    legend: { data: ['平均拟稿时长', '最短', '最长'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '分钟', axisLabel: { formatter: (v: number) => v >= 60 ? Math.floor(v / 60) + 'h' : v + 'm' } },
    series: [
      { name: '平均拟稿时长', type: 'bar', data: avgValues, itemStyle: { color: '#1890ff' }, barMaxWidth: 40 },
      { name: '最短', type: 'bar', data: minValues, itemStyle: { color: '#95de64' }, barMaxWidth: 40 },
      { name: '最长', type: 'bar', data: maxValues, itemStyle: { color: '#ff7875' }, barMaxWidth: 40 }
    ]
  }, true)
}

const renderNodeDwellChart = () => {
  if (!nodeDwellChart) return
  const data = nodeDwellData.value
  const labels = data.map(d => `${d.nodeName}\n(${d.postName})`)
  const avgValues = data.map(d => d.avgDwellMinutes)
  const withinRates = data.map(d => d.withinRate)

  nodeDwellChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        let html = `<b>${params[0].name.replace('\n', ' ')}</b><br/>`
        for (const p of params) {
          if (p.seriesName === '平均停留时长') {
            const hours = Math.floor(p.value / 60)
            const mins = p.value % 60
            html += `${p.marker} ${p.seriesName}: ${hours > 0 ? hours + '小时' : ''}${mins}分钟<br/>`
          } else {
            html += `${p.marker} ${p.seriesName}: ${p.value}%<br/>`
          }
        }
        return html
      }
    },
    legend: { data: ['平均停留时长', '时效达标率'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels, axisLabel: { rotate: 30, interval: 0, fontSize: 11 } },
    yAxis: [
      { type: 'value', name: '分钟', position: 'left', axisLabel: { formatter: (v: number) => v >= 60 ? Math.floor(v / 60) + 'h' : v + 'm' } },
      { type: 'value', name: '达标率%', position: 'right', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      { name: '平均停留时长', type: 'bar', data: avgValues, itemStyle: { color: '#fa8c16' }, barMaxWidth: 40 },
      { name: '时效达标率', type: 'line', yAxisIndex: 1, data: withinRates, itemStyle: { color: '#52c41a' }, lineStyle: { width: 2 }, symbol: 'circle', symbolSize: 6 }
    ]
  }, true)
}

const renderCountersignCycleChart = () => {
  if (!countersignCycleChart) return
  const data = countersignCycleData.value
  const names = data.map(d => d.docTypeName)
  const avgValues = data.map(d => d.avgCycleMinutes)
  const minValues = data.map(d => d.minCycleMinutes)
  const maxValues = data.map(d => d.maxCycleMinutes)

  countersignCycleChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        let html = `<b>${params[0].name}</b><br/>`
        for (const p of params) {
          const days = Math.floor(p.value / (60 * 24))
          const hours = Math.floor((p.value % (60 * 24)) / 60)
          html += `${p.marker} ${p.seriesName}: ${days > 0 ? days + '天' : ''}${hours > 0 ? hours + '小时' : ''}<br/>`
        }
        return html
      }
    },
    legend: { data: ['平均周期', '最短周期', '最长周期'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', name: '分钟', axisLabel: { formatter: (v: number) => { const d = Math.floor(v / (60 * 24)); return d > 0 ? d + '天' : Math.floor(v / 60) + 'h' } } },
    series: [
      { name: '平均周期', type: 'bar', data: avgValues, itemStyle: { color: '#52c41a' }, barMaxWidth: 50 },
      { name: '最短周期', type: 'bar', data: minValues, itemStyle: { color: '#95de64' }, barMaxWidth: 50 },
      { name: '最长周期', type: 'bar', data: maxValues, itemStyle: { color: '#ff7875' }, barMaxWidth: 50 }
    ]
  }, true)
}

const renderTrendChart = () => {
  if (!trendChart) return
  const data = trendData.value
  const dates = data.map(d => d.date)
  const draftLine = data.map(d => d.avgDraftMinutes)
  const dwellLine = data.map(d => d.avgDwellMinutes)
  const csLine = data.map(d => d.avgCountersignMinutes)
  const rateLine = data.map(d => d.completionRate)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let html = `<b>${params[0].name}</b><br/>`
        for (const p of params) {
          if (p.seriesName === '办结率') {
            html += `${p.marker} ${p.seriesName}: ${p.value}%<br/>`
          } else {
            const hours = Math.floor(p.value / 60)
            const mins = p.value % 60
            html += `${p.marker} ${p.seriesName}: ${hours > 0 ? hours + '小时' : ''}${mins}分钟<br/>`
          }
        }
        return html
      }
    },
    legend: { data: ['拟稿时长', '节点停留时长', '会签周期', '办结率'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '分钟', position: 'left', axisLabel: { formatter: (v: number) => v >= 60 ? Math.floor(v / 60) + 'h' : v + 'm' } },
      { type: 'value', name: '办结率%', position: 'right', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      { name: '拟稿时长', type: 'line', data: draftLine, smooth: true, itemStyle: { color: '#1890ff' }, areaStyle: { color: 'rgba(24,144,255,0.1)' } },
      { name: '节点停留时长', type: 'line', data: dwellLine, smooth: true, itemStyle: { color: '#fa8c16' }, areaStyle: { color: 'rgba(250,140,22,0.1)' } },
      { name: '会签周期', type: 'line', data: csLine, smooth: true, itemStyle: { color: '#52c41a' }, areaStyle: { color: 'rgba(82,196,26,0.1)' } },
      { name: '办结率', type: 'line', yAxisIndex: 1, data: rateLine, smooth: true, itemStyle: { color: '#722ed1' }, lineStyle: { type: 'dashed', width: 2 } }
    ]
  }, true)
}

watch(deptDraftData, () => renderDeptDraftChart())
watch(nodeDwellData, () => renderNodeDwellChart())
watch(countersignCycleData, () => renderCountersignCycleChart())
watch(trendData, () => renderTrendChart())

const handleResize = () => {
  deptDraftChart?.resize()
  nodeDwellChart?.resize()
  countersignCycleChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  await loadUnits()
  await loadAllData()
  await nextTick()
  initCharts()
  renderAllCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  deptDraftChart?.dispose()
  nodeDwellChart?.dispose()
  countersignCycleChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped lang="less">
.timeliness-stat {
  padding: 16px;
}

.chart-container {
  height: 320px;
  width: 100%;
}
</style>
