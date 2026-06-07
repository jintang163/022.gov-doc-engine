<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">签章记录</h2>
      <a-space>
        <a-button type="primary" @click="handleGoSign">
          <template #icon><PlusOutlined /></template>
          新增签章
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="公文标题">
          <a-input
            v-model:value="searchForm.documentTitle"
            placeholder="请输入公文标题"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item label="印章名称">
          <a-input
            v-model:value="searchForm.sealName"
            placeholder="请输入印章名称"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item label="签章人">
          <a-input
            v-model:value="searchForm.signer"
            placeholder="请输入签章人"
            allow-clear
            style="width: 150px;"
          />
        </a-form-item>
        <a-form-item label="签章类型">
          <a-select
            v-model:value="searchForm.signatureType"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option value="SIGNATURE">落款章</a-select-option>
            <a-select-option value="RIDING">骑缝章</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="验证状态">
          <a-select
            v-model:value="searchForm.verifyStatus"
            placeholder="全部"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option value="VERIFIED">验证通过</a-select-option>
            <a-select-option value="NOT_VERIFIED">未验证</a-select-option>
            <a-select-option value="VERIFY_FAILED">验证失败</a-select-option>
            <a-select-option value="TAMPERED">文档已篡改</a-select-option>
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
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'signatureType'">
            <a-tag color="blue">{{ record.signatureTypeName }}</a-tag>
          </template>
          <template v-else-if="column.key === 'verifyStatus'">
            <a-tag :color="getVerifyStatusColor(record.verifyStatus)">
              {{ getVerifyStatusLabel(record.verifyStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'isValid'">
            <a-tag :color="getValidStatusColor(record.isValid)">
              {{ getValidStatusLabel(record.isValid) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'position'">
            <span>X: {{ record.positionX }}, Y: {{ record.positionY }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleVerify(record)">验章</a-button>
              <a-button type="link" size="small" @click="handleDownload(record)">下载</a-button>
              <a-button type="link" size="small" @click="handlePreview(record)">预览</a-button>
              <a-button type="link" size="small" danger @click="handleRevoke(record)">撤销</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-empty v-if="dataSource.length === 0" description="暂无签章记录" />
    </a-card>

    <a-modal
      v-model:open="revokeModalVisible"
      title="撤销签章"
      width="500px"
      @ok="handleConfirmRevoke"
      @cancel="revokeModalVisible = false"
      okText="确认撤销"
      okType="danger"
      :confirm-loading="revoking"
    >
      <p>确定要撤销该签章吗？撤销后签章将失效。</p>
      <a-form :model="revokeForm" layout="vertical">
        <a-form-item label="撤销原因">
          <a-textarea
            v-model:value="revokeForm.reason"
            :rows="3"
            placeholder="请输入撤销原因"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  getSignaturePage,
  verifySignature,
  downloadSignedDocument,
  revokeSignature
} from '@/api/signature'
import type {
  DocSignatureVO,
  DocSignatureQueryDTO
} from '@/types/signature'
import {
  getVerifyStatusColor,
  getVerifyStatusLabel,
  getValidStatusColor,
  getValidStatusLabel
} from '@/types/signature'

const router = useRouter()

const loading = ref(false)
const revoking = ref(false)
const dataSource = ref<DocSignatureVO[]>([])
const revokeModalVisible = ref(false)
const currentRecord = ref<DocSignatureVO | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive<DocSignatureQueryDTO>({
  documentTitle: '',
  sealName: '',
  signer: '',
  signatureType: '',
  verifyStatus: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

const revokeForm = reactive({
  reason: ''
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
  { title: '公文标题', dataIndex: 'documentTitle', key: 'documentTitle', width: 200, ellipsis: true },
  { title: '印章名称', dataIndex: 'sealName', key: 'sealName', width: 150 },
  { title: '印章类型', dataIndex: 'sealType', key: 'sealType', width: 120 },
  { title: '签章类型', dataIndex: 'signatureType', key: 'signatureType', width: 100 },
  { title: '页码', dataIndex: 'pageNumber', key: 'pageNumber', width: 80 },
  { title: '签章位置', key: 'position', width: 140 },
  { title: '签章人', dataIndex: 'signer', key: 'signer', width: 100 },
  { title: '签章时间', dataIndex: 'signTime', key: 'signTime', width: 180 },
  { title: '验证状态', dataIndex: 'verifyStatus', key: 'verifyStatus', width: 120 },
  { title: '是否有效', dataIndex: 'isValid', key: 'isValid', width: 100 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const result = await getSignaturePage(params)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('加载签章记录失败:', error)
    message.error('加载签章记录失败')
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
  searchForm.documentTitle = ''
  searchForm.sealName = ''
  searchForm.signer = ''
  searchForm.signatureType = ''
  searchForm.verifyStatus = ''
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

const handleGoSign = () => {
  router.push('/signature/sign')
}

const handleViewDetail = (record: DocSignatureVO) => {
  router.push(`/signature/detail/${record.id}`)
}

const handleVerify = async (record: DocSignatureVO) => {
  Modal.confirm({
    title: '验章确认',
    content: `确认要对 "${record.documentTitle}" 进行验章吗？`,
    okText: '确认验章',
    cancelText: '取消',
    onOk: async () => {
      try {
        await verifySignature(record.id)
        message.success('验章完成')
        router.push(`/signature/verify/${record.id}`)
      } catch (error) {
        console.error('验章失败:', error)
        message.error('验章失败')
      }
    }
  })
}

const handleDownload = async (record: DocSignatureVO) => {
  try {
    const blob = await downloadSignedDocument(record.id)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `${record.documentTitle}_已签章.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    message.error('下载失败')
  }
}

const handlePreview = (record: DocSignatureVO) => {
  router.push(`/signature/detail/${record.id}`)
}

const handleRevoke = (record: DocSignatureVO) => {
  currentRecord.value = record
  revokeForm.reason = ''
  revokeModalVisible.value = true
}

const handleConfirmRevoke = async () => {
  if (!currentRecord.value) return
  if (!revokeForm.reason.trim()) {
    message.warning('请输入撤销原因')
    return
  }

  revoking.value = true
  try {
    await revokeSignature(currentRecord.value.id, revokeForm.reason)
    message.success('撤销成功')
    revokeModalVisible.value = false
    loadData()
  } catch (error) {
    console.error('撤销失败:', error)
    message.error('撤销失败')
  } finally {
    revoking.value = false
  }
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
