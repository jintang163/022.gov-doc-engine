<template>
  <div class="workflow-design">
    <a-card :bordered="false">
      <template #title>
        <a-breadcrumb class="breadcrumb">
          <a-breadcrumb-item @click="handleBack" class="breadcrumb-item">
            流程定义管理
          </a-breadcrumb-item>
          <a-breadcrumb-item>{{ pageTitle }}</a-breadcrumb-item>
        </a-breadcrumb>
      </template>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
        class="basic-info-form"
      >
        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="流程编码" name="processKey">
              <a-input
                v-model:value="formData.processKey"
                placeholder="请输入流程编码"
                :disabled="isEdit"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="流程名称" name="processName">
              <a-input
                v-model:value="formData.processName"
                placeholder="请输入流程名称"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="流程分类" name="processCategory">
              <a-select
                v-model:value="formData.processCategory"
                placeholder="请选择流程分类"
                :options="processCategoryOptions"
                allow-clear
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="designer-container">
        <div class="toolbar">
          <a-space>
            <span class="toolbar-title">节点库</span>
            <div
              v-for="nodeType in nodeTypeOptions"
              :key="nodeType.value"
              class="node-item"
              draggable="true"
              @dragstart="handlePaletteDragStart($event, nodeType.value)"
            >
              <component :is="getNodeIcon(nodeType.value)" />
              <span>{{ nodeType.label }}</span>
            </div>
          </a-space>
          <a-space>
            <a-button type="primary" @click="handleSave">
              <template #icon><SaveOutlined /></template>
              保存
            </a-button>
            <a-button type="primary" :disabled="!isEdit" @click="handlePublish">
              <template #icon><RocketOutlined /></template>
              发布
            </a-button>
            <a-button @click="handleClear">
              <template #icon><DeleteOutlined /></template>
              清空
            </a-button>
            <a-button-group>
              <a-button @click="handleZoomOut">
                <template #icon><ZoomOutOutlined /></template>
                缩小
              </a-button>
              <a-button>{{ zoomLevel }}%</a-button>
              <a-button @click="handleZoomIn">
                <template #icon><ZoomInOutlined /></template>
                放大
              </a-button>
            </a-button-group>
            <a-button @click="handleZoomReset">
              <template #icon><FullscreenOutlined /></template>
              重置
            </a-button>
          </a-space>
        </div>

        <div class="designer-content">
          <ProcessDesigner
            ref="designerRef"
            :initial-nodes="initialNodes"
            :initial-edges="initialEdges"
            @save="handleDesignerSave"
          />
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  SaveOutlined,
  RocketOutlined,
  DeleteOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  FullscreenOutlined,
  PlayCircleOutlined,
  StopOutlined,
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  GatewayOutlined
} from '@ant-design/icons-vue'
import ProcessDesigner from '@/components/ProcessDesigner.vue'
import {
  getProcessDefinition,
  saveProcessDefinition,
  updateProcessDefinition,
  publishProcessDefinition
} from '@/api/workflow'
import type {
  WfProcessDefinitionVO,
  WfProcessDefinitionSaveDTO,
  WfProcessNodeDTO,
  WfProcessEdgeDTO
} from '@/types/workflow'
import type { WfNode, WfEdge, NodeType } from '@/types/workflow'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const designerRef = ref<InstanceType<typeof ProcessDesigner> | null>(null)
const saving = ref(false)
const loading = ref(false)
const zoomLevel = ref(100)

const isEdit = computed(() => !!route.params.id)
const processDefinitionId = computed(() => Number(route.params.id) || 0)

const pageTitle = computed(() => (isEdit.value ? '编辑流程' : '新建流程'))

const initialNodes = ref<WfNode[]>([])
const initialEdges = ref<WfEdge[]>([])

const processCategoryOptions = [
  { label: '审批流程', value: 'APPROVAL' },
  { label: '会签流程', value: 'COUNTERSIGN' },
  { label: '公文流转', value: 'DOCUMENT' },
  { label: '请假流程', value: 'LEAVE' },
  { label: '报销流程', value: 'EXPENSE' }
]

const nodeTypeOptions = [
  { label: '开始', value: 'start', icon: 'play-circle' },
  { label: '结束', value: 'end', icon: 'stop' },
  { label: '用户任务', value: 'userTask', icon: 'user' },
  { label: '会签', value: 'countersign', icon: 'team' },
  { label: '并行网关', value: 'parallelGateway', icon: 'apartment' },
  { label: '排他网关', value: 'exclusiveGateway', icon: 'gateway' }
]

const formData = reactive<WfProcessDefinitionSaveDTO>({
  processKey: '',
  processName: '',
  processCategory: undefined,
  version: 1,
  description: '',
  nodes: [],
  edges: []
})

const rules = {
  processKey: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  processName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }]
}

const getNodeIcon = (type: NodeType) => {
  const iconMap: Record<NodeType, any> = {
    start: PlayCircleOutlined,
    end: StopOutlined,
    userTask: UserOutlined,
    countersign: TeamOutlined,
    parallelGateway: ApartmentOutlined,
    exclusiveGateway: GatewayOutlined
  }
  return iconMap[type] || UserOutlined
}

const handlePaletteDragStart = (e: DragEvent, type: NodeType) => {
  e.dataTransfer?.setData('nodeType', type)
}

const handleBack = () => {
  router.push('/workflow')
}

const handleZoomIn = () => {
  if (zoomLevel.value < 200) {
    zoomLevel.value += 10
  }
}

const handleZoomOut = () => {
  if (zoomLevel.value > 50) {
    zoomLevel.value -= 10
  }
}

const handleZoomReset = () => {
  zoomLevel.value = 100
}

const handleClear = () => {
  Modal.confirm({
    title: '清空画布',
    content: '确定要清空画布吗？所有节点和连线都将被删除。',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      initialNodes.value = []
      initialEdges.value = []
      message.success('已清空')
    }
  })
}

const handleDesignerSave = (data: { nodes: WfNode[]; edges: WfEdge[] }) => {
  formData.nodes = convertToProcessNodeDTO(data.nodes)
  formData.edges = convertToProcessEdgeDTO(data.edges)
}

const convertToProcessNodeDTO = (nodes: WfNode[]): WfProcessNodeDTO[] => {
  return nodes.map(node => ({
    nodeId: node.id,
    nodeName: node.name,
    nodeType: node.type.toUpperCase(),
    participantType: node.config?.assigneeType || 'USER',
    participantConfig: node.config ? JSON.stringify(node.config) : undefined,
    participants: node.config?.participants || [],
    positionX: node.x,
    positionY: node.y,
    width: node.width,
    height: node.height
  }))
}

const convertToProcessEdgeDTO = (edges: WfEdge[]): WfProcessEdgeDTO[] => {
  return edges.map(edge => ({
    edgeId: edge.id,
    edgeName: edge.label,
    sourceNodeId: edge.source,
    targetNodeId: edge.target,
    conditionExpression: edge.condition
  }))
}

const convertToWfNode = (nodes: WfProcessNodeDTO[]): WfNode[] => {
  return nodes.map(node => {
    let config: any = {}
    if (node.participantConfig) {
      try {
        config = JSON.parse(node.participantConfig)
      } catch (e) {
        console.error('解析节点配置失败', e)
      }
    }
    if (node.participants && node.participants.length > 0) {
      config.participants = node.participants
    }
    return {
      id: node.nodeId,
      type: node.nodeType.toLowerCase() as NodeType,
      name: node.nodeName,
      x: node.positionX || 0,
      y: node.positionY || 0,
      width: node.width || 120,
      height: node.height || 60,
      config
    }
  })
}

const convertToWfEdge = (edges: WfProcessEdgeDTO[]): WfEdge[] => {
  return edges.map(edge => ({
    id: edge.edgeId,
    source: edge.sourceNodeId,
    target: edge.targetNodeId,
    label: edge.edgeName,
    condition: edge.conditionExpression
  }))
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (designerRef.value) {
      const designerData = (designerRef.value as any).getData?.()
      if (designerData) {
        formData.nodes = convertToProcessNodeDTO(designerData.nodes)
        formData.edges = convertToProcessEdgeDTO(designerData.edges)
      }
    }

    if (isEdit.value) {
      formData.id = processDefinitionId.value
      const res = await updateProcessDefinition(formData)
      if (res.code === 200) {
        message.success('更新成功')
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await saveProcessDefinition(formData)
      if (res.code === 200) {
        message.success('保存成功')
        const newId = res.data
        router.push(`/workflow/design/${newId}`)
      } else {
        message.error(res.message || '保存失败')
      }
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    saving.value = false
  }
}

const handlePublish = async () => {
  if (!isEdit.value) {
    message.warning('请先保存流程后再发布')
    return
  }

  Modal.confirm({
    title: '发布流程',
    content: '确定要发布该流程吗？发布后将无法修改。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        saving.value = true
        const res = await publishProcessDefinition(processDefinitionId.value)
        if (res.code === 200) {
          message.success('发布成功')
        } else {
          message.error(res.message || '发布失败')
        }
      } catch (error) {
        message.error('发布失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const loadProcessDefinition = async (id: number) => {
  loading.value = true
  try {
    const res = await getProcessDefinition(id)
    if (res.code === 200 && res.data) {
      const data = res.data as WfProcessDefinitionVO
      Object.assign(formData, {
        id: data.id,
        processKey: data.processKey,
        processName: data.processName,
        processCategory: data.processCategory,
        version: data.version,
        description: data.description,
        nodes: data.nodes || [],
        edges: data.edges || []
      })

      if (data.nodes && data.nodes.length > 0) {
        initialNodes.value = convertToWfNode(data.nodes)
      }
      if (data.edges && data.edges.length > 0) {
        initialEdges.value = convertToWfEdge(data.edges)
      }
    } else {
      message.error(res.message || '获取流程定义失败')
    }
  } catch (error) {
    message.error('获取流程定义失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit.value && processDefinitionId.value) {
    loadProcessDefinition(processDefinitionId.value)
  }
})
</script>

<style scoped lang="less">
.workflow-design {
  padding: 16px;

  .breadcrumb {
    cursor: pointer;

    .breadcrumb-item {
      cursor: pointer;
      color: #1890ff;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .basic-info-form {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .designer-container {
    height: calc(100vh - 380px);
    min-height: 500px;
    display: flex;
    flex-direction: column;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #fafafa;
      border-bottom: 1px solid #e8e8e8;

      .toolbar-title {
        font-weight: 500;
        color: #333;
        margin-right: 8px;
      }

      .node-item {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 12px;
        background: #fff;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        cursor: grab;
        transition: all 0.3s;

        &:hover {
          border-color: #1890ff;
          color: #1890ff;
        }

        &:active {
          cursor: grabbing;
        }
      }
    }

    .designer-content {
      flex: 1;
      overflow: hidden;
    }
  }
}
</style>
