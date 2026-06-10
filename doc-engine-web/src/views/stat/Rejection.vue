<template>
  <div class="rejection-stat">
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
      <a-col :span="6">
        <a-card title="退回/驳回总数" :bordered="false" class="stat-card">
          <div class="stat-value">{{ overview.totalCount || 0 }}</div>
          <div class="stat-label">总次数</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card title="退回修改次数" :bordered="false" class="stat-card">
          <div class="stat-value warn">{{ overview.totalReturnCount || 0 }}</div>
          <div class="stat-label">退回修改</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card title="驳回次数" :bordered="false" class="stat-card">
          <div class="stat-value danger">{{ overview.totalRejectionCount || 0 }}</div>
          <div class="stat-label">驳回</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card title="退回率" :bordered="false" class="stat-card">
          <div class="stat-value primary">{{ overview.returnRate || 0 }}%</div>
          <div class="stat-label">占全部审批比例</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card title="退回原因词云" :bordered="false">
          <div ref="wordCloudChartRef" class="chart-container tall"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="退回原因占比（饼图）" :bordered="false">
          <div ref="pieChartRef" class="chart-container tall"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card title="退回原因排行（条形图）" :bordered="false">
          <div ref="barChartRef" class="chart-container tall"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="退回原因明细" :bordered="false">
          <a-table
            :columns="reasonColumns"
            :data-source="reasonData"
            :pagination="false"
            size="small"
            :scroll="{ y: 380 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'percentage'">
                <a-progress :percent="record.percentage" :size="'small'" />
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getStatRejectionOverview, getStatRejectionWords, getStatRejectionReasons } from '@/api/stat'
import { getUnitTree } from '@/api/org'
import type { StatQueryDTO, StatRejectionOverviewVO, StatRejectionWordVO, StatRejectionReasonVO } from '@/types/stat'
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

const overview = ref<StatRejectionOverviewVO>({
  totalRejectionCount: 0,
  totalReturnCount: 0,
  totalCount: 0,
  returnRate: 0
})
const wordData = ref<StatRejectionWordVO[]>([])
const reasonData = ref<StatRejectionReasonVO[]>([])

const wordCloudChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

let wordCloudChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

const reasonColumns = [
  { title: '序号', dataIndex: 'index', key: 'index', width: 60, customRender: ({ index }: any) => index + 1 },
  { title: '退回原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
  { title: '次数', dataIndex: 'count', key: 'count', width: 80, align: 'right' as const },
  { title: '占比', dataIndex: 'percentage', key: 'percentage', width: 160 }
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

const handleSearch = async () => {
  await loadAllData()
  renderAllCharts()
}

const handleReset = async () => {
  dateRange.value = []
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.unitCode = undefined
  queryForm.docType = undefined
  await loadAllData()
  renderAllCharts()
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

const loadOverview = async () => {
  try {
    const res = await getStatRejectionOverview(queryForm)
    if (res.code === 200 && res.data) {
      overview.value = res.data
    }
  } catch (e) {
    message.error('加载退回概览失败')
  }
}

const loadWords = async () => {
  try {
    const res = await getStatRejectionWords(queryForm)
    if (res.code === 200) {
      wordData.value = res.data || []
    }
  } catch (e) {
    message.error('加载词频统计失败')
  }
}

const loadReasons = async () => {
  try {
    const res = await getStatRejectionReasons(queryForm)
    if (res.code === 200) {
      reasonData.value = res.data || []
    }
  } catch (e) {
    message.error('加载退回原因失败')
  }
}

const loadAllData = async () => {
  await Promise.all([
    loadOverview(),
    loadWords(),
    loadReasons()
  ])
}

const initCharts = () => {
  if (wordCloudChartRef.value) {
    wordCloudChart = echarts.init(wordCloudChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
}

const WORD_COLORS = ['#1890ff', '#fa8c16', '#52c41a', '#722ed1', '#eb2f96', '#13c2c2', '#f5222d', '#faad14', '#2f54eb', '#a0d911']

const renderWordCloudChart = () => {
  if (!wordCloudChart) return
  const data = wordData.value.slice(0, 50)
  if (data.length === 0) {
    wordCloudChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 16 } }
    }, true)
    return
  }

  const maxCount = Math.max(...data.map(d => d.count))
  const minFontSize = 14
  const maxFontSize = 60

  const chartData = data.map((d, i) => ({
    name: d.word,
    value: d.count,
    textStyle: {
      fontSize: minFontSize + (d.count / maxCount) * (maxFontSize - minFontSize),
      color: WORD_COLORS[i % WORD_COLORS.length],
      fontWeight: 'bold' as const
    }
  }))

  wordCloudChart.setOption({
    tooltip: {
      formatter: (params: any) => `<b>${params.name}</b><br/>出现次数: ${params.value}`
    },
    series: [{
      type: 'graph',
      layout: 'none',
      roam: false,
      label: {
        show: true,
        formatter: '{b}'
      },
      data: chartData.map((item, idx) => {
        const angle = (idx / chartData.length) * Math.PI * 2
        const radius = 50 + (idx % 5) * 30
        return {
          ...item,
          x: 300 + Math.cos(angle) * radius + (Math.random() - 0.5) * 40,
          y: 220 + Math.sin(angle) * radius + (Math.random() - 0.5) * 40,
          symbolSize: 0,
          itemStyle: { color: 'transparent' },
          label: item.textStyle
        }
      })
    }]
  }, true)
}

const renderPieChart = () => {
  if (!pieChart) return
  const data = reasonData.value.slice(0, 10)
  if (data.length === 0) {
    pieChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 16 } }
    }, true)
    return
  }

  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 20,
      bottom: 20
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{b}\n{d}%'
      },
      data: data.map(d => ({
        name: d.reason,
        value: d.count
      }))
    }]
  }, true)
}

const renderBarChart = () => {
  if (!barChart) return
  const data = reasonData.value.slice(0, 15).reverse()
  if (data.length === 0) {
    barChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 16 } }
    }, true)
    return
  }

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        return `<b>${p.name}</b><br/>次数: ${p.value}`
      }
    },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '次数' },
    yAxis: {
      type: 'category',
      data: data.map(d => d.reason),
      axisLabel: { interval: 0 }
    },
    series: [{
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.count,
        itemStyle: { color: WORD_COLORS[i % WORD_COLORS.length] }
      })),
      barMaxWidth: 30,
      label: {
        show: true,
        position: 'right',
        formatter: '{c}'
      }
    }]
  }, true)
}

const renderAllCharts = () => {
  renderWordCloudChart()
  renderPieChart()
  renderBarChart()
}

const handleResize = () => {
  wordCloudChart?.resize()
  pieChart?.resize()
  barChart?.resize()
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
  wordCloudChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped lang="less">
.rejection-stat {
  padding: 16px;
}

.stat-card {
  text-align: center;

  .stat-value {
    font-size: 36px;
    font-weight: 700;
    color: #1890ff;
    line-height: 1.2;
    margin-bottom: 8px;

    &.warn { color: #fa8c16; }
    &.danger { color: #f5222d; }
    &.primary { color: #52c41a; }
  }

  .stat-label {
    font-size: 14px;
    color: #666;
  }
}

.chart-container {
  height: 320px;
  width: 100%;

  &.tall {
    height: 420px;
  }
}
</style>
