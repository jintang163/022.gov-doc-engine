import request from '@/utils/request'
import type {
  WfProcessDefinitionVO,
  WfProcessDefinitionSaveDTO,
  WfProcessDefinitionQueryDTO,
  WfProcessInstanceVO,
  WfProcessStartDTO,
  WfTaskVO,
  WfTaskQueryDTO,
  WfTaskDelegateDTO,
  WfTaskAddSignDTO,
  WfTaskCountVO,
  WfCountersignVO,
  WfCountersignItemVO,
  WfCountersignSignDTO,
  WfApprovalOpinionVO,
  WfOpinionTemplateVO,
  WfOpinionTemplateSaveDTO,
  WfOpinionTemplateQueryDTO,
  WfProcessGraphVO,
  WfApprovalDTO
} from '@/types/workflow'
import type { PageResult, Result } from '@/types/template'

// ==================== 流程定义管理接口 ====================

export const getProcessDefinitionPage = (params: WfProcessDefinitionQueryDTO) => {
  return request<PageResult<WfProcessDefinitionVO>>({
    url: '/wf/process-definition/page',
    method: 'get',
    params
  })
}

export const getProcessDefinition = (id: number) => {
  return request<WfProcessDefinitionVO>({
    url: `/wf/process-definition/${id}`,
    method: 'get'
  })
}

export const getProcessDefinitionByCode = (processCode: string) => {
  return request<WfProcessDefinitionVO>({
    url: `/wf/process-definition/code/${processCode}`,
    method: 'get'
  })
}

export const saveProcessDefinition = (data: WfProcessDefinitionSaveDTO) => {
  return request<number>({
    url: '/wf/process-definition',
    method: 'post',
    data
  })
}

export const updateProcessDefinition = (data: WfProcessDefinitionSaveDTO) => {
  return request<void>({
    url: '/wf/process-definition',
    method: 'put',
    data
  })
}

export const publishProcessDefinition = (id: number) => {
  return request<void>({
    url: `/wf/process-definition/publish/${id}`,
    method: 'post'
  })
}

export const suspendProcessDefinition = (id: number) => {
  return request<void>({
    url: `/wf/process-definition/suspend/${id}`,
    method: 'post'
  })
}

export const deleteProcessDefinition = (id: number) => {
  return request<void>({
    url: `/wf/process-definition/${id}`,
    method: 'delete'
  })
}

export const listPublishedProcessDefinitions = () => {
  return request<WfProcessDefinitionVO[]>({
    url: '/wf/process-definition/list/published',
    method: 'get'
  })
}

export const getProcessDefinitionGraph = (processDefId: number) => {
  return request<WfProcessGraphVO>({
    url: `/wf/process-definition/graph/${processDefId}`,
    method: 'get'
  })
}

export const deployProcessDefinition = (id: number) => {
  return request<void>({
    url: `/wf/process-definition/deploy/${id}`,
    method: 'post'
  })
}

// ==================== 流程实例管理接口 ====================

export const getProcessInstancePage = (params: { processDefId?: number; businessKey?: string; status?: string; keyword?: string; pageNum?: number; pageSize?: number }) => {
  return request<PageResult<WfProcessInstanceVO>>({
    url: '/wf/process-instance/page',
    method: 'get',
    params
  })
}

export const getProcessInstance = (id: number) => {
  return request<WfProcessInstanceVO>({
    url: `/wf/process-instance/${id}`,
    method: 'get'
  })
}

export const getProcessInstanceByBusinessKey = (businessKey: string) => {
  return request<WfProcessInstanceVO>({
    url: `/wf/process-instance/business/${businessKey}`,
    method: 'get'
  })
}

export const startProcessInstance = (data: WfProcessStartDTO) => {
  return request<number>({
    url: '/wf/process-instance/start',
    method: 'post',
    data
  })
}

export const suspendProcessInstance = (id: number) => {
  return request<void>({
    url: `/wf/process-instance/suspend/${id}`,
    method: 'post'
  })
}

export const activateProcessInstance = (id: number) => {
  return request<void>({
    url: `/wf/process-instance/activate/${id}`,
    method: 'post'
  })
}

export const terminateProcessInstance = (id: number, reason?: string) => {
  return request<void>({
    url: `/wf/process-instance/terminate/${id}`,
    method: 'post',
    data: { reason }
  })
}

export const getProcessInstanceGraph = (id: number) => {
  return request<WfProcessGraphVO>({
    url: `/wf/process-instance/graph/${id}`,
    method: 'get'
  })
}

// ==================== 任务管理接口 ====================

export const getTodoTaskPage = (params: WfTaskQueryDTO) => {
  return request<PageResult<WfTaskVO>>({
    url: '/wf/task/todo/page',
    method: 'get',
    params
  })
}

export const getDoneTaskPage = (params: WfTaskQueryDTO) => {
  return request<PageResult<WfTaskVO>>({
    url: '/wf/task/done/page',
    method: 'get',
    params
  })
}

export const getTaskPage = (params: WfTaskQueryDTO) => {
  return request<PageResult<WfTaskVO>>({
    url: '/wf/task/page',
    method: 'get',
    params
  })
}

export const getTask = (id: number) => {
  return request<WfTaskVO>({
    url: `/wf/task/${id}`,
    method: 'get'
  })
}

export const getTasksByProcessInstance = (processInstanceId: number) => {
  return request<WfTaskVO[]>({
    url: `/wf/task/process/${processInstanceId}`,
    method: 'get'
  })
}

export const claimTask = (id: number) => {
  return request<void>({
    url: `/wf/task/claim/${id}`,
    method: 'post'
  })
}

export const unclaimTask = (id: number) => {
  return request<void>({
    url: `/wf/task/unclaim/${id}`,
    method: 'post'
  })
}

export const completeTask = (data: WfApprovalDTO) => {
  return request<void>({
    url: '/wf/task/complete',
    method: 'post',
    data
  })
}

export const delegateTask = (data: WfTaskDelegateDTO) => {
  return request<void>({
    url: '/wf/task/delegate',
    method: 'post',
    data
  })
}

export const addSignTask = (data: WfTaskAddSignDTO) => {
  return request<void>({
    url: '/wf/task/add-sign',
    method: 'post',
    data
  })
}

export const getTaskCount = () => {
  return request<WfTaskCountVO>({
    url: '/wf/task/count',
    method: 'get'
  })
}

// ==================== 会签管理接口 ====================

export const getCountersign = (id: number) => {
  return request<WfCountersignVO>({
    url: `/wf/countersign/${id}`,
    method: 'get'
  })
}

export const getCountersignByProcessInstance = (processInstanceId: number) => {
  return request<WfCountersignVO[]>({
    url: `/wf/countersign/process/${processInstanceId}`,
    method: 'get'
  })
}

export const getCountersignItems = (countersignId: number) => {
  return request<WfCountersignItemVO[]>({
    url: `/wf/countersign/items/${countersignId}`,
    method: 'get'
  })
}

export const signCountersign = (data: WfCountersignSignDTO) => {
  return request<void>({
    url: '/wf/countersign/sign',
    method: 'post',
    data
  })
}

// ==================== 审批意见接口 ====================

export const getOpinionsByProcessInstance = (processInstanceId: number) => {
  return request<WfApprovalOpinionVO[]>({
    url: `/wf/approval-opinion/process/${processInstanceId}`,
    method: 'get'
  })
}

export const getOpinionsByTask = (taskId: number) => {
  return request<WfApprovalOpinionVO[]>({
    url: `/wf/approval-opinion/task/${taskId}`,
    method: 'get'
  })
}

// ==================== 意见模板接口 ====================

export const getOpinionTemplateList = (params: WfOpinionTemplateQueryDTO) => {
  return request<WfOpinionTemplateVO[]>({
    url: '/wf/opinion-template/list',
    method: 'get',
    params
  })
}

export const getOpinionTemplate = (id: number) => {
  return request<WfOpinionTemplateVO>({
    url: `/wf/opinion-template/${id}`,
    method: 'get'
  })
}

export const saveOpinionTemplate = (data: WfOpinionTemplateSaveDTO) => {
  return request<number>({
    url: '/wf/opinion-template',
    method: 'post',
    data
  })
}

export const updateOpinionTemplate = (data: WfOpinionTemplateSaveDTO) => {
  return request<void>({
    url: '/wf/opinion-template',
    method: 'put',
    data
  })
}

export const deleteOpinionTemplate = (id: number) => {
  return request<void>({
    url: `/wf/opinion-template/${id}`,
    method: 'delete'
  })
}

// ==================== 辅助函数 ====================

export const getProcessStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    running: '运行中',
    completed: '已完成',
    suspended: '已挂起',
    terminated: '已终止'
  }
  return map[status] || status
}

export const getProcessStatusColor = (status: string) => {
  const map: Record<string, string> = {
    running: 'processing',
    completed: 'success',
    suspended: 'warning',
    terminated: 'error'
  }
  return map[status] || 'default'
}

export const getTaskStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    delegated: '已转办',
    canceled: '已取消'
  }
  return map[status] || status
}

export const getTaskStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    processing: 'processing',
    completed: 'success',
    delegated: 'blue',
    canceled: 'default'
  }
  return map[status] || 'default'
}

export const getCountersignStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待开始',
    signing: '会签中',
    completed: '已完成',
    rejected: '已否决'
  }
  return map[status] || status
}

export const getCountersignStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'default',
    signing: 'processing',
    completed: 'success',
    rejected: 'error'
  }
  return map[status] || 'default'
}

export const getSignResultLabel = (result: string) => {
  const map: Record<string, string> = {
    agree: '同意',
    reject: '反对',
    abstain: '弃权'
  }
  return map[result] || result
}

export const getSignResultColor = (result: string) => {
  const map: Record<string, string> = {
    agree: 'success',
    reject: 'error',
    abstain: 'default'
  }
  return map[result] || 'default'
}

export const getApprovalTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    pass: '通过',
    reject: '驳回',
    return: '退回修改',
    terminate: '终止',
    delegate: '转办',
    addSign: '加签',
    countersign: '会签'
  }
  return map[type] || type
}

export const getApprovalTypeColor = (type: string) => {
  const map: Record<string, string> = {
    pass: 'success',
    reject: 'error',
    return: 'warning',
    terminate: 'red',
    delegate: 'blue',
    addSign: 'purple',
    countersign: 'geekblue'
  }
  return map[type] || 'default'
}

export const getNodeTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    start: '开始',
    end: '结束',
    userTask: '用户任务',
    countersign: '会签',
    parallelGateway: '并行网关',
    exclusiveGateway: '排他网关',
    inclusiveGateway: '包容网关'
  }
  return map[type] || type
}

export const getNodeTypeColor = (type: string) => {
  const map: Record<string, string> = {
    start: 'success',
    end: 'default',
    userTask: 'blue',
    countersign: 'purple',
    parallelGateway: 'orange',
    exclusiveGateway: 'cyan',
    inclusiveGateway: 'geekblue'
  }
  return map[type] || 'default'
}

export const getVoteTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    one_pass: '一票通过',
    all_pass: '全部通过',
    one_reject: '一票否决',
    percentage: '百分比通过'
  }
  return map[type] || type
}

export const getCountersignTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    parallel: '并行会签',
    sequential: '顺序会签'
  }
  return map[type] || type
}

export const getParticipantTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    user: '用户',
    post: '岗位',
    dept: '部门',
    role: '角色',
    expression: '表达式'
  }
  return map[type] || type
}

export const getPriorityLabel = (priority: number) => {
  const map: Record<number, string> = {
    0: '普通',
    1: '高',
    2: '紧急'
  }
  return map[priority] || '普通'
}

export const getPriorityColor = (priority: number) => {
  const map: Record<number, string> = {
    0: 'default',
    1: 'orange',
    2: 'red'
  }
  return map[priority] || 'default'
}

export const formatDuration = (duration: number) => {
  if (!duration || duration <= 0) return '-'
  const days = Math.floor(duration / (1000 * 60 * 60 * 24))
  const hours = Math.floor((duration % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))
  if (days > 0) {
    return `${days}天${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟`
  } else {
    return '少于1分钟'
  }
}
