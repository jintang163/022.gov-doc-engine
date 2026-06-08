<template>
  <div class="countersign-panel">
    <a-card :bordered="false" v-if="countersignInfo">
      <template #title>
        <span class="panel-title">{{ countersignInfo.nodeName }} - 会签详情</span>
        <a-tag :color="countersignInfo.status === 'completed' ? 'green' : 'blue'" class="status-tag">
          {{ countersignInfo.status === 'completed' ? '已完成' : '进行中' }}
        </a-tag>
      </template>

      <div class="countersign-info">
        <a-descriptions :column="2" size="small" bordered>
          <a-descriptions-item label="会签类型">
            {{ countersignInfo.typeName }}
          </a-descriptions-item>
          <a-descriptions-item label="投票规则">
            {{ countersignInfo.voteRuleName }}
            <template v-if="countersignInfo.voteRule === 'percentage'">
              （{{ countersignInfo.percentage }}%）
            </template>
            <template v-if="countersignInfo.voteRule === 'amount'">
              （{{ countersignInfo.amount }}人）
            </template>
          </a-descriptions-item>
          <a-descriptions-item label="应签人数">
            {{ countersignInfo.totalCount }} 人
          </a-descriptions-item>
          <a-descriptions-item label="已签人数">
            {{ countersignInfo.signedCount }} 人
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">会签进度</span>
          <span class="progress-count">
            {{ countersignInfo.signedCount }} / {{ countersignInfo.totalCount }}
          </span>
        </div>
        <a-progress
          :percent="Math.round((countersignInfo.signedCount / countersignInfo.totalCount) * 100)"
          :stroke-color="getProgressColor()"
        />
        <div class="vote-stats">
          <a-space :size="16">
            <span class="stat-item agree">
              <check-circle-outlined /> 同意 {{ countersignInfo.agreeCount }} 票
            </span>
            <span class="stat-item oppose">
              <close-circle-outlined /> 反对 {{ countersignInfo.opposeCount }} 票
            </span>
            <span class="stat-item abstain">
              <minus-circle-outlined /> 弃权 {{ countersignInfo.abstainCount }} 票
            </span>
          </a-space>
        </div>
      </div>

      <div class="signers-section">
        <div class="section-title">会签人员</div>
        <a-list
          :data-source="countersignInfo.signers"
          :pagination="false"
          item-layout="horizontal"
        >
          <template #renderItem="{ item }">
            <a-list-item class="signer-item">
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar>{{ item.signerName.charAt(0) }}</a-avatar>
                </template>
                <template #title>
                  <span class="signer-name">{{ item.signerName }}</span>
                  <a-tag
                    :color="getStatusColor(item.status)"
                    class="signer-status"
                  >
                    {{ item.statusName }}
                  </a-tag>
                  <a-tag
                    v-if="item.result"
                    :color="getResultColor(item.result)"
                    class="signer-result"
                  >
                    {{ item.resultName }}
                  </a-tag>
                </template>
                <template #description>
                  <div v-if="item.signTime" class="sign-time">
                    <clock-circle-outlined /> {{ item.signTime }}
                  </div>
                  <div v-if="item.duration" class="sign-duration">
                    耗时：{{ item.duration }}
                  </div>
                  <div v-if="item.opinion" class="sign-opinion">
                    意见：{{ item.opinion }}
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>

      <div v-if="!readonly && isCurrentUserPending" class="sign-action-section">
        <a-divider />
        <div class="section-title">签署操作</div>
        <a-form layout="vertical">
          <a-form-item label="签署意见">
            <a-textarea
              v-model:value="signData.opinion"
              :rows="3"
              placeholder="请输入签署意见（可选）"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSign('agree')">
                <template #icon><check-circle-outlined /></template>
                同意
              </a-button>
              <a-button danger @click="handleSign('oppose')">
                <template #icon><close-circle-outlined /></template>
                反对
              </a-button>
              <a-button @click="handleSign('abstain')">
                <template #icon><minus-circle-outlined /></template>
                弃权
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </a-card>

    <a-modal
      v-model:open="confirmVisible"
      :title="confirmTitle"
      @ok="handleConfirm"
      @cancel="confirmVisible = false"
    >
      <p>{{ confirmContent }}</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { WfCountersignInfoVO, WfCountersignData, WfCountersignSignerVO } from '@/types/workflow'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  MinusCircleOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'

interface Props {
  countersignId: string
  countersignInfo?: WfCountersignInfoVO | null
  readonly?: boolean
  currentUserId?: string
}

interface Emits {
  (e: 'sign', data: WfCountersignData): void
}

const props = withDefaults(defineProps<Props>(), {
  countersignInfo: null,
  readonly: false,
  currentUserId: ''
})

const emit = defineEmits<Emits>()

const signData = ref<WfCountersignData>({
  countersignId: '',
  result: 'agree',
  opinion: ''
})

const confirmVisible = ref(false)
const pendingResult = ref<'agree' | 'oppose' | 'abstain' | null>(null)

const isCurrentUserPending = computed(() => {
  if (!props.countersignInfo || !props.currentUserId) return false
  return props.countersignInfo.signers.some(
    (s: WfCountersignSignerVO) => s.signerId === props.currentUserId && s.status === 'pending'
  )
})

const confirmTitle = computed(() => {
  const titles: Record<string, string> = {
    agree: '确认同意',
    oppose: '确认反对',
    abstain: '确认弃权'
  }
  return titles[pendingResult.value || ''] || '确认签署'
})

const confirmContent = computed(() => {
  const contents: Record<string, string> = {
    agree: '确定要同意此会签吗？',
    oppose: '确定要反对此会签吗？',
    abstain: '确定要弃权吗？'
  }
  return contents[pendingResult.value || ''] || '确定要执行此操作吗？'
})

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    pending: 'default',
    signed: 'green',
    delegated: 'orange'
  }
  return colors[status] || 'default'
}

const getResultColor = (result: string): string => {
  const colors: Record<string, string> = {
    agree: 'green',
    oppose: 'red',
    abstain: 'default'
  }
  return colors[result] || 'default'
}

const getProgressColor = (): string => {
  if (!props.countersignInfo) return '#1890ff'
  const ratio = props.countersignInfo.agreeCount / props.countersignInfo.totalCount
  if (ratio >= 0.75) return '#52c41a'
  if (ratio >= 0.5) return '#1890ff'
  if (ratio >= 0.25) return '#faad14'
  return '#ff4d4f'
}

const handleSign = (result: 'agree' | 'oppose' | 'abstain') => {
  pendingResult.value = result
  signData.value.result = result
  confirmVisible.value = true
}

const handleConfirm = () => {
  if (!pendingResult.value) return

  const submitData: WfCountersignData = {
    countersignId: props.countersignId,
    result: pendingResult.value,
    opinion: signData.value.opinion
  }

  emit('sign', submitData)
  confirmVisible.value = false
  signData.value.opinion = ''
  pendingResult.value = null
  message.success('提交成功')
}
</script>

<style scoped lang="less">
.countersign-panel {
  .panel-title {
    font-weight: 500;
    font-size: 16px;
  }

  .status-tag {
    margin-left: 8px;
  }

  .countersign-info {
    margin-bottom: 16px;
  }

  .progress-section {
    margin-bottom: 16px;

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .progress-title {
        font-weight: 500;
      }

      .progress-count {
        color: #666;
        font-size: 14px;
      }
    }

    .vote-stats {
      margin-top: 12px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;

        &.agree {
          color: #52c41a;
        }

        &.oppose {
          color: #ff4d4f;
        }

        &.abstain {
          color: #8c8c8c;
        }
      }
    }
  }

  .section-title {
    font-weight: 500;
    margin-bottom: 12px;
  }

  .signers-section {
    .signer-item {
      .signer-name {
        font-weight: 500;
        margin-right: 8px;
      }

      .signer-status,
      .signer-result {
        margin-right: 4px;
      }

      .sign-time {
        color: #666;
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .sign-duration {
        color: #8c8c8c;
        font-size: 12px;
        margin-top: 4px;
      }

      .sign-opinion {
        color: #333;
        font-size: 13px;
        margin-top: 4px;
      }
    }
  }

  .sign-action-section {
    margin-top: 16px;
  }
}
</style>
