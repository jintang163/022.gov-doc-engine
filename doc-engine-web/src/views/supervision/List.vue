<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">督办管理</h2>
      <a-space>
        <a-button @click="handleIdentifyTimeout">
          <template #icon><ClockCircleOutlined /></template>
          识别超时公文
        </a-button>
        <a-button @click="handleIdentifyUrgeOverdue">
          <template #icon><BellOutlined /></template>
          识别催办超标
        </a-button>
        <a-button type="primary" @click="batchGenerateModalVisible = true" :disabled="selectedRowKeys.length === 0">
          <template #icon><PlusOutlined /></template>
          批量生成督办单
        </a-button>
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
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.pushStatus"
            placeholder="推送状态"
            :options="pushStatusOptions"
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
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
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
          <template v-else-if="column.key === 'pushStatus'">
            <a-tag :color="getPushStatusColor(record.pushStatus)">
              {{ record.pushStatusName }}
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
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handlePush(record.id)"
                v-if="record.status === 'generated' || record.status === 'notified'"
              >
                推送
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleComplete(record.id)"
                v-if="record.status !== 'completed' && record.status !== 'closed'"
              >
                办结
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="batchGenerateModalVisible"
      title="批量生成督办单"
      :confirm-loading="batchGenerating"
      @ok="handleBatchGenerate"
      @cancel="batchGenerateModalVisible = false"
    >
      <p>确定为选中的 {{ pendingSupervisions.length }} 条记录生成督办单吗？</p>
      <a-table
        :columns="pendingColumns"
        :data-source="pendingSupervisions"
        :pagination="false"
        size="small"
        bordered
        row-key="incomingId"
        style="margin-top: 16px"
      />
    </a-modal>

    <a-modal
      v-model:open="identifyResultModalVisible"
      :title="identifyResultTitle"
      :footer="null"
      width="900px"
    >
      <a-alert
        v-if="identifyResults.length === 0"
        message="未发现符合条件的公文"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-table
        :columns="identifyColumns"
        :data-source="identifyResults"
        :pagination="false"
        size="small"
        bordered
        row-key="incomingId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'overdueDays'">
            <span style="color: #ff4d4f">{{ record.overdueDays }} 天</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleGenerateSingle(record)">
              生成督办单
            </a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  PlusOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
  BellOutlined
} from '@ant-design/icons-vue'
import {
  getSupervisionPage,
  identifyTimeoutDocs,
  identifyUrgeOverdueDocs,
  generateSupervision,
  batchGenerateSupervision,
  pushToLeader,
  completeSupervision
} from '@/api/supervision'
import type { DocSupervisionVO, DocSupervisionQueryDTO, DocSupervisionDTO } from '@/types/supervision'
import {
  supervisionTypeOptions,
  supervisionStatusOptions,
  pushStatusOptions
} from '@/types/supervision'

const router = useRouter()

const loading = ref(false)
const dataSource = ref<DocSupervisionVO[]>([])
const selectedRowKeys = ref<number[]>([])
const batchGenerateModalVisible = ref(false)
const batchGenerating = ref(false)
const identifyResultModalVisible = ref(false)
const identifyResultTitle = ref('')
const identifyResults = ref<DocSupervisionVO[]>([])
const pendingSupervisions = ref<DocSupervisionVO[]>([])

const queryForm = reactive<DocSupervisionQueryDTO>({
  keyword: '',
  supervisionType: undefined,
  status: undefined,
  pushStatus: undefined,
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
  { title: '推送状态', dataIndex: 'pushStatus', key: 'pushStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 180, fixed: 'right' }
]

const pendingColumns = [
  { title: '来文字号', dataIndex: 'sourceDocNumber', key: 'sourceDocNumber' },
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', ellipsis: true },
  { title: '督办类型', dataIndex: 'supervisionTypeName', key: 'supervisionTypeName' },
  { title: '责任人', dataIndex: 'responsibleUserName', key: 'responsibleUserName' },
  { title: '超时天数', dataIndex: 'overdueDays', key: 'overdueDays' }
]

const identifyColumns = [
  { title: '来文字号', dataIndex: 'sourceDocNumber', key: 'sourceDocNumber', width: 180 },
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', ellipsis: true },
  { title: '责任人', dataIndex: 'responsibleUserName', key: 'responsibleUserName', width: 100 },
  { title: '责任部门', dataIndex: 'responsibleDeptName', key: 'responsibleDeptName', width: 120 },
  { title: '超时天数', dataIndex: 'overdueDays', key: 'overdueDays', width: 100, align: 'center' },
  { title: '催办次数', dataIndex: 'urgeCount', key: 'urgeCount', width: 100, align: 'center' },
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

const getPushStatusColor = (status: string) => {
  const option = pushStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSupervisionPage(queryForm)
    dataSource.value = res.list
    pagination.total = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  } catch (error) {
    message.error('加载督办列表失败')
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
  queryForm.pushStatus = undefined
  queryForm.pageNum = 1
  loadData()
}

const handlePageChange = (page: any) => {
  queryForm.pageNum = page.current
  queryForm.pageSize = page.pageSize
  loadData()
}

const onSelectChange = (selectedKeys: number[]) => {
  selectedRowKeys.value = selectedKeys
}

const goToDetail = (id: number) => {
  router.push(`/supervision/${id}`)
}

const handleIdentifyTimeout = async () => {
  try {
    const res = await identifyTimeoutDocs()
    identifyResults.value = res
    identifyResultTitle.value = `超时公文识别结果（共 ${res.length} 条）`
    identifyResultModalVisible.value = true
  } catch (error) {
    message.error('识别超时公文失败')
  }
}

const handleIdentifyUrgeOverdue = async () => {
  try {
    const res = await identifyUrgeOverdueDocs()
    identifyResults.value = res
    identifyResultTitle.value = `催办超标公文识别结果（共 ${res.length} 条）`
    identifyResultModalVisible.value = true
  } catch (error) {
    message.error('识别催办超标公文失败')
  }
}

const handleGenerateSingle = async (record: DocSupervisionVO) => {
  Modal.confirm({
    title: '确认生成督办单',
    content: `确定为"${record.docTitle}"生成督办单吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const dto: DocSupervisionDTO = {
          incomingId: record.incomingId,
          handlingId: record.handlingId,
          supervisionType: record.supervisionType,
          responsibleUserId: record.responsibleUserId,
          responsibleUserName: record.responsibleUserName,
          responsibleDeptId: record.responsibleDeptId,
          responsibleDeptName: record.responsibleDeptName
        }
        await generateSupervision(dto)
        message.success('督办单生成成功')
        identifyResultModalVisible.value = false
        loadData()
      } catch (error) {
        message.error('督办单生成失败')
      }
    }
  })
}

const handleBatchGenerate = async () => {
  if (pendingSupervisions.value.length === 0) {
    message.warning('暂无待生成的督办数据')
    return
  }
  batchGenerating.value = true
  try {
    const dtoList: DocSupervisionDTO[] = pendingSupervisions.value.map(item => ({
      incomingId: item.incomingId,
      handlingId: item.handlingId,
      supervisionType: item.supervisionType,
      responsibleUserId: item.responsibleUserId,
      responsibleUserName: item.responsibleUserName,
      responsibleDeptId: item.responsibleDeptId,
      responsibleDeptName: item.responsibleDeptName
    }))
    await batchGenerateSupervision(dtoList)
    message.success('批量生成督办单成功')
    batchGenerateModalVisible.value = false
    loadData()
  } catch (error) {
    message.error('批量生成督办单失败')
  } finally {
    batchGenerating.value = false
  }
}

const handlePush = async (id: number) => {
  Modal.confirm({
    title: '确认推送',
    content: '确定将该督办单推送至分管领导吗？',
    okText: '确认推送',
    cancelText: '取消',
    onOk: async () => {
      try {
        await pushToLeader(id)
        message.success('推送成功')
        loadData()
      } catch (error) {
        message.error('推送失败')
      }
    }
  })
}

const handleComplete = async (id: number) => {
  Modal.confirm({
    title: '确认办结',
    content: '确定将该督办单标记为已完成吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await completeSupervision(id)
        message.success('督办已办结')
        loadData()
      } catch (error) {
        message.error('操作失败')
      }
    }
  })
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
