<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">公文完整性验证</h2>
    </div>

    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="公文ID">
          <a-input-number
            v-model:value="searchDocId"
            :min="1"
            placeholder="请输入公文ID"
            style="width: 200px;"
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch" :loading="searching">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <template v-if="searched">
      <a-card v-if="currentIntegrity" :bordered="false" :loading="loading">
        <a-descriptions title="完整性信息" bordered :column="2">
          <a-descriptions-item label="公文ID">
            {{ currentIntegrity.docId }}
          </a-descriptions-item>
          <a-descriptions-item label="内容哈希">
            <span class="monospace hash-text">{{ currentIntegrity.contentHash }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="签名哈希">
            <span class="monospace hash-text">{{ currentIntegrity.signatureHash }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="哈希算法">
            {{ currentIntegrity.hashAlgorithm }}
          </a-descriptions-item>
          <a-descriptions-item label="版本号">
            {{ currentIntegrity.version }}
          </a-descriptions-item>
          <a-descriptions-item label="验证状态">
            <a-tag :color="getStatusColor(currentIntegrity.verifyStatus)">
              {{ getStatusLabel(currentIntegrity.verifyStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="验证时间">
            {{ currentIntegrity.verifyTime }}
          </a-descriptions-item>
          <a-descriptions-item label="验证人">
            {{ currentIntegrity.verifiedByName }}
          </a-descriptions-item>
        </a-descriptions>

        <div style="margin-top: 16px;">
          <a-button type="primary" @click="handleVerify" :loading="verifying">
            <template #icon><SafetyOutlined /></template>
            验证完整性
          </a-button>
        </div>

        <a-divider />

        <div style="margin-bottom: 12px;">
          <h3><HistoryOutlined style="margin-right: 8px;" />验证历史</h3>
        </div>
        <a-timeline v-if="historyList.length > 0">
          <a-timeline-item v-for="item in historyList" :key="item.id" :color="getTimelineColor(item.verifyStatus)">
            <div>
              <strong>版本 {{ item.version }}</strong>
              <a-tag :color="getStatusColor(item.verifyStatus)" style="margin-left: 8px;">
                {{ getStatusLabel(item.verifyStatus) }}
              </a-tag>
            </div>
            <div class="monospace hash-text" style="margin-top: 4px;">{{ item.contentHash }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 4px;">{{ item.verifyTime }}</div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无验证历史" />
      </a-card>

      <a-card v-else :bordered="false">
        <a-empty description="未找到该公文的完整性记录" />
      </a-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, SafetyOutlined, HistoryOutlined } from '@ant-design/icons-vue'
import { getIntegrityByDocId, getIntegrityHistory, verifyIntegrity } from '@/api/security'
import type { DocIntegrityVO, DocIntegrityVerifyDTO } from '@/types/security'
import { integrityStatusOptions } from '@/types/security'

const searchDocId = ref<number | undefined>(undefined)
const searched = ref(false)
const searching = ref(false)
const loading = ref(false)
const verifying = ref(false)
const currentIntegrity = ref<DocIntegrityVO | null>(null)
const historyList = ref<DocIntegrityVO[]>([])

const getStatusColor = (status: string) => {
  const opt = integrityStatusOptions.find(o => o.value === status)
  return opt?.color ?? 'default'
}

const getStatusLabel = (status: string) => {
  const opt = integrityStatusOptions.find(o => o.value === status)
  return opt?.label ?? status
}

const getTimelineColor = (status: string) => {
  if (status === 'verified') return 'green'
  if (status === 'tampered') return 'red'
  return 'blue'
}

const handleSearch = async () => {
  if (!searchDocId.value) {
    message.warning('请输入公文ID')
    return
  }
  searching.value = true
  loading.value = true
  try {
    const [integrity, history] = await Promise.all([
      getIntegrityByDocId(searchDocId.value),
      getIntegrityHistory(searchDocId.value)
    ])
    currentIntegrity.value = integrity
    historyList.value = history
    searched.value = true
  } catch {
    currentIntegrity.value = null
    historyList.value = []
    searched.value = true
  } finally {
    searching.value = false
    loading.value = false
  }
}

const handleVerify = async () => {
  if (!searchDocId.value) return
  verifying.value = true
  try {
    const dto: DocIntegrityVerifyDTO = { docId: searchDocId.value }
    const result = await verifyIntegrity(dto)
    currentIntegrity.value = result
    const history = await getIntegrityHistory(searchDocId.value)
    historyList.value = history
    message.success('完整性验证完成')
  } catch {
    message.error('完整性验证失败')
  } finally {
    verifying.value = false
  }
}

onMounted(() => {
  searched.value = false
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

.monospace {
  font-family: 'Consolas', 'Monaco', monospace;
}

.hash-text {
  word-break: break-all;
  font-size: 12px;
  color: #666;
}
</style>
