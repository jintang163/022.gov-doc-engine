<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">签章操作</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="文档选择" :bordered="false" style="margin-bottom: 16px;">
          <a-form :model="formData" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="16">
                <a-form-item label="公文">
                  <a-input
                    v-model:value="documentTitle"
                    placeholder="请选择公文"
                    readonly
                    @click="showDocumentModal = true"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-button style="width: 100%; margin-top: 30px;" @click="showDocumentModal = true">
                  选择公文
                </a-button>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

        <a-card title="印章选择" :bordered="false" style="margin-bottom: 16px;">
          <a-form :model="formData" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="16">
                <a-form-item label="选择印章">
                  <a-select
                    v-model:value="formData.sealId"
                    placeholder="请选择可用印章"
                    @change="handleSealChange"
                    :loading="sealLoading"
                  >
                    <a-select-option v-for="seal in availableSeals" :key="seal.id" :value="seal.id">
                      {{ seal.sealName }} ({{ seal.sealType }})
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <div class="seal-preview" v-if="selectedSeal">
                  <img :src="selectedSeal.sealImageUrl" :alt="selectedSeal.sealName" />
                  <p class="seal-name">{{ selectedSeal.sealName }}</p>
                </div>
                <div class="seal-preview empty" v-else>
                  <FileImageOutlined style="font-size: 48px; color: #d9d9d9;" />
                  <p style="color: #999; margin-top: 8px;">暂无印章预览</p>
                </div>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

        <a-card title="签章类型" :bordered="false" style="margin-bottom: 16px;">
          <a-radio-group v-model:value="formData.signatureType" button-style="solid">
            <a-radio-button value="SIGNATURE">落款章</a-radio-button>
            <a-radio-button value="RIDING">骑缝章</a-radio-button>
          </a-radio-group>
        </a-card>

        <a-card title="签章参数" :bordered="false" style="margin-bottom: 16px;">
          <a-form :model="formData" layout="vertical">
            <a-row :gutter="16">
              <template v-if="formData.signatureType === 'SIGNATURE'">
                <a-col :span="12">
                  <a-form-item label="页码">
                    <a-input-number
                      v-model:value="formData.pageNumber"
                      :min="1"
                      style="width: 100%;"
                      placeholder="默认最后一页"
                    />
                    <span style="color: #999; font-size: 12px;">留空或-1表示最后一页</span>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="位置 X (毫米)">
                    <a-input-number
                      v-model:value="formData.positionX"
                      :min="0"
                      :step="0.5"
                      style="width: 100%;"
                      placeholder="水平位置"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="位置 Y (毫米)">
                    <a-input-number
                      v-model:value="formData.positionY"
                      :min="0"
                      :step="0.5"
                      style="width: 100%;"
                      placeholder="垂直位置"
                    />
                  </a-form-item>
                </a-col>
              </template>
              <a-col :span="12">
                <a-form-item label="印章宽度 (毫米)">
                  <a-input-number
                    v-model:value="formData.sealWidth"
                    :min="10"
                    :max="200"
                    :step="1"
                    style="width: 100%;"
                    placeholder="默认使用印章设置"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="印章高度 (毫米)">
                  <a-input-number
                    v-model:value="formData.sealHeight"
                    :min="10"
                    :max="200"
                    :step="1"
                    style="width: 100%;"
                    placeholder="默认使用印章设置"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

        <a-card title="其他信息" :bordered="false" style="margin-bottom: 16px;">
          <a-form :model="formData" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="签章原因">
                  <a-input
                    v-model:value="formData.signReason"
                    placeholder="请输入签章原因"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="签章地点">
                  <a-input
                    v-model:value="formData.signLocation"
                    placeholder="请输入签章地点"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="签章密码">
                  <a-input-password
                    v-model:value="formData.password"
                    placeholder="请输入签章密码"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

        <a-card :bordered="false">
          <a-space>
            <a-button type="primary" @click="handlePreviewPosition" :loading="previewing">
              <template #icon><EyeOutlined /></template>
              预览位置
            </a-button>
            <a-button type="primary" @click="handleConfirmSign" :loading="signing">
              <template #icon><CheckCircleOutlined /></template>
              确认签章
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="位置预览" :bordered="false" style="margin-bottom: 16px;">
          <div class="preview-container">
            <div class="document-preview" v-if="formData.documentId > 0">
              <iframe
                :src="documentPreviewUrl"
                style="width: 100%; height: 600px; border: none; background: #fff;"
              />
            </div>
            <div class="document-preview empty" v-else>
              <FileTextOutlined style="font-size: 64px; color: #d9d9d9;" />
              <p style="color: #999; margin-top: 16px;">请先选择文档</p>
            </div>
          </div>
        </a-card>

        <a-card title="签章说明" :bordered="false">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="落款章">
              在指定页码的指定位置加盖印章，适用于公文末尾的签章
            </a-descriptions-item>
            <a-descriptions-item label="骑缝章">
              在文档的每一页边缘加盖印章，确保文档的完整性和不可篡改性
            </a-descriptions-item>
            <a-descriptions-item label="位置坐标">
              以毫米为单位，从页面左上角开始计算
            </a-descriptions-item>
            <a-descriptions-item label="印章尺寸">
              默认使用印章预设尺寸，可根据需要调整
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="showDocumentModal"
      title="选择公文"
      width="1000px"
      :footer="null"
    >
      <a-input
        v-model:value="documentKeyword"
        placeholder="搜索公文标题..."
        style="margin-bottom: 16px;"
        allow-clear
      >
        <template #prefix><SearchOutlined /></template>
      </a-input>
      <a-table
        :columns="documentColumns"
        :data-source="documentList"
        :loading="documentLoading"
        :pagination="documentPagination"
        row-key="id"
        bordered
        @change="handleDocumentPageChange"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="handleSelectDocument(record)">
              选择
            </a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal
      v-model:open="successModalVisible"
      title="签章成功"
      width="600px"
      :footer="null"
    >
      <a-result status="success" title="签章成功" sub-title="文档签章已完成">
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleViewDetail">
              <template #icon><FileTextOutlined /></template>
              查看详情
            </a-button>
            <a-button @click="handleDownloadSigned">
              <template #icon><DownloadOutlined /></template>
              下载文档
            </a-button>
          </a-space>
        </template>
      </a-result>
      <a-descriptions :column="1" bordered size="small" v-if="signedRecord">
        <a-descriptions-item label="公文标题">{{ signedRecord.documentTitle }}</a-descriptions-item>
        <a-descriptions-item label="印章名称">{{ signedRecord.sealName }}</a-descriptions-item>
        <a-descriptions-item label="签章类型">{{ signedRecord.signatureTypeName }}</a-descriptions-item>
        <a-descriptions-item label="签章人">{{ signedRecord.signer }}</a-descriptions-item>
        <a-descriptions-item label="签章时间">{{ signedRecord.signTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
  FileImageOutlined,
  FileTextOutlined,
  SearchOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import {
  listAvailableSeals,
  createSignature,
  getDocumentList,
  getDocumentDetail,
  getDocumentPreviewUrl,
  downloadSignedDocument,
  previewSignaturePosition
} from '@/api/signature'
import type {
  SealVO,
  DocSignatureVO,
  DocSignatureCreateDTO,
  DocDocument
} from '@/types/signature'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const sealLoading = ref(false)
const documentLoading = ref(false)
const previewing = ref(false)
const signing = ref(false)
const showDocumentModal = ref(false)
const successModalVisible = ref(false)
const documentKeyword = ref('')
const documentTitle = ref('')
const availableSeals = ref<SealVO[]>([])
const selectedSeal = ref<SealVO | null>(null)
const documentList = ref<DocDocument[]>([])
const signedRecord = ref<DocSignatureVO | null>(null)

const formData = reactive<DocSignatureCreateDTO>({
  documentId: 0,
  sealId: 0,
  signatureType: 'SIGNATURE',
  pageNumber: -1,
  positionX: 150,
  positionY: 250,
  sealWidth: 40,
  sealHeight: 40,
  signReason: '',
  signLocation: '',
  password: ''
})

const documentPagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const documentColumns = [
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', width: 250 },
  { title: '发文字号', dataIndex: 'docNumber', key: 'docNumber', width: 150 },
  { title: '公文类型', dataIndex: 'docType', key: 'docType', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 100 }
]

const documentPreviewUrl = computed(() => {
  if (formData.documentId > 0) {
    return getDocumentPreviewUrl(formData.documentId)
  }
  return ''
})

const loadAvailableSeals = async () => {
  sealLoading.value = true
  try {
    availableSeals.value = await listAvailableSeals()
  } catch (error) {
    console.error('加载可用印章失败:', error)
    message.error('加载可用印章失败')
  } finally {
    sealLoading.value = false
  }
}

const handleSealChange = (sealId: number) => {
  const seal = availableSeals.value.find((s) => s.id === sealId)
  if (seal) {
    selectedSeal.value = seal
    formData.sealWidth = seal.sealWidth || 40
    formData.sealHeight = seal.sealHeight || 40
  } else {
    selectedSeal.value = null
  }
}

const loadDocumentList = async () => {
  documentLoading.value = true
  try {
    const result = await getDocumentList({
      pageNum: documentPagination.current,
      pageSize: documentPagination.pageSize,
      keyword: documentKeyword.value
    })
    documentList.value = result.list || []
    documentPagination.total = result.total || 0
  } catch (error) {
    console.error('加载公文列表失败:', error)
    message.error('加载公文列表失败')
  } finally {
    documentLoading.value = false
  }
}

const handleDocumentPageChange = (paginationInfo: { current: number; pageSize: number }) => {
  documentPagination.current = paginationInfo.current
  documentPagination.pageSize = paginationInfo.pageSize
  loadDocumentList()
}

const handleSelectDocument = (doc: DocDocument) => {
  formData.documentId = doc.id
  documentTitle.value = doc.docTitle
  showDocumentModal.value = false
  message.success(`已选择公文：${doc.docTitle}`)
}

const handlePreviewPosition = async () => {
  if (!validateForm()) return

  previewing.value = true
  try {
    await previewSignaturePosition(formData)
    message.success('位置预览已更新')
  } catch (error) {
    console.error('预览位置失败:', error)
    message.error('预览位置失败')
  } finally {
    previewing.value = false
  }
}

const validateForm = (): boolean => {
  if (formData.documentId <= 0) {
    message.warning('请选择公文')
    return false
  }
  if (formData.sealId <= 0) {
    message.warning('请选择印章')
    return false
  }
  if (!formData.password) {
    message.warning('请输入签章密码')
    return false
  }
  if (formData.signatureType === 'SIGNATURE') {
    if (formData.positionX < 0 || formData.positionY < 0) {
      message.warning('请输入有效的位置坐标')
      return false
    }
  }
  if (formData.sealWidth <= 0 || formData.sealHeight <= 0) {
    message.warning('请输入有效的印章尺寸')
    return false
  }
  return true
}

const handleConfirmSign = async () => {
  if (!validateForm()) return

  Modal.confirm({
    title: '确认签章',
    content: '确认要对文档进行签章吗？签章后文档将不可修改。',
    okText: '确认签章',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      signing.value = true
      try {
        const result = await createSignature(formData)
        signedRecord.value = result
        successModalVisible.value = true
        message.success('签章成功')
      } catch (error) {
        console.error('签章失败:', error)
        message.error('签章失败')
      } finally {
        signing.value = false
      }
    }
  })
}

const handleViewDetail = () => {
  if (signedRecord.value) {
    router.push(`/signature/detail/${signedRecord.value.id}`)
  }
}

const handleDownloadSigned = async () => {
  if (!signedRecord.value) return

  try {
    const blob = await downloadSignedDocument(signedRecord.value.id)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `${signedRecord.value.documentTitle}_已签章.pdf`
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

const handleReset = () => {
  const defaultDocumentId = Number(route.query.documentId) || 0
  formData.documentId = defaultDocumentId
  formData.sealId = 0
  formData.signatureType = 'SIGNATURE'
  formData.pageNumber = -1
  formData.positionX = 150
  formData.positionY = 250
  formData.sealWidth = 40
  formData.sealHeight = 40
  formData.signReason = ''
  formData.signLocation = ''
  formData.password = ''
  selectedSeal.value = null
  documentTitle.value = ''

  if (defaultDocumentId > 0) {
    loadDocumentDetail(defaultDocumentId)
  }
}

const loadDocumentDetail = async (id: number) => {
  try {
    const doc = await getDocumentDetail(id)
    formData.documentId = doc.id
    documentTitle.value = doc.docTitle
  } catch (error) {
    console.error('加载公文详情失败:', error)
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadAvailableSeals()
  loadDocumentList()

  const documentId = Number(route.query.documentId)
  if (documentId > 0) {
    loadDocumentDetail(documentId)
  }
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

.seal-preview {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  background: #fafafa;

  &.empty {
    cursor: default;
  }

  img {
    max-width: 80px;
    max-height: 80px;
    object-fit: contain;
  }

  .seal-name {
    margin-top: 4px;
    font-size: 12px;
    color: #666;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 100%;
  }
}

.preview-container {
  background: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}

.document-preview {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;

  &.empty {
    height: 600px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
}
</style>
