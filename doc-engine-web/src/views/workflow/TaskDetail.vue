<template>
  <div class="task-detail-page">
    <div class="page-header">
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item @click="handleBack">工作流</a-breadcrumb-item>
        <a-breadcrumb-item @click="handleBack">我的待办</a-breadcrumb-item>
        <a-breadcrumb-item>任务详情</a-breadcrumb-item>
      </a-breadcrumb>
      <h2 class="page-title">{{ taskDetail?.businessTitle || '任务详情' }}</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="8" :xl="6">
        <a-card :bordered="false" class="info-card" title="业务信息">
          <a-descriptions :column="1" bordered size="small" v-if="taskDetail">
            <a-descriptions-item label="业务标题">
              {{ taskDetail.businessTitle }}
            </a-descriptions-item>
            <a-descriptions-item label="流程名称">
              {{ taskDetail.processName }}
            </a-descriptions-item>
            <a-descriptions-item label="流程编号">
              {{ taskDetail.processKey }}
            </a-descriptions-item>
            <a-descriptions-item label="业务类型">
              {{ taskDetail.businessTypeName }}
            </a-descriptions-item>
            <a-descriptions-item label="发起人">
              {{ taskDetail.initiatorName }}
            </a-descriptions-item>
            <a-descriptions-item label="发起部门">
              {{ taskDetail.initiatorDeptName }}
            </a-descriptions-item>
            <a-descriptions-item label="发起时间">
              {{ taskDetail.startTime }}
            </a-descriptions-item>
            <a-descriptions-item label="当前节点">
              <a-tag color="blue">{{ taskDetail.nodeName }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="任务编号">
              {{ taskDetail.taskKey }}
            </a-descriptions-item>
            <a-descriptions-item label="任务名称">
              {{ taskDetail.taskName }}
            </a-descriptions-item>
            <a-descriptions-item label="任务类型">
              <a-tag :color="getTaskTypeColor(taskDetail.taskType)">
                {{ taskDetail.taskTypeName }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="优先级">
              <a-tag :color="getPriorityColor(taskDetail.priority)">
                {{ getPriorityLabel(taskDetail.priority) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="任务状态">
              <a-tag :color="getTaskStatusColor(taskDetail.status)">
                {{ taskDetail.statusName }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ taskDetail.createTime }}
            </a-descriptions-item>
            <a-descriptions-item label="耗时">
              {{ taskDetail.durationName || '-' }}
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 10 }" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="16" :xl="12">
        <a-card :bordered="false" class="graph-card" title="流程跟踪图">
          <ProcessTrackGraph
            :graph-data="graphData"
            :process-instance-id="taskDetail?.processInstanceId?.toString()"
            @node-click="handleNodeClick"
          />
        </a-card>

        <a-card :bordered="false" class="history-card" title="审批历史">
          <a-timeline v-if="opinionList.length > 0">
            <a-timeline-item
              v-for="(item, index) in opinionList"
              :key="item.id"
              :color="getTimelineColor(item.operationType)"
            >
              <div class="timeline-item">
                <div class="timeline-header">
                  <span class="operator">{{ item.approvalUserName }}</span>
                  <span class="dept">{{ item.approvalUserDeptName }}</span>
                  <a-tag :color="getOperationTypeColor(item.operationType)" size="small">
                    {{ item.operationTypeName }}
                  </a-tag>
                </div>
                <div class="timeline-node">
                  <span class="node-label">节点：</span>
                  <span class="node-name">{{ item.nodeName }}</span>
                </div>
                <div v-if="item.approvalOpinion" class="timeline-opinion">
                  <span class="opinion-label">意见：</span>
                  <span class="opinion-content">{{ item.approvalOpinion }}</span>
                </div>
                <div v-if="item.approvalAttachment" class="timeline-attachment">
                  <span class="attachment-label">附件：</span>
                  <a-tag color="blue">
                    <template #icon><PaperClipOutlined /></template>
                    查看附件
                  </a-tag>
                </div>
                <div class="timeline-footer">
                  <span class="time">
                    <ClockCircleOutlined /> {{ item.approvalTime }}
                  </span>
                  <span v-if="item.durationName" class="duration">
                    耗时：{{ item.durationName }}
                  </span>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无审批历史" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="24" :xl="6" v-if="showApprovalPanel || showCountersignPanel">
        <template v-if="isCountersignTask && taskDetail?.countersignId">
          <CountersignPanel
            :countersign-id="taskDetail.countersignId.toString()"
            :countersign-info="countersignInfo"
            :readonly="!isCurrentUserPendingSign"
            :current-user-id="currentUserId"
            @sign="handleCountersign"
          />
        </template>
        <template v-else-if="isTodoTask">
          <ApprovalPanel
            :task-id="taskId.toString()"
            :business-key="taskDetail?.businessKey || ''"
            :readonly="!isCurrentUserAssignee"
            :show-actions="availableActions"
            :available-nodes="availableNodes"
            :current-user-id="currentUserId"
            @approve="handleApproval"
          />
        </template>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ClockCircleOutlined,
  PaperClipOutlined
} from '@ant-design/icons-vue'
import ProcessTrackGraph from '@/components/ProcessTrackGraph.vue'
import ApprovalPanel from '@/components/ApprovalPanel.vue'
import CountersignPanel from '@/components/CountersignPanel.vue'
import {
  getTask,
  getProcessInstanceGraph,
  getOpinionsByProcessInstanceId,
  completeTask,
  signCountersign,
  getCountersign
} from '@/api/workflow'
import type {
  WfTaskVO,
  WfProcessGraphVO,
  WfApprovalOpinionVO,
  WfApprovalDTO,
  WfCountersignSignDTO,
  WfCountersignVO,
  WfNode
} from '@/types/workflow'
import {
  getTaskTypeColor,
  getPriorityColor,
  getPriorityLabel,
  getTaskStatusColor,
  getOperationTypeColor
} from '@/types/workflow'

const getCurrentUserId = (): string => {
  return localStorage.getItem('userId') || sessionStorage.getItem('userId') || ''
}

interface WfApprovalData {
  taskId: string
  businessKey: string
  action: string
  opinion: string
  attachmentIds: number[]
  targetNodeId: string
  targetNodeName: string
  targetUserId: string
  targetUserName: string
  addSignUsers: any[]
}

interface WfCountersignData {
  countersignId: string
  result: string
  opinion: string
}

interface WfCountersignInfoVO {
  nodeName: string
  status: string
  typeName: string
  voteRuleName: string
  voteRule: string
  percentage?: number
  amount?: number
  totalCount: number
  signedCount: number
  agreeCount: number
  opposeCount: number
  abstainCount: number
  signers: WfCountersignSignerVO[]
}

interface WfCountersignSignerVO {
  signerId: string
  signerName: string
  status: string
  statusName: string
  result?: string
  resultName?: string
  signTime?: string
  duration?: string
  opinion?: string
}

const route = useRoute()
const router = useRouter()

const currentUserId = computed(() => getCurrentUserId())

const loading = ref(false)
const taskId = computed(() => Number(route.params.id))
const taskDetail = ref<WfTaskVO | null>(null)
const graphData = ref<WfProcessGraphVO | null>(null)
const opinionList = ref<WfApprovalOpinionVO[]>([])
const rawCountersignInfo = ref<WfCountersignVO | null>(null)
const countersignInfo = ref<WfCountersignInfoVO | null>(null)
const availableNodes = ref<WfNode[]>([])

const isTodoTask = computed(() => {
  if (!taskDetail.value) return false
  return taskDetail.value.status === 'PENDING' || taskDetail.value.status === 'CLAIMED' || taskDetail.value.status === 'PROCESSING'
})

const isCountersignTask = computed(() => {
  if (!taskDetail.value) return false
  return taskDetail.value.taskType === 'COUNTERSIGN' || !!taskDetail.value.countersignId
})

const isCurrentUserAssignee = computed(() => {
  if (!taskDetail.value || !currentUserId.value) return false
  return taskDetail.value.assigneeId === currentUserId.value
})

const isCurrentUserPendingSign = computed(() => {
  if (!countersignInfo.value || !currentUserId.value) return false
  return countersignInfo.value.signers?.some?.(
    (s) => s.signerId === currentUserId.value && s.status === 'pending'
  )
})

const convertToCountersignInfoVO = (vo: WfCountersignVO): WfCountersignInfoVO => {
  return {
    nodeName: vo.nodeName,
    status: vo.status === 'PROCESSING' ? 'processing' : vo.status === 'COMPLETED' ? 'completed' : 'processing',
    typeName: vo.countersignTypeName,
    voteRuleName: vo.voteTypeName,
    voteRule: vo.voteType,
    percentage: vo.voteRate * 100,
    totalCount: vo.totalCount,
    signedCount: vo.signedCount,
    agreeCount: vo.agreeCount,
    opposeCount: vo.opposeCount,
    abstainCount: vo.abstainCount,
    signers: vo.items.map((item) => ({
      signerId: item.signUserId,
      signerName: item.signUserName,
      status: item.signResult ? 'signed' : 'pending',
      statusName: item.signResult ? '已签署' : '待签署',
      result: item.signResult?.toLowerCase(),
      resultName: item.signResultName,
      signTime: item.signTime,
      opinion: item.signOpinion
    }))
  }
}

const showApprovalPanel = computed(() => {
  return isTodoTask.value && !isCountersignTask.value
})

const showCountersignPanel = computed(() => {
  return isTodoTask.value && isCountersignTask.value
})

const availableActions = computed(() => {
  const actions: string[] = ['approve', 'reject']
  if (taskDetail.value?.allowReject === 1) {
    actions.push('back')
  }
  if (taskDetail.value?.allowDelegate === 1) {
    actions.push('transfer')
  }
  if (taskDetail.value?.allowAddSign === 1) {
    actions.push('addSign')
  }
  return actions
})

const getTimelineColor = (operationType: string) => {
  const colorMap: Record<string, string> = {
    START: 'blue',
    SUBMIT: 'cyan',
    AGREE: 'green',
    APPROVE: 'green',
    OPPOSE: 'red',
    REJECT: 'red',
    ABSTAIN: 'default',
    WITHDRAW: 'orange',
    DELEGATE: 'orange',
    TRANSFER: 'purple',
    ADD_SIGN: 'cyan',
    COUNTERSIGN: 'magenta',
    CC: 'gold',
    CANCEL: 'red',
    TERMINATE: 'red',
    SUSPEND: 'warning',
    RESUME: 'green',
    COMPLETE: 'green'
  }
  return colorMap[operationType] || 'blue'
}

const fetchTaskDetail = async () => {
  if (!taskId.value) return

  loading.value = true
  try {
    const res = await getTask(taskId.value.toString())
    if (res.code === 200) {
      taskDetail.value = res.data
      if (taskDetail.value?.countersignId) {
        fetchCountersignInfo(taskDetail.value.countersignId)
      }
    } else {
      message.error(res.message || '获取任务详情失败')
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    message.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

const fetchProcessGraph = async () => {
  if (!taskDetail.value?.processInstanceId) return

  try {
    const res = await getProcessInstanceGraph(taskDetail.value.processInstanceId.toString())
    if (res.code === 200) {
      graphData.value = res.data
      buildAvailableNodes()
    }
  } catch (error) {
    console.error('获取流程图失败:', error)
  }
}

const fetchOpinionList = async () => {
  if (!taskDetail.value?.processInstanceId) return

  try {
    const res = await getOpinionsByProcessInstanceId(taskDetail.value.processInstanceId.toString())
    if (res.code === 200) {
      opinionList.value = res.data
    }
  } catch (error) {
    console.error('获取审批意见失败:', error)
  }
}

const fetchCountersignInfo = async (countersignId: number) => {
  try {
    const res = await getCountersign(countersignId.toString())
    if (res.code === 200) {
      rawCountersignInfo.value = res.data
      countersignInfo.value = convertToCountersignInfoVO(res.data)
    }
  } catch (error) {
    console.error('获取会签信息失败:', error)
  }
}

const buildAvailableNodes = () => {
  if (!graphData.value) return

  availableNodes.value = graphData.value.nodes
    .filter((node) => node.status === 'COMPLETED' || node.status === 'ACTIVE')
    .map((node) => ({
      id: node.nodeId,
      name: node.nodeName,
      type: node.nodeType as any,
      x: node.positionX,
      y: node.positionY,
      width: node.width,
      height: node.height,
      config: {}
    }))
}

const handleNodeClick = (node: any) => {
  console.log('Node clicked:', node)
}

const handleApproval = async (data: WfApprovalData) => {
  try {
    const params: WfApprovalDTO = {
      taskId: Number(data.taskId),
      approvalType: data.action.toUpperCase(),
      approvalOpinion: data.opinion,
      approvalAttachment: data.attachmentIds.join(','),
      nextNodeId: data.targetNodeId || undefined,
      nextNodeName: data.targetNodeName || undefined,
      rejectNodeId: data.targetNodeId || undefined,
      rejectNodeName: data.targetNodeName || undefined,
      remark: ''
    }

    const res = await completeTask(params)
    if (res.code === 200) {
      message.success('审批提交成功')
      handleBack()
    } else {
      message.error(res.message || '审批提交失败')
    }
  } catch (error) {
    console.error('审批提交失败:', error)
    message.error('审批提交失败')
  }
}

const handleCountersign = async (data: WfCountersignData) => {
  try {
    const params: WfCountersignSignDTO = {
      countersignId: Number(data.countersignId),
      signResult: data.result.toUpperCase(),
      signOpinion: data.opinion,
      remark: ''
    }

    const res = await signCountersign(params)
    if (res.code === 200) {
      message.success('会签提交成功')
      await fetchTaskDetail()
      await fetchOpinionList()
      if (taskDetail.value?.countersignId) {
        await fetchCountersignInfo(taskDetail.value.countersignId)
      }
    } else {
      message.error(res.message || '会签提交失败')
    }
  } catch (error) {
    console.error('会签提交失败:', error)
    message.error('会签提交失败')
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  fetchTaskDetail().then(() => {
    fetchProcessGraph()
    fetchOpinionList()
  })
})
</script>

<style lang="less" scoped>
.task-detail-page {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;

  .page-header {
    margin-bottom: 24px;
    padding: 16px 24px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

    .breadcrumb {
      margin-bottom: 8px;
      :deep(.ant-breadcrumb-link) {
        cursor: pointer;
        &:hover {
          color: #1890ff;
        }
      }
    }

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #262626;
    }

    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-card,
  .graph-card,
  .history-card {
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
    margin-bottom: 16px;
  }

  .graph-card {
    :deep(.ant-card-body) {
      padding: 0;
    }
  }

  .history-card {
    .timeline-item {
      padding: 8px 0;

      .timeline-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .operator {
          font-weight: 500;
          color: #262626;
        }

        .dept {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      .timeline-node {
        font-size: 13px;
        color: #595959;
        margin-bottom: 4px;

        .node-label {
          color: #8c8c8c;
        }

        .node-name {
          font-weight: 500;
        }
      }

      .timeline-opinion {
        font-size: 13px;
        color: #595959;
        margin-bottom: 4px;
        background: #fafafa;
        padding: 8px 12px;
        border-radius: 4px;
        border-left: 3px solid #1890ff;

        .opinion-label {
          color: #8c8c8c;
        }

        .opinion-content {
          color: #262626;
        }
      }

      .timeline-attachment {
        margin-bottom: 4px;

        .attachment-label {
          color: #8c8c8c;
          font-size: 13px;
        }
      }

      .timeline-footer {
        display: flex;
        align-items: center;
        gap: 16px;
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;

        .time {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
}
</style>
