<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">处理详情</h2>
      <a-space>
        <a-button
          type="primary"
          danger
          @click="showUrgeModal"
          v-if="detail && detail.handlingType === 'assign' && detail.status === 'pending'"
        >
          <template #icon><BellOutlined /></template>
          催办
        </a-button>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="收文信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered size="small" v-if="detail?.incoming">
            <a-descriptions-item label="公文标题" :span="2">
              {{ detail.incoming.docTitle }}
            </a-descriptions-item>
            <a-descriptions-item label="来源单位">
              {{ detail.incoming.sourceUnit }}
            </a-descriptions-item>
            <a-descriptions-item label="来文字号">
              {{ detail.incoming.sourceDocNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="密级">
              {{ detail.incoming.securityLevel }}
            </a-descriptions-item>
            <a-descriptions-item label="缓急程度">
              {{ detail.incoming.urgencyLevel }}
            </a-descriptions-item>
            <a-descriptions-item label="收文日期">
              {{ detail.incoming.receivedDate }}
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              {{ detail.incoming.statusName }}
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 5 }" />
        </a-card>

        <a-card title="催办记录" :bordered="false">
          <a-timeline v-if="urgeLogs.length > 0">
            <a-timeline-item
              v-for="log in urgeLogs"
              :key="log.id"
              :color="log.status === 'acknowledged' ? 'green' : 'blue'"
            >
              <div class="urge-item">
                <div class="urge-header">
                  <span class="urge-type">
                    {{ log.urgeType === 'system' ? '系统催办' : '人工催办' }}
                  </span>
                  <a-tag :color="log.status === 'acknowledged' ? 'success' : 'processing'">
                    {{ log.statusName }}
                  </a-tag>
                  <span class="urge-time">{{ log.createTime }}</span>
                </div>
                <div class="urge-content">{{ log.urgeContent }}</div>
                <div class="urge-footer">
                  被催办人：{{ log.urgedUserName }}（{{ log.urgedDeptName }}）
                  <template v-if="log.acknowledgeTime">
                    ，确认时间：{{ log.acknowledgeTime }}
                  </template>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无催办记录" />
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="处理信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="detail">
            <a-descriptions-item label="处理编号">{{ detail.handlingNo }}</a-descriptions-item>
            <a-descriptions-item label="处理类型">{{ detail.handlingTypeName }}</a-descriptions-item>
            <a-descriptions-item label="处理人">{{ detail.handlerName }}</a-descriptions-item>
            <a-descriptions-item label="处理部门">{{ detail.handlerDeptName }}</a-descriptions-item>
            <a-descriptions-item label="目标部门">{{ detail.targetDeptName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="目标人员">{{ detail.targetUserName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="办理期限">{{ detail.deadline || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(detail.status)">{{ detail.statusName }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="处理时间">{{ detail.handlingTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="处理意见">{{ detail.opinion || '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 6 }" />
        </a-card>

        <a-card
          title="反馈信息"
          :bordered="false"
          style="margin-bottom: 16px;"
          v-if="detail && (detail.feedbackContent || detail.feedbackTime)"
        >
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="反馈内容">{{ detail.feedbackContent }}</a-descriptions-item>
            <a-descriptions-item label="反馈时间">{{ detail.feedbackTime }}</a-descriptions-item>
            <a-descriptions-item label="反馈附件">{{ detail.feedbackAttachment || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card
          title="操作区"
          :bordered="false"
          v-if="detail && detail.handlingType === 'assign' && detail.status === 'pending'"
        >
          <a-form layout="vertical">
            <a-form-item label="反馈内容">
              <a-textarea
                v-model:value="feedbackForm.feedbackContent"
                :rows="4"
                placeholder="请输入反馈内容"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSubmitFeedback" :loading="submitting">
                <template #icon><SendOutlined /></template>
                提交反馈
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="urgeModalVisible"
      title="发送催办"
      :confirm-loading="urging"
      @ok="handleUrgeSubmit"
      @cancel="urgeModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="催办内容">
          <a-textarea
            v-model:value="urgeForm.urgeContent"
            :rows="4"
            placeholder="请输入催办内容"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, SendOutlined, BellOutlined } from '@ant-design/icons-vue'
import { getHandlingDetail, submitFeedback } from '@/api/incoming'
import { urgeHandling, getUrgeLogsByHandling } from '@/api/supervision'
import type { DocHandlingVO, DocHandlingFeedbackDTO } from '@/types/incoming'
import type { DocUrgeLogVO } from '@/types/supervision'
import { handlingStatusOptions } from '@/types/incoming'

const route = useRoute()
const router = useRouter()

const detail = ref<DocHandlingVO | null>(null)
const submitting = ref(false)
const urgeLogs = ref<DocUrgeLogVO[]>([])
const urgeModalVisible = ref(false)
const urging = ref(false)

const feedbackForm = reactive<DocHandlingFeedbackDTO>({
  handlingId: 0,
  feedbackContent: undefined
})

const urgeForm = reactive({
  urgeContent: '请尽快处理该公文'
})

const getStatusColor = (status: string) => {
  const option = handlingStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }

  try {
    detail.value = await getHandlingDetail(id)
    feedbackForm.handlingId = id
    loadUrgeLogs(id)
  } catch (error) {
    message.error('加载处理详情失败')
  }
}

const loadUrgeLogs = async (handlingId: number) => {
  try {
    urgeLogs.value = await getUrgeLogsByHandling(handlingId)
  } catch (error) {
    console.error('加载催办记录失败', error)
  }
}

const showUrgeModal = () => {
  urgeForm.urgeContent = '请尽快处理该公文'
  urgeModalVisible.value = true
}

const handleUrgeSubmit = async () => {
  const id = Number(route.params.id)
  urging.value = true
  try {
    await urgeHandling(id, urgeForm.urgeContent)
    message.success('催办发送成功')
    urgeModalVisible.value = false
    loadUrgeLogs(id)
  } catch (error) {
    message.error('催办发送失败')
  } finally {
    urging.value = false
  }
}

const handleSubmitFeedback = async () => {
  if (!feedbackForm.feedbackContent) {
    message.warning('请输入反馈内容')
    return
  }
  submitting.value = true
  try {
    await submitFeedback(feedbackForm)
    message.success('反馈提交成功')
    await loadDetail()
  } catch (error) {
    message.error('反馈提交失败')
  } finally {
    submitting.value = false
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

.urge-item {
  padding: 4px 0;

  .urge-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;

    .urge-type {
      font-weight: 500;
      color: #1890ff;
    }

    .urge-time {
      margin-left: auto;
      color: #8c8c8c;
      font-size: 12px;
    }
  }

  .urge-content {
    color: #595959;
    margin-bottom: 6px;
  }

  .urge-footer {
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
