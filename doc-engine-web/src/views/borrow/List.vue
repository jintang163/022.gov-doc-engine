<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">借阅管理</h2>
    </div>

    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部借阅" />
        <a-tab-pane key="my" tab="我的借阅" />
        <a-tab-pane key="pending" tab="待审批" />
      </a-tabs>

      <a-form :model="queryForm" layout="inline" class="search-bar">
        <a-form-item label="关键词">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="借阅单号/公文标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="item in borrowStatusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="借阅方式">
          <a-select
            v-model:value="queryForm.borrowType"
            placeholder="请选择借阅方式"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="item in borrowTypeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

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
            {{ record.archive?.docTitle || '-' }}
          </template>
          <template v-else-if="column.key === 'borrowDate'">
            {{ record.startDate }} ~ {{ record.endDate }}
          </template>
          <template v-else-if="column.key === 'borrowReason'">
            {{ record.borrowReason?.length > 20 ? record.borrowReason.substring(0, 20) + '...' : record.borrowReason }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleViewDetail(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button
                v-if="record.status === 'pending'"
                type="link"
                size="small"
                @click="handleOpenApprove(record)"
              >
                <template #icon><CheckOutlined /></template>
                审批
              </a-button>
              <a-button
                v-if="record.status === 'active'"
                type="link"
                size="small"
                @click="handleReturn(record)"
              >
                <template #icon><UndoOutlined /></template>
                归还
              </a-button>
              <a-button
                v-if="record.status === 'active' || record.status === 'approved'"
                type="link"
                size="small"
                @click="handleViewContent(record)"
              >
                <template #icon><EyeOutlined /></template>
                查看内容
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="approveModalVisible"
      title="借阅审批"
      @ok="handleApproveSubmit"
      :confirm-loading="approveLoading"
    >
      <a-form-item label="审批结果">
        <a-radio-group v-model:value="approveForm.approveResult">
          <a-radio value="approved">批准</a-radio>
          <a-radio value="rejected">驳回</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="审批意见">
        <a-textarea
          v-model:value="approveForm.approveOpinion"
          :rows="4"
          placeholder="请输入审批意见"
        />
      </a-form-item>
    </a-modal>

    <a-modal
      v-model:open="contentModalVisible"
      title="借阅内容"
      width="800px"
      :footer="null"
    >
      <div class="watermarked-content" v-html="watermarkedContent" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  CheckOutlined,
  CloseOutlined,
  UndoOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import {
  getBorrowPage,
  getMyBorrows,
  getPendingApprovals,
  approveBorrow,
  returnBorrow,
  getWatermarkedContent
} from '@/api/archive'
import type { DocBorrowVO, DocBorrowApproveDTO, DocBorrowQueryDTO } from '@/types/archive'
import { borrowStatusOptions, borrowTypeOptions } from '@/types/archive'

const router = useRouter()

const loading = ref(false)
const activeTab = ref('all')
const dataSource = ref<DocBorrowVO[]>([])

const approveModalVisible = ref(false)
const approveLoading = ref(false)
const currentBorrowRecord = ref<DocBorrowVO | null>(null)

const contentModalVisible = ref(false)
const watermarkedContent = ref('')

const queryForm = reactive<DocBorrowQueryDTO>({
  keyword: undefined,
  status: undefined,
  borrowType: undefined,
  pageNum: 1,
  pageSize: 10
})

const approveForm = reactive<DocBorrowApproveDTO>({
  borrowId: 0,
  approveResult: 'approved',
  approveOpinion: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const getStatusLabel = (status: string) => {
  const item = borrowStatusOptions.find((s) => s.value === status)
  return item?.label || status
}

const getStatusColor = (status: string) => {
  const item = borrowStatusOptions.find((s) => s.value === status)
  return item?.color || 'default'
}

const columns = [
  { title: '借阅单号', dataIndex: 'borrowNo', key: 'borrowNo', width: 160 },
  { title: '公文标题', key: 'docTitle', ellipsis: true },
  { title: '借阅方式', dataIndex: 'borrowTypeName', key: 'borrowTypeName', width: 100 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 100 },
  { title: '申请部门', dataIndex: 'applicantDeptName', key: 'applicantDeptName', width: 120 },
  { title: '借阅事由', key: 'borrowReason', width: 180, ellipsis: true },
  { title: '借阅日期', key: 'borrowDate', width: 200 },
  { title: '状态', key: 'status', width: 100, align: 'center' as const },
  { title: '查看次数', dataIndex: 'viewCount', key: 'viewCount', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 260, fixed: 'right' as const }
]

const fetchTableData = async () => {
  loading.value = true
  try {
    const params: DocBorrowQueryDTO = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    let result: any
    if (activeTab.value === 'my') {
      result = await getMyBorrows(params)
    } else if (activeTab.value === 'pending') {
      result = await getPendingApprovals(params)
    } else {
      result = await getBorrowPage(params)
    }

    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('获取借阅列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.current = 1
  fetchTableData()
}

const handleQuery = () => {
  pagination.current = 1
  fetchTableData()
}

const handleReset = () => {
  queryForm.keyword = undefined
  queryForm.status = undefined
  queryForm.borrowType = undefined
  handleQuery()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTableData()
}

const handleViewDetail = (record: DocBorrowVO) => {
  router.push(`/borrow/detail/${record.id}`)
}

const handleOpenApprove = (record: DocBorrowVO) => {
  currentBorrowRecord.value = record
  approveForm.borrowId = record.id
  approveForm.approveResult = 'approved'
  approveForm.approveOpinion = undefined
  approveModalVisible.value = true
}

const handleApproveSubmit = async () => {
  if (!approveForm.approveResult) {
    message.warning('请选择审批结果')
    return
  }
  approveLoading.value = true
  try {
    await approveBorrow(approveForm)
    message.success(approveForm.approveResult === 'approved' ? '已批准' : '已驳回')
    approveModalVisible.value = false
    fetchTableData()
  } catch (error) {
    message.error('审批失败')
  } finally {
    approveLoading.value = false
  }
}

const handleReturn = async (record: DocBorrowVO) => {
  try {
    await returnBorrow(record.id)
    message.success('归还成功')
    fetchTableData()
  } catch (error) {
    message.error('归还失败')
  }
}

const handleViewContent = async (record: DocBorrowVO) => {
  try {
    const content = await getWatermarkedContent(record.id)
    watermarkedContent.value = content || ''
    contentModalVisible.value = true
  } catch (error) {
    message.error('获取内容失败')
  }
}

onMounted(() => {
  fetchTableData()
})
</script>

<style scoped lang="less">
.watermarked-content {
  min-height: 400px;
  padding: 24px;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  line-height: 1.8;
}
</style>
