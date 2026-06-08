<template>
  <div class="approval-panel">
    <a-card :bordered="false">
      <template #title>
        <span class="panel-title">审批操作</span>
      </template>

      <a-form layout="vertical">
        <a-form-item label="审批意见">
          <a-textarea
            v-model:value="approvalData.opinion"
            :rows="4"
            :placeholder="readonly ? '暂无审批意见' : '请输入审批意见'"
            :disabled="readonly"
          />
        </a-form-item>

        <a-form-item v-if="!readonly">
          <OpinionTemplateSelector
            :template-type="currentAction || 'general'"
            :current-user-id="currentUserId"
            @select="handleOpinionSelect"
          />
        </a-form-item>

        <a-form-item v-if="!readonly && showActions.includes('transfer')" label="附件上传">
          <AttachmentManager
            :document-id="attachmentDocId"
            :disabled="readonly"
            @change="handleAttachmentChange"
          />
        </a-form-item>

        <a-form-item
          v-if="!readonly && currentAction && needTargetNode"
          label="选择目标节点"
        >
          <a-select
            v-model:value="approvalData.targetNodeId"
            placeholder="请选择目标节点"
            @change="handleNodeChange"
          >
            <a-select-option
              v-for="node in availableNodes"
              :key="node.id"
              :value="node.id"
            >
              {{ node.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          v-if="!readonly && currentAction && needTargetUser"
          :label="currentAction === 'transfer' ? '转办给' : '加签人员'"
        >
          <ParticipantSelector
            v-model:modelValue="targetUsers"
            :multiple="currentAction === 'addSign'"
            :types="['user']"
          />
        </a-form-item>
      </a-form>

      <div class="action-buttons" v-if="!readonly">
        <a-space :size="8" wrap>
          <template v-for="action in filteredActions" :key="action.action">
            <a-button
              :type="action.type === 'primary' ? 'primary' : 'default'"
              :danger="action.type === 'danger'"
              @click="handleActionClick(action)"
            >
              {{ action.actionName }}
            </a-button>
          </template>
        </a-space>
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
import { ref, computed, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { WfApprovalAction, WfApprovalData, WfNode, WfParticipantDTO } from '@/types/workflow'
import { defaultApprovalActions } from '@/types/workflow'
import OpinionTemplateSelector from './OpinionTemplateSelector.vue'
import ParticipantSelector from './ParticipantSelector.vue'
import AttachmentManager from './AttachmentManager.vue'

interface Props {
  taskId: string
  businessKey: string
  readonly?: boolean
  showActions?: string[]
  availableNodes?: WfNode[]
  currentUserId?: string
}

interface Emits {
  (e: 'approve', data: WfApprovalData): void
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  showActions: () => ['approve', 'reject', 'back', 'terminate', 'transfer', 'addSign'],
  availableNodes: () => [],
  currentUserId: ''
})

const emit = defineEmits<Emits>()

const attachmentDocId = ref(0)
const approvalData = ref<WfApprovalData>({
  taskId: '',
  businessKey: '',
  action: '',
  opinion: '',
  attachmentIds: [],
  targetNodeId: '',
  targetNodeName: '',
  targetUserId: '',
  targetUserName: '',
  addSignUsers: []
})

const currentAction = ref<string | null>(null)
const targetUsers = ref<WfParticipantDTO[]>([])
const confirmVisible = ref(false)
const pendingAction = ref<WfApprovalAction | null>(null)

const filteredActions = computed(() => {
  return defaultApprovalActions.filter(action =>
    props.showActions.includes(action.action)
  )
})

const needTargetNode = computed(() => {
  if (!currentAction.value) return false
  const action = defaultApprovalActions.find(a => a.action === currentAction.value)
  return action?.needTargetNode || false
})

const needTargetUser = computed(() => {
  if (!currentAction.value) return false
  const action = defaultApprovalActions.find(a => a.action === currentAction.value)
  return action?.needTargetUser || false
})

const confirmTitle = computed(() => {
  const actionMap: Record<string, string> = {
    approve: '确认通过',
    reject: '确认驳回',
    back: '确认退回',
    terminate: '确认终止',
    transfer: '确认转办',
    addSign: '确认加签'
  }
  return actionMap[currentAction.value || ''] || '确认操作'
})

const confirmContent = computed(() => {
  const actionMap: Record<string, string> = {
    approve: '确定要通过该审批吗？',
    reject: '确定要驳回该审批吗？',
    back: '确定要退回该审批吗？',
    terminate: '确定要终止该流程吗？此操作不可撤销。',
    transfer: '确定要将该任务转办给他人吗？',
    addSign: '确定要添加会签人员吗？'
  }
  return actionMap[currentAction.value || ''] || '确定要执行此操作吗？'
})

const handleActionClick = (action: WfApprovalAction) => {
  currentAction.value = action.action
  pendingAction.value = action

  if (action.action === 'terminate') {
    confirmVisible.value = true
    return
  }

  if (action.needTargetNode || action.needTargetUser) {
    return
  }

  confirmVisible.value = true
}

const handleOpinionSelect = (content: string) => {
  approvalData.value.opinion = content
}

const handleAttachmentChange = (attachments: any[]) => {
  approvalData.value.attachmentIds = attachments.map(a => a.id)
}

const handleNodeChange = (nodeId: string) => {
  const node = props.availableNodes.find(n => n.id === nodeId)
  if (node) {
    approvalData.value.targetNodeId = nodeId
    approvalData.value.targetNodeName = node.name
  }
}

const handleConfirm = () => {
  if (!pendingAction.value) return

  if (needTargetNode.value && !approvalData.value.targetNodeId) {
    message.warning('请选择目标节点')
    return
  }

  if (needTargetUser.value && targetUsers.value.length === 0) {
    message.warning('请选择目标用户')
    return
  }

  if (needTargetUser.value && currentAction.value === 'transfer') {
    approvalData.value.targetUserId = targetUsers.value[0]?.value || ''
    approvalData.value.targetUserName = targetUsers.value[0]?.label || ''
  }

  if (needTargetUser.value && currentAction.value === 'addSign') {
    approvalData.value.addSignUsers = targetUsers.value
  }

  const submitData: WfApprovalData = {
    ...approvalData.value,
    taskId: props.taskId,
    businessKey: props.businessKey,
    action: currentAction.value || ''
  }

  emit('approve', submitData)
  confirmVisible.value = false
  resetForm()
}

const resetForm = () => {
  approvalData.value = {
    taskId: '',
    businessKey: '',
    action: '',
    opinion: '',
    attachmentIds: [],
    targetNodeId: '',
    targetNodeName: '',
    targetUserId: '',
    targetUserName: '',
    addSignUsers: []
  }
  currentAction.value = null
  targetUsers.value = []
  pendingAction.value = null
}

watch(() => props.businessKey, (val) => {
  if (val) {
    attachmentDocId.value = Number(val) || Date.now()
  }
}, { immediate: true })
</script>

<style scoped lang="less">
.approval-panel {
  .panel-title {
    font-weight: 500;
    font-size: 16px;
  }

  .action-buttons {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
