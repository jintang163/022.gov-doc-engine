<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">归档详情</h2>
      <a-space>
        <a-button type="primary" @click="handleBorrow" :disabled="!detail">
          <template #icon><FileProtectOutlined /></template>
          借阅申请
        </a-button>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回列表
        </a-button>
      </a-space>
    </div>

    <a-card title="归档信息" :bordered="false" style="margin-bottom: 16px;">
      <a-descriptions :column="2" bordered size="small" v-if="detail">
        <a-descriptions-item label="归档编号">{{ detail.archiveNo }}</a-descriptions-item>
        <a-descriptions-item label="归档方式">{{ detail.archiveMethodName }}</a-descriptions-item>
        <a-descriptions-item label="保管期限">{{ detail.retentionPeriodName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(detail.status)">{{ detail.statusName }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="公文标题" :span="2">{{ detail.docTitle }}</a-descriptions-item>
        <a-descriptions-item label="发文字号">{{ detail.docNumber }}</a-descriptions-item>
        <a-descriptions-item label="文种分类">{{ detail.docType }}</a-descriptions-item>
        <a-descriptions-item label="密级">{{ detail.securityLevel }}</a-descriptions-item>
        <a-descriptions-item label="归档年度">{{ detail.archiveYear }}</a-descriptions-item>
        <a-descriptions-item label="归档日期">{{ detail.archiveDate }}</a-descriptions-item>
        <a-descriptions-item label="归档部门">{{ detail.archiveDeptName }}</a-descriptions-item>
        <a-descriptions-item label="存放位置">{{ detail.archiveLocation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="所属单位">{{ detail.unitName }}</a-descriptions-item>
      </a-descriptions>
      <a-skeleton v-else active :paragraph="{ rows: 6 }" />
    </a-card>

    <a-card title="内容预览" :bordered="false">
      <template #extra>
        <a-tag v-if="detail" color="blue">
          <template #icon><EyeOutlined /></template>
          快照
        </a-tag>
      </template>
      <div class="content-preview" v-if="detail">
        <iframe
          v-if="detail.docContentSnapshot"
          :srcdoc="detail.docContentSnapshot"
          class="preview-iframe"
        />
        <a-empty v-else description="暂无内容快照" />
      </div>
      <a-skeleton v-else active :paragraph="{ rows: 8 }" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, FileProtectOutlined, EyeOutlined } from '@ant-design/icons-vue'
import { getArchiveDetail } from '@/api/archive'
import type { DocArchiveVO } from '@/types/archive'
import { archiveStatusOptions } from '@/types/archive'

const route = useRoute()
const router = useRouter()

const detail = ref<DocArchiveVO | null>(null)

const getStatusColor = (status: string) => {
  const option = archiveStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }

  try {
    detail.value = await getArchiveDetail(id)
  } catch (error) {
    console.error('加载归档详情失败:', error)
    message.error('加载归档详情失败')
  }
}

const handleBorrow = () => {
  if (!detail.value) return
  router.push(`/borrow/apply?archiveId=${detail.value.id}`)
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
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

.content-preview {
  background: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}

.preview-iframe {
  width: 100%;
  height: 700px;
  border: none;
  background: #fff;
  border-radius: 4px;
}
</style>
