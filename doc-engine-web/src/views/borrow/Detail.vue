<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">借阅详情</h2>
      <a-space>
        <a-button
          v-if="detail && (detail.status === 'active' || detail.status === 'approved')"
          type="primary"
          @click="handleViewContent"
        >
          <template #icon><EyeOutlined /></template>
          查看内容
        </a-button>
        <a-button
          v-if="detail && detail.status === 'active'"
          @click="handleReturn"
          :loading="returning"
        >
          <template #icon><UndoOutlined /></template>
          归还
        </a-button>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="公文信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered size="small" v-if="detail?.archive">
            <a-descriptions-item label="公文标题" :span="2">
              {{ detail.archive.docTitle }}
            </a-descriptions-item>
            <a-descriptions-item label="发文字号">
              {{ detail.archive.docNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="文种分类">
              {{ detail.archive.archiveType }}
            </a-descriptions-item>
            <a-descriptions-item label="密级">
              {{ detail.archive.securityLevel }}
            </a-descriptions-item>
            <a-descriptions-item label="归档年度">
              {{ detail.archive.archiveYear }}
            </a-descriptions-item>
            <a-descriptions-item label="归档编号">
              {{ detail.archive.archiveNo }}
            </a-descriptions-item>
            <a-descriptions-item label="归档方式">
              {{ detail.archive.archiveMethodName }}
            </a-descriptions-item>
            <a-descriptions-item label="保管期限">
              {{ detail.archive.retentionPeriodName }}
            </a-descriptions-item>
            <a-descriptions-item label="归档部门">
              {{ detail.archive.archiveDeptName }}
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 5 }" />
        </a-card>

        <a-card title="内容预览" :bordered="false" v-if="contentVisible">
          <template #extra>
            <a-tag color="blue">
              <template #icon><EyeOutlined /></template>
              水印预览
            </a-tag>
          </template>
          <div class="content-preview">
            <div class="watermarked-content" v-html="watermarkedContent" />
          </div>
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="借阅信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="detail">
            <a-descriptions-item label="借阅单号">{{ detail.borrowNo }}</a-descriptions-item>
            <a-descriptions-item label="借阅方式">{{ detail.borrowTypeName }}</a-descriptions-item>
            <a-descriptions-item label="借阅事由">{{ detail.borrowReason }}</a-descriptions-item>
            <a-descriptions-item label="开始日期">{{ detail.startDate }}</a-descriptions-item>
            <a-descriptions-item label="结束日期">{{ detail.endDate }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(detail.status)">{{ detail.statusName }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="查看次数">{{ detail.viewCount }}</a-descriptions-item>
            <a-descriptions-item label="最后查看">{{ detail.lastViewTime || '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 6 }" />
        </a-card>

        <a-card title="申请人信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="detail">
            <a-descriptions-item label="申请人">{{ detail.applicantName }}</a-descriptions-item>
            <a-descriptions-item label="申请部门">{{ detail.applicantDeptName }}</a-descriptions-item>
            <a-descriptions-item label="申请时间">{{ detail.createTime }}</a-descriptions-item>
            <a-descriptions-item label="备注">{{ detail.remark || '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 3 }" />
        </a-card>

        <a-card title="审批信息" :bordered="false" v-if="detail && detail.status !== 'pending'">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="审批人">{{ detail.approverName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="审批时间">{{ detail.approveTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="审批意见">{{ detail.approveOpinion || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="待审批" :bordered="false" v-if="detail && detail.status === 'pending'">
          <a-form layout="vertical">
            <a-form-item label="审批结果">
              <a-radio-group v-model:value="approveForm.approveResult">
                <a-radio value="approved">批准</a-radio>
                <a-radio value="rejected">驳回</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="审批意见">
              <a-textarea
                v-model:value="approveForm.approveOpinion"
                :rows="3"
                placeholder="请输入审批意见"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleApprove" :loading="approving">
                提交审批
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  EyeOutlined,
  UndoOutlined
} from '@ant-design/icons-vue'
import {
  getBorrowDetail,
  approveBorrow,
  returnBorrow,
  getWatermarkedContent
} from '@/api/archive'
import type { DocBorrowVO, DocBorrowApproveDTO } from '@/types/archive'
import { borrowStatusOptions } from '@/types/archive'

const route = useRoute()
const router = useRouter()

const detail = ref<DocBorrowVO | null>(null)
const returning = ref(false)
const approving = ref(false)
const contentVisible = ref(false)
const watermarkedContent = ref('')

const approveForm = reactive<DocBorrowApproveDTO>({
  borrowId: 0,
  approveResult: 'approved',
  approveOpinion: undefined
})

const getStatusColor = (status: string) => {
  const option = borrowStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }

  try {
    detail.value = await getBorrowDetail(id)
    approveForm.borrowId = id
  } catch (error) {
    message.error('加载借阅详情失败')
  }
}

const handleViewContent = async () => {
  if (!detail.value) return
  try {
    const content = await getWatermarkedContent(detail.value.id)
    watermarkedContent.value = content || ''
    contentVisible.value = true
  } catch (error) {
    message.error('获取内容失败')
  }
}

const handleReturn = async () => {
  if (!detail.value) return
  returning.value = true
  try {
    await returnBorrow(detail.value.id)
    message.success('归还成功')
    await loadDetail()
  } catch (error) {
    message.error('归还失败')
  } finally {
    returning.value = false
  }
}

const handleApprove = async () => {
  if (!approveForm.approveResult) {
    message.warning('请选择审批结果')
    return
  }
  approving.value = true
  try {
    await approveBorrow(approveForm)
    message.success(approveForm.approveResult === 'approved' ? '已批准' : '已驳回')
    await loadDetail()
  } catch (error) {
    message.error('审批失败')
  } finally {
    approving.value = false
  }
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

.watermarked-content {
  min-height: 500px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 24px;
  line-height: 1.8;
}
</style>
