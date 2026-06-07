<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">签章详情</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <a-button type="primary" @click="handleVerify" :loading="verifying">
          <template #icon><SafetyCertificateOutlined /></template>
          验章
        </a-button>
        <a-button type="primary" @click="handleDownload" :loading="downloading">
          <template #icon><DownloadOutlined /></template>
          下载
        </a-button>
        <a-button danger @click="handleRevoke" :disabled="signatureDetail?.isValid === 0">
          <template #icon><DeleteOutlined /></template>
          撤销
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="文档预览" :bordered="false" style="margin-bottom: 16px;">
          <div class="preview-container">
            <div class="document-preview" v-if="signatureDetail">
              <iframe
                :src="documentPreviewUrl"
                style="width: 100%; height: 700px; border: none; background: #fff;"
              />
            </div>
            <div class="document-preview empty" v-else>
              <a-spin tip="加载中..." />
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="签章信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="signatureDetail">
            <a-descriptions-item label="公文标题">{{ signatureDetail.documentTitle }}</a-descriptions-item>
            <a-descriptions-item label="印章名称">{{ signatureDetail.sealName }}</a-descriptions-item>
            <a-descriptions-item label="印章类型">{{ signatureDetail.sealType }}</a-descriptions-item>
            <a-descriptions-item label="签章类型">
              <a-tag color="blue">{{ signatureDetail.signatureTypeName }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="页码">
              {{ signatureDetail.pageNumber === -1 ? '最后一页' : `第 ${signatureDetail.pageNumber} 页` }}
            </a-descriptions-item>
            <a-descriptions-item label="签章位置">
              X: {{ signatureDetail.positionX }}, Y: {{ signatureDetail.positionY }} 毫米
            </a-descriptions-item>
            <a-descriptions-item label="印章尺寸">
              {{ signatureDetail.sealWidth }} x {{ signatureDetail.sealHeight }} 毫米
            </a-descriptions-item>
            <a-descriptions-item label="签章原因">{{ signatureDetail.signReason || '-' }}</a-descriptions-item>
            <a-descriptions-item label="签章地点">{{ signatureDetail.signLocation || '-' }}</a-descriptions-item>
            <a-descriptions-item label="签章人">{{ signatureDetail.signer }}</a-descriptions-item>
            <a-descriptions-item label="签章部门">{{ signatureDetail.signerDept }}</a-descriptions-item>
            <a-descriptions-item label="签章时间">{{ signatureDetail.signTime }}</a-descriptions-item>
            <a-descriptions-item label="验证状态">
              <a-tag :color="getVerifyStatusColor(signatureDetail.verifyStatus)">
                {{ getVerifyStatusLabel(signatureDetail.verifyStatus) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="是否有效">
              <a-tag :color="getValidStatusColor(signatureDetail.isValid)">
                {{ getValidStatusLabel(signatureDetail.isValid) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="证书序列号">
              <span class="monospace">{{ signatureDetail.certificateSerialNumber }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="签名算法">{{ signatureDetail.algorithm }}</a-descriptions-item>
            <a-descriptions-item label="文件哈希">
              <span class="monospace hash-text">{{ signatureDetail.fileHash }}</span>
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 12 }" />
        </a-card>

        <a-card title="验章历史" :bordered="false">
          <a-timeline v-if="verifyHistory.length > 0">
            <a-timeline-item
              v-for="(item, index) in verifyHistory" :key="index"
              :color="item.valid ? 'green' : 'red'"
            >
              <div class="verify-item">
                <div class="verify-header">
                  <span class="verify-time">{{ item.verifyTime }}</span>
                  <a-tag :color="item.valid ? 'success' : 'error'">
                    {{ item.valid ? '验证通过' : '验证失败' }}
                  </a-tag>
                </div>
                <div class="verify-message">{{ item.verifyMessage }}</div>
                <div class="verify-hash">
                  <span>哈希匹配：</span>
                  <a-tag :color="item.hashMatch ? 'success' : 'error'">
                    {{ item.hashMatch ? '是' : '否' }}
                  </a-tag>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无验章历史" />
        </a-card>
      </a-col>
    </a-row>

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
      <p>确定要撤销该签章吗？撤销后签章将失效，此操作不可恢复。</p>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  SafetyCertificateOutlined,
  DownloadOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  getSignatureDetail,
  verifySignature,
  downloadSignedDocument,
  revokeSignature,
  getVerifyHistory,
  getSignedDocumentPreviewUrl,
  getSignaturePage
} from '@/api/signature'
import type {
  DocSignatureVO,
  DocSignatureVerifyResultVO
} from '@/types/signature'
import {
  getVerifyStatusColor,
  getVerifyStatusLabel,
  getValidStatusColor,
  getValidStatusLabel
} from '@/types/signature'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const verifying = ref(false)
const downloading = ref(false)
const revoking = ref(false)
const signatureDetail = ref<DocSignatureVO | null>(null)
const verifyHistory = ref<DocSignatureVerifyResultVO[]>([])
const revokeModalVisible = ref(false)
const signatureId = ref<number>(0)

const revokeForm = reactive({
  reason: ''
})

const documentPreviewUrl = computed(() => {
  if (signatureId.value > 0) {
    return getSignedDocumentPreviewUrl(signatureId.value)
  }
  return ''
})

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }
  signatureId.value = id

  loading.value = true
  try {
    signatureDetail.value = await getSignatureDetail(id)
  } catch (error) {
    console.error('加载签章详情失败:', error)
    message.error('加载签章详情失败')
  } finally {
    loading.value = false
  }
}

const loadVerifyHistory = async () => {
  if (!signatureId.value) return

  try {
    verifyHistory.value = await getVerifyHistory(signatureId.value)
  } catch (error) {
    console.error('加载验章历史失败:', error)
  }
}

const handleVerify = async () => {
  verifying.value = true
  try {
    await verifySignature(signatureId.value)
    message.success('验章完成')
    await loadDetail()
    await loadVerifyHistory()
    router.push(`/signature/verify/${signatureId.value}`)
  } catch (error) {
    console.error('验章失败:', error)
    message.error('验章失败')
  } finally {
    verifying.value = false
  }
}

const handleDownload = async () => {
  if (!signatureDetail.value) return

  downloading.value = true
  try {
    const blob = await downloadSignedDocument(signatureId.value)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `${signatureDetail.value.documentTitle}_已签章.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    message.error('下载失败')
  } finally {
    downloading.value = false
  }
}

const handleRevoke = () => {
  if (!signatureDetail.value || signatureDetail.value.isValid === 0) {
    message.warning('该签章已无效，无需撤销')
    return
  }
  revokeForm.reason = ''
  revokeModalVisible.value = true
}

const handleConfirmRevoke = async () => {
  if (!revokeForm.reason.trim()) {
    message.warning('请输入撤销原因')
    return
  }

  Modal.confirm({
    title: '确认撤销',
    content: '确认要撤销该签章吗？此操作不可恢复。',
    okText: '确认撤销',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      revoking.value = true
      try {
        await revokeSignature(signatureId.value, revokeForm.reason)
        message.success('撤销成功')
        revokeModalVisible.value = false
        await loadDetail()
        await loadVerifyHistory()
      } catch (error) {
        console.error('撤销失败:', error)
        message.error('撤销失败')
      } finally {
        revoking.value = false
      }
    }
  })
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
  loadVerifyHistory()
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
    height: 700px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.monospace {
  font-family: 'Consolas', 'Monaco', monospace;
}

.hash-text {
  word-break: break-all;
  font-size: 12px;
  color: #666;
}

.verify-item {
  padding: 8px 0;

  .verify-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;

    .verify-time {
      font-size: 12px;
      color: #999;
    }
  }

  .verify-message {
    font-size: 14px;
    color: #333;
    margin-bottom: 4px;
  }

  .verify-hash {
    font-size: 12px;
    color: #666;
  }
}
</style>
