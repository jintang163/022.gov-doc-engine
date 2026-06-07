<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">验章结果</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" :loading="loading">
      <div v-if="verifyResult">
        <div class="verify-status">
          <div class="status-icon" :class="{ valid: verifyResult.valid, invalid: !verifyResult.valid }">
            <CheckCircleOutlined v-if="verifyResult.valid" />
            <CloseCircleOutlined v-else />
          </div>
          <div class="status-info">
            <h3 :class="{ 'text-success': verifyResult.valid, 'text-error': !verifyResult.valid }">
              {{ verifyResult.valid ? '签章有效' : '签章无效' }}
            </h3>
            <p class="status-name">{{ verifyResult.verifyStatusName }}</p>
          </div>
        </div>

        <a-alert
          :type="verifyResult.valid ? 'success' : 'error'"
          :message="verifyResult.valid ? '验证通过' : '验证失败'"
          :description="verifyResult.verifyMessage"
          show-icon
          style="margin-bottom: 24px;"
        />

        <a-descriptions title="验证信息" bordered :column="2" style="margin-bottom: 24px;">
          <a-descriptions-item label="验证状态">
            <a-tag :color="getVerifyStatusColor(verifyResult.verifyStatus)">
              {{ verifyResult.verifyStatusName }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="验证时间">
            {{ verifyResult.verifyTime }}
          </a-descriptions-item>
          <a-descriptions-item label="签章人">
            {{ verifyResult.signer }}
          </a-descriptions-item>
          <a-descriptions-item label="签章部门">
            {{ verifyResult.signerDept }}
          </a-descriptions-item>
          <a-descriptions-item label="签章时间">
            {{ verifyResult.signTime }}
          </a-descriptions-item>
          <a-descriptions-item label="证书序列号">
            <span class="monospace">{{ verifyResult.certificateSerialNumber }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="签名算法">
            {{ verifyResult.algorithm }}
          </a-descriptions-item>
          <a-descriptions-item label="哈希匹配">
            <a-tag :color="verifyResult.hashMatch ? 'success' : 'error'">
              {{ verifyResult.hashMatch ? '匹配' : '不匹配' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>

        <a-descriptions title="哈希信息" bordered :column="1" style="margin-bottom: 24px;">
          <a-descriptions-item label="文件哈希">
            <span class="monospace hash-text">{{ verifyResult.fileHash }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="文档哈希">
            <span class="monospace hash-text">{{ verifyResult.documentHash }}</span>
          </a-descriptions-item>
        </a-descriptions>

        <div class="action-area">
          <a-space>
            <a-button type="primary" @click="handleReVerify" :loading="verifying">
              <template #icon><ReloadOutlined /></template>
              重新验证
            </a-button>
            <a-button @click="handleBackToList">
              <template #icon><UnorderedListOutlined /></template>
              返回列表
            </a-button>
            <a-button @click="handleViewDetail">
              <template #icon><FileTextOutlined /></template>
              查看详情
            </a-button>
          </a-space>
        </div>
      </div>

      <a-empty v-else description="暂无验章结果" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ReloadOutlined,
  UnorderedListOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import { verifySignature } from '@/api/signature'
import type { DocSignatureVerifyResultVO } from '@/types/signature'
import { getVerifyStatusColor } from '@/types/signature'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const verifying = ref(false)
const verifyResult = ref<DocSignatureVerifyResultVO | null>(null)
const signatureId = ref<number>(0)

const loadVerifyResult = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }
  signatureId.value = id

  loading.value = true
  try {
    verifyResult.value = await verifySignature(id)
  } catch (error) {
    console.error('加载验章结果失败:', error)
    message.error('加载验章结果失败')
  } finally {
    loading.value = false
  }
}

const handleReVerify = async () => {
  verifying.value = true
  try {
    verifyResult.value = await verifySignature(signatureId.value)
    message.success('重新验证完成')
  } catch (error) {
    console.error('重新验证失败:', error)
    message.error('重新验证失败')
  } finally {
    verifying.value = false
  }
}

const handleBack = () => {
  router.back()
}

const handleBackToList = () => {
  router.push('/signature/list')
}

const handleViewDetail = () => {
  router.push(`/signature/detail/${signatureId.value}`)
}

onMounted(() => {
  loadVerifyResult()
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

.verify-status {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;

  .status-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48px;

    &.valid {
      background: #f6ffed;
      color: #52c41a;
    }

    &.invalid {
      background: #fff2f0;
      color: #ff4d4f;
    }
  }

  .status-info {
    h3 {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
    }

    .text-success {
      color: #52c41a;
    }

    .text-error {
      color: #ff4d4f;
    }

    .status-name {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
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

.action-area {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}
</style>
