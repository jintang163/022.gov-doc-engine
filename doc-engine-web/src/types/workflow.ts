export interface WfProcessDefinitionVO {
  id: number
  processCode: string
  processName: string
  processType: string
  processTypeName: string
  processCategory: string
  version: number
  isCurrentVersion: number
  status: number
  statusName: string
  description: string
  bpmnXml: string
  processGraph: string
  formKey: string
  unitCode: string
  unitName: string
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  nodes: WfProcessNodeDTO[]
  edges: WfProcessEdgeDTO[]
}

export interface WfProcessDefinitionSaveDTO {
  id?: number
  processCode: string
  processName: string
  processType: string
  processCategory?: string
  description?: string
  bpmnXml?: string
  processGraph?: string
  formKey?: string
  unitCode?: string
  unitName?: string
  remark?: string
  nodes?: WfProcessNodeDTO[]
  edges?: WfProcessEdgeDTO[]
  participants?: WfParticipantDTO[]
}

export interface WfProcessDefinitionQueryDTO {
  processCode?: string
  processName?: string
  processType?: string
  processCategory?: string
  status?: number
  unitCode?: string
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export interface WfProcessNodeDTO {
  id?: number
  nodeId: string
  nodeName: string
  nodeType: string
  nodeConfig?: Record<string, any>
  formProperties?: Record<string, any>[]
  x?: number
  y?: number
  width?: number
  height?: number
  sortOrder?: number
  remark?: string
  participants?: WfParticipantDTO[]
}

export interface WfProcessEdgeDTO {
  id?: number
  edgeId: string
  edgeName?: string
  sourceNodeId: string
  targetNodeId: string
  conditionExpression?: string
  conditionLabel?: string
  edgePoints?: Record<string, number>[]
  sortOrder?: number
  remark?: string
}

export interface WfParticipantDTO {
  id?: number
  nodeId?: string
  participantType: string
  participantValue: string
  participantName?: string
  assignmentType?: string
  sortOrder?: number
  remark?: string
}

export interface WfProcessInstanceVO {
  id: number
  instanceNo: string
  processDefId: number
  processCode: string
  processName: string
  businessKey: string
  businessType: string
  businessTitle: string
  status: string
  statusName: string
  startUserId: string
  startUserName: string
  startDeptId: string
  startDeptName: string
  startTime: string
  endTime: string
  duration: number
  durationText: string
  currentNodeId: string
  currentNodeName: string
  formData: Record<string, any>
  variables: Record<string, any>
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  currentTasks: WfTaskVO[]
  opinions: WfApprovalOpinionVO[]
  history: WfProcessHistoryVO[]
}

export interface WfProcessStartDTO {
  processDefId: number
  businessKey?: string
  businessType?: string
  businessTitle: string
  formData?: Record<string, any>
  variables?: Record<string, any>
  remark?: string
}

export interface WfTaskVO {
  id: number
  taskNo: string
  processInstanceId: number
  processDefId: number
  nodeId: string
  nodeName: string
  taskType: string
  taskTypeName: string
  businessKey: string
  businessType: string
  businessTitle: string
  status: string
  statusName: string
  assigneeType: string
  assigneeTypeName: string
  assigneeId: string
  assigneeName: string
  delegatedFromUserId: string
  delegatedFromUserName: string
  claimTime: string
  completeTime: string
  dueTime: string
  priority: number
  priorityName: string
  formData: Record<string, any>
  variables: Record<string, any>
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  processInstance: WfProcessInstanceVO
  countersign: WfCountersignVO
  canDelegate: boolean
  canAddSign: boolean
  canTerminate: boolean
}

export interface WfTaskQueryDTO {
  assigneeId?: string
  status?: string
  taskType?: string
  businessType?: string
  businessKey?: string
  processInstanceId?: number
  processDefId?: number
  nodeId?: string
  priority?: number
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export interface WfApprovalDTO {
  taskId: number
  approvalType: string
  approvalResult?: string
  approvalOpinion: string
  opinionTemplateId?: number
  attachmentIds?: number[]
  formData?: Record<string, any>
  variables?: Record<string, any>
  targetUserId?: string
  targetUserName?: string
  targetNodeId?: string
  targetNodeName?: string
  remark?: string
}

export interface WfTaskDelegateDTO {
  taskId: number
  targetUserId: string
  targetUserName: string
  delegateReason?: string
  remark?: string
}

export interface WfTaskAddSignDTO {
  taskId: number
  addSignType?: string
  signUsers: WfParticipantDTO[]
  addSignReason?: string
  remark?: string
}

export interface WfTaskCountVO {
  todoCount: number
  doneCount: number
  countersignCount: number
  urgentCount: number
  totalCount: number
}

export interface WfCountersignVO {
  id: number
  countersignNo: string
  processInstanceId: number
  processDefId: number
  nodeId: string
  nodeName: string
  taskId: number
  businessKey: string
  businessType: string
  businessTitle: string
  countersignType: string
  countersignTypeName: string
  voteType: string
  voteTypeName: string
  passPercentage?: number
  signOrder: Record<string, any>[]
  status: string
  statusName: string
  totalCount: number
  signedCount: number
  passedCount: number
  rejectedCount: number
  abstainedCount: number
  startTime: string
  endTime: string
  duration: number
  durationText: string
  variables: Record<string, any>
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  items: WfCountersignItemVO[]
  passRate: number
  canSign: boolean
  currentSignItemId: number
}

export interface WfCountersignItemVO {
  id: number
  countersignId: number
  processInstanceId: number
  taskId: number
  signUserId: string
  signUserName: string
  signUserDeptId: string
  signUserDeptName: string
  signType: string
  signTypeName: string
  signOrder: number
  status: string
  statusName: string
  signResult: string
  signResultName: string
  signOpinion: string
  signTime: string
  duration: number
  durationText: string
  signSequence: number
  variables: Record<string, any>
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

export interface WfCountersignSignDTO {
  countersignItemId: number
  signResult: string
  signOpinion: string
  attachmentIds?: number[]
  variables?: Record<string, any>
  remark?: string
}

export interface WfApprovalOpinionVO {
  id: number
  processInstanceId: number
  taskId: number
  nodeId: string
  nodeName: string
  businessKey: string
  approvalType: string
  approvalTypeName: string
  approvalResult: string
  approvalResultName: string
  approvalOpinion: string
  opinionTemplateId: number
  attachments: Record<string, any>[]
  approverId: string
  approverName: string
  approverDeptId: string
  approverDeptName: string
  targetUserId: string
  targetUserName: string
  targetNodeId: string
  targetNodeName: string
  approvalTime: string
  variables: Record<string, any>
  remark: string
  createBy: string
  createTime: string
}

export interface WfProcessHistoryVO {
  id: number
  processInstanceId: number
  nodeId: string
  nodeName: string
  nodeType: string
  nodeTypeName: string
  taskId: number
  operatorId: string
  operatorName: string
  operationType: string
  operationTypeName: string
  enterTime: string
  leaveTime: string
  duration: number
  durationText: string
  status: string
  statusName: string
  variables: Record<string, any>
  remark: string
  opinion: WfApprovalOpinionVO
}

export interface WfProcessGraphVO {
  processDefId: number
  processInstanceId?: number
  nodes: WfProcessGraphNode[]
  edges: WfProcessGraphEdge[]
}

export interface WfProcessGraphNode {
  id: string
  name: string
  type: string
  x: number
  y: number
  width: number
  height: number
  status: string
  statusName: string
  data?: Record<string, any>
  history?: Record<string, any>[]
}

export interface WfProcessGraphEdge {
  id: string
  name: string
  source: string
  target: string
  conditionLabel?: string
  points?: Record<string, number>[]
  status?: string
  statusName?: string
}

export interface WfOpinionTemplateVO {
  id: number
  templateType: string
  templateTypeName: string
  templateContent: string
  sortOrder: number
  isPreset: number
  isPresetName: string
  userId: string
  userName: string
  status: number
  statusName: string
  remark: string
  createTime: string
}

export interface WfOpinionTemplateSaveDTO {
  id?: number
  templateType: string
  templateContent: string
  sortOrder?: number
  remark?: string
}

export interface WfOpinionTemplateQueryDTO {
  templateType?: string
  userId?: string
  isPreset?: number
  status?: number
  keyword?: string
}

export const nodeTypeOptions = [
  { label: '开始', value: 'start', color: 'success' },
  { label: '结束', value: 'end', color: 'default' },
  { label: '用户任务', value: 'userTask', color: 'blue' },
  { label: '会签', value: 'countersign', color: 'purple' },
  { label: '并行网关', value: 'parallelGateway', color: 'orange' },
  { label: '排他网关', value: 'exclusiveGateway', color: 'cyan' },
  { label: '包容网关', value: 'inclusiveGateway', color: 'geekblue' }
]

export const processStatusOptions = [
  { label: '运行中', value: 'running', color: 'processing' },
  { label: '已完成', value: 'completed', color: 'success' },
  { label: '已挂起', value: 'suspended', color: 'warning' },
  { label: '已终止', value: 'terminated', color: 'error' }
]

export const taskStatusOptions = [
  { label: '待处理', value: 'pending', color: 'warning' },
  { label: '处理中', value: 'processing', color: 'processing' },
  { label: '已完成', value: 'completed', color: 'success' },
  { label: '已转办', value: 'delegated', color: 'blue' },
  { label: '已取消', value: 'canceled', color: 'default' }
]

export const taskTypeOptions = [
  { label: '用户任务', value: 'userTask', color: 'blue' },
  { label: '会签任务', value: 'countersign', color: 'purple' }
]

export const countersignTypeOptions = [
  { label: '并行会签', value: 'parallel', color: 'blue' },
  { label: '顺序会签', value: 'sequential', color: 'geekblue' }
]

export const voteTypeOptions = [
  { label: '一票通过', value: 'one_pass', color: 'success' },
  { label: '全部通过', value: 'all_pass', color: 'processing' },
  { label: '一票否决', value: 'one_reject', color: 'error' },
  { label: '百分比通过', value: 'percentage', color: 'orange' }
]

export const countersignStatusOptions = [
  { label: '待开始', value: 'pending', color: 'default' },
  { label: '会签中', value: 'signing', color: 'processing' },
  { label: '已完成', value: 'completed', color: 'success' },
  { label: '已否决', value: 'rejected', color: 'error' }
]

export const signResultOptions = [
  { label: '同意', value: 'agree', color: 'success' },
  { label: '反对', value: 'reject', color: 'error' },
  { label: '弃权', value: 'abstain', color: 'default' }
]

export const approvalTypeOptions = [
  { label: '通过', value: 'pass', color: 'success' },
  { label: '驳回', value: 'reject', color: 'error' },
  { label: '退回修改', value: 'return', color: 'warning' },
  { label: '终止', value: 'terminate', color: 'red' },
  { label: '转办', value: 'delegate', color: 'blue' },
  { label: '加签', value: 'addSign', color: 'purple' },
  { label: '会签', value: 'countersign', color: 'geekblue' }
]

export const operationTypeOptions = [
  { label: '到达', value: 'arrive', color: 'default' },
  { label: '完成', value: 'complete', color: 'success' },
  { label: '转办', value: 'delegate', color: 'blue' },
  { label: '加签', value: 'addSign', color: 'purple' },
  { label: '终止', value: 'terminate', color: 'error' }
]

export const participantTypeOptions = [
  { label: '用户', value: 'user', color: 'blue' },
  { label: '岗位', value: 'post', color: 'green' },
  { label: '部门', value: 'dept', color: 'orange' },
  { label: '角色', value: 'role', color: 'purple' },
  { label: '表达式', value: 'expression', color: 'cyan' }
]

export const priorityOptions = [
  { label: '普通', value: 0, color: 'default' },
  { label: '高', value: 1, color: 'orange' },
  { label: '紧急', value: 2, color: 'red' }
]

export const templateTypeOptions = [
  { label: '同意', value: 'pass', color: 'success' },
  { label: '驳回', value: 'reject', color: 'error' },
  { label: '通用', value: 'general', color: 'default' }
]

export type NodeType = 'start' | 'end' | 'userTask' | 'countersign' | 'parallelGateway' | 'exclusiveGateway' | 'inclusiveGateway'

export interface WfNodeConfig {
  participantType?: string
  participants?: WfParticipantDTO[]
  countersignType?: string
  voteType?: string
  passPercentage?: number
  allowDelegate?: boolean
  allowAddSign?: boolean
  allowReject?: boolean
  formFields?: string[]
}

export interface WfNode {
  id: string
  type: NodeType
  name: string
  x: number
  y: number
  width: number
  height: number
  config?: WfNodeConfig
}

export interface WfEdge {
  id: string
  source: string
  target: string
  label?: string
  condition?: string
  points?: { x: number; y: number }[]
}

export interface WfApprovalAction {
  action: string
  actionName: string
  type: string
  needTargetNode?: boolean
  needTargetUser?: boolean
}

export interface WfApprovalData {
  taskId: string
  businessKey: string
  action: string
  opinion: string
  attachmentIds: number[]
  targetNodeId?: string
  targetNodeName?: string
  targetUserId?: string
  targetUserName?: string
  addSignUsers?: WfParticipantDTO[]
}

export interface WfCountersignData {
  countersignId: string
  countersignItemId: string
  result: string
  opinion: string
  attachmentIds?: number[]
}

export const defaultApprovalActions: WfApprovalAction[] = [
  { action: 'pass', actionName: '通过', type: 'primary' },
  { action: 'reject', actionName: '驳回', type: 'danger' },
  { action: 'return', actionName: '退回修改', type: 'default', needTargetNode: true },
  { action: 'terminate', actionName: '终止', type: 'danger' },
  { action: 'delegate', actionName: '转办', type: 'default', needTargetUser: true },
  { action: 'addSign', actionName: '加签', type: 'default', needTargetUser: true }
]
