<template>
  <div class="process-designer">
    <div class="designer-toolbar">
      <a-space>
        <span class="toolbar-title">节点库</span>
        <div
          v-for="nodeType in nodeTypeOptions"
          :key="nodeType.value"
          class="node-item"
          draggable="true"
          @dragstart="handlePaletteDragStart($event, nodeType.value)"
        >
          <component :is="getNodeIcon(nodeType.icon)" />
          <span>{{ nodeType.label }}</span>
        </div>
      </a-space>
      <a-space>
        <a-button type="primary" @click="handleSave">
          <template #icon><save-outlined /></template>
          保存
        </a-button>
        <a-button @click="handleClear">
          <template #icon><delete-outlined /></template>
          清空
        </a-button>
      </a-space>
    </div>

    <div class="designer-body">
      <div
        class="canvas-container"
        ref="canvasRef"
        @dragover.prevent
        @drop="handleCanvasDrop"
        @click="handleCanvasClick"
        @mousedown="handleCanvasMouseDown"
        @mousemove="handleCanvasMouseMove"
        @mouseup="handleCanvasMouseUp"
      >
        <svg class="edge-layer">
          <defs>
            <marker
              id="arrowhead"
              markerWidth="10"
              markerHeight="7"
              refX="9"
              refY="3.5"
              orient="auto"
            >
              <polygon points="0 0, 10 3.5, 0 7" fill="#999" />
            </marker>
            <marker
              id="arrowhead-active"
              markerWidth="10"
              markerHeight="7"
              refX="9"
              refY="3.5"
              orient="auto"
            >
              <polygon points="0 0, 10 3.5, 0 7" fill="#1890ff" />
            </marker>
          </defs>

          <template v-for="edge in edges" :key="edge.id">
            <path
              :d="getEdgePath(edge)"
              :class="['edge-path', { active: selectedEdgeId === edge.id }]"
              stroke="#999"
              stroke-width="2"
              fill="none"
              marker-end="url(#arrowhead)"
              @click.stop="handleEdgeClick(edge.id)"
            />
            <text
              v-if="edge.label"
              :x="getEdgeLabelPosition(edge).x"
              :y="getEdgeLabelPosition(edge).y"
              class="edge-label"
            >
              {{ edge.label }}
            </text>
          </template>

          <path
            v-if="connectingEdge"
            :d="getTempEdgePath()"
            stroke="#1890ff"
            stroke-width="2"
            stroke-dasharray="5,5"
            fill="none"
          />
        </svg>

        <div
          v-for="node in nodes"
          :key="node.id"
          :class="['node', node.type, { selected: selectedNodeId === node.id }]"
          :style="{
            left: node.x + 'px',
            top: node.y + 'px',
            width: node.width + 'px',
            height: node.height + 'px'
          }"
          @click.stop="handleNodeClick(node.id)"
          @mousedown.stop="handleNodeMouseDown($event, node.id)"
        >
          <div class="node-content">
            <component :is="getNodeIconByType(node.type)" class="node-icon" />
            <span class="node-name">{{ node.name }}</span>
          </div>
          <div
            v-if="selectedNodeId === node.id"
            class="connect-point"
            @mousedown.stop="handleConnectStart($event, node.id)"
          />
          <div
            v-if="selectedNodeId === node.id && node.type !== 'start' && node.type !== 'end'"
            class="delete-btn"
            @click.stop="handleDeleteNode(node.id)"
          >
            <close-outlined />
          </div>
        </div>
      </div>

      <div class="property-panel" v-if="selectedNode">
        <div class="panel-header">
          <span>节点属性</span>
          <a-button type="text" size="small" @click="selectedNodeId = null">
            <close-outlined />
          </a-button>
        </div>
        <div class="panel-body">
          <a-form layout="vertical">
            <a-form-item label="节点名称">
              <a-input v-model:value="selectedNode.name" placeholder="请输入节点名称" />
            </a-form-item>

            <template v-if="selectedNode.type === 'userTask'">
              <a-form-item label="审批人类型">
                <a-select v-model:value="selectedNode.config.assigneeType" placeholder="请选择审批人类型">
                  <a-select-option value="user">指定用户</a-select-option>
                  <a-select-option value="role">指定角色</a-select-option>
                  <a-select-option value="dept">指定部门</a-select-option>
                  <a-select-option value="post">指定岗位</a-select-option>
                  <a-select-option value="expression">表达式</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item v-if="selectedNode.config.assigneeType === 'expression'" label="审批表达式">
                <a-input v-model:value="selectedNode.config.assignee" placeholder="请输入表达式" />
              </a-form-item>
              <a-form-item v-else label="审批人">
                <ParticipantSelector
                  v-model:modelValue="selectedNode.config.participants"
                  :types="[selectedNode.config.assigneeType || 'user']"
                  :multiple="true"
                />
              </a-form-item>
            </template>

            <template v-if="selectedNode.type === 'countersign'">
              <a-form-item label="会签规则">
                <a-select v-model:value="selectedNode.config.countersignRule" placeholder="请选择会签规则">
                  <a-select-option v-for="rule in countersignRuleOptions" :key="rule.value" :value="rule.value">
                    {{ rule.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item v-if="selectedNode.config.countersignRule === 'percentage'" label="通过百分比">
                <a-input-number v-model:value="selectedNode.config.countersignPercentage" :min="1" :max="100" style="width: 100%" />
              </a-form-item>
              <a-form-item v-if="selectedNode.config.countersignRule === 'amount'" label="通过人数">
                <a-input-number v-model:value="selectedNode.config.countersignAmount" :min="1" style="width: 100%" />
              </a-form-item>
              <a-form-item label="会签人员">
                <ParticipantSelector
                  v-model:modelValue="selectedNode.config.participants"
                  :multiple="true"
                />
              </a-form-item>
            </template>

            <template v-if="selectedNode.type === 'exclusiveGateway'">
              <a-form-item label="条件表达式">
                <a-textarea v-model:value="selectedNode.config.condition" placeholder="请输入条件表达式" :rows="3" />
              </a-form-item>
            </template>
          </a-form>
        </div>
      </div>
    </div>

    <a-modal
      v-model:open="deleteConfirmVisible"
      title="确认删除"
      @ok="confirmDelete"
      @cancel="deleteConfirmVisible = false"
    >
      <p>确定要删除该节点吗？相关的连线也会被删除。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { WfNode, WfEdge, NodeType, WfNodeConfig } from '@/types/workflow'
import { nodeTypeOptions, countersignRuleOptions } from '@/types/workflow'
import {
  PlayCircleOutlined,
  StopOutlined,
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  GatewayOutlined,
  SaveOutlined,
  DeleteOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import ParticipantSelector from './ParticipantSelector.vue'

interface Props {
  initialNodes?: WfNode[]
  initialEdges?: WfEdge[]
}

interface Emits {
  (e: 'save', data: { nodes: WfNode[]; edges: WfEdge[] }): void
}

const props = withDefaults(defineProps<Props>(), {
  initialNodes: () => [],
  initialEdges: () => []
})

const emit = defineEmits<Emits>()

const canvasRef = ref<HTMLDivElement | null>(null)
const nodes = ref<WfNode[]>([])
const edges = ref<WfEdge[]>([])
const selectedNodeId = ref<string | null>(null)
const selectedEdgeId = ref<string | null>(null)
const deleteConfirmVisible = ref(false)
const nodeToDelete = ref<string | null>(null)

const draggingNodeId = ref<string | null>(null)
const dragOffset = ref({ x: 0, y: 0 })
const connectingEdge = ref<{ sourceId: string; startX: number; startY: number; endX: number; endY: number } | null>(null)
const isPanning = ref(false)
const panOffset = ref({ x: 0, y: 0 })
const lastMousePos = ref({ x: 0, y: 0 })

const selectedNode = computed(() => {
  return nodes.value.find(n => n.id === selectedNodeId.value) || null
})

const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 9)
}

const getNodeIcon = (icon: string) => {
  const iconMap: Record<string, any> = {
    'play-circle': PlayCircleOutlined,
    'stop': StopOutlined,
    'user': UserOutlined,
    'team': TeamOutlined,
    'apartment': ApartmentOutlined,
    'gateway': GatewayOutlined
  }
  return iconMap[icon] || UserOutlined
}

const getNodeIconByType = (type: NodeType) => {
  const iconMap: Record<NodeType, any> = {
    'start': PlayCircleOutlined,
    'end': StopOutlined,
    'userTask': UserOutlined,
    'countersign': TeamOutlined,
    'parallelGateway': ApartmentOutlined,
    'exclusiveGateway': GatewayOutlined
  }
  return iconMap[type] || UserOutlined
}

const getNodeDefaultConfig = (type: NodeType): WfNodeConfig => {
  const configs: Record<NodeType, WfNodeConfig> = {
    'start': {},
    'end': {},
    'userTask': {
      assigneeType: 'user',
      participants: []
    },
    'countersign': {
      countersignRule: 'all',
      participants: []
    },
    'parallelGateway': {},
    'exclusiveGateway': {
      condition: ''
    }
  }
  return configs[type] || {}
}

const getNodeDefaultSize = (type: NodeType) => {
  const sizes: Record<NodeType, { width: number; height: number }> = {
    'start': { width: 80, height: 80 },
    'end': { width: 80, height: 80 },
    'userTask': { width: 120, height: 60 },
    'countersign': { width: 120, height: 60 },
    'parallelGateway': { width: 60, height: 60 },
    'exclusiveGateway': { width: 60, height: 60 }
  }
  return sizes[type] || { width: 100, height: 60 }
}

const getNodeDefaultName = (type: NodeType): string => {
  const names: Record<NodeType, string> = {
    'start': '开始',
    'end': '结束',
    'userTask': '用户任务',
    'countersign': '会签',
    'parallelGateway': '并行网关',
    'exclusiveGateway': '排他网关'
  }
  return names[type] || '节点'
}

const handlePaletteDragStart = (e: DragEvent, type: NodeType) => {
  e.dataTransfer?.setData('nodeType', type)
}

const handleCanvasDrop = (e: DragEvent) => {
  const nodeType = e.dataTransfer?.getData('nodeType') as NodeType
  if (!nodeType || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left - 50
  const y = e.clientY - rect.top - 30

  const size = getNodeDefaultSize(nodeType)
  const newNode: WfNode = {
    id: generateId(),
    type: nodeType,
    name: getNodeDefaultName(nodeType),
    x,
    y,
    width: size.width,
    height: size.height,
    config: getNodeDefaultConfig(nodeType)
  }

  nodes.value.push(newNode)
  selectedNodeId.value = newNode.id
  selectedEdgeId.value = null
}

const handleCanvasClick = () => {
  selectedNodeId.value = null
  selectedEdgeId.value = null
}

const handleCanvasMouseDown = (e: MouseEvent) => {
  if (e.button === 0 && (e.target as HTMLElement).classList.contains('canvas-container')) {
    isPanning.value = true
    lastMousePos.value = { x: e.clientX, y: e.clientY }
  }
}

const handleCanvasMouseMove = (e: MouseEvent) => {
  if (draggingNodeId.value) {
    const node = nodes.value.find(n => n.id === draggingNodeId.value)
    if (node && canvasRef.value) {
      const rect = canvasRef.value.getBoundingClientRect()
      node.x = e.clientX - rect.left - dragOffset.value.x
      node.y = e.clientY - rect.top - dragOffset.value.y
    }
  }

  if (connectingEdge.value) {
    const rect = canvasRef.value?.getBoundingClientRect()
    if (rect) {
      connectingEdge.value.endX = e.clientX - rect.left
      connectingEdge.value.endY = e.clientY - rect.top
    }
  }

  if (isPanning.value && canvasRef.value) {
    const dx = e.clientX - lastMousePos.value.x
    const dy = e.clientY - lastMousePos.value.y
    panOffset.value.x += dx
    panOffset.value.y += dy
    lastMousePos.value = { x: e.clientX, y: e.clientY }
  }
}

const handleCanvasMouseUp = (e: MouseEvent) => {
  if (connectingEdge.value && canvasRef.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    const mouseX = e.clientX - rect.left
    const mouseY = e.clientY - rect.top

    const targetNode = nodes.value.find(n => {
      return mouseX >= n.x && mouseX <= n.x + n.width &&
             mouseY >= n.y && mouseY <= n.y + n.height &&
             n.id !== connectingEdge.value!.sourceId
    })

    if (targetNode) {
      const existingEdge = edges.value.find(
        ed => ed.source === connectingEdge.value!.sourceId && ed.target === targetNode.id
      )

      if (!existingEdge) {
        const newEdge: WfEdge = {
          id: generateId(),
          source: connectingEdge.value.sourceId,
          target: targetNode.id
        }
        edges.value.push(newEdge)
      } else {
        message.warning('该连线已存在')
      }
    }
  }

  draggingNodeId.value = null
  connectingEdge.value = null
  isPanning.value = false
}

const handleNodeClick = (nodeId: string) => {
  selectedNodeId.value = nodeId
  selectedEdgeId.value = null
}

const handleNodeMouseDown = (e: MouseEvent, nodeId: string) => {
  const node = nodes.value.find(n => n.id === nodeId)
  if (!node || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  dragOffset.value = {
    x: e.clientX - rect.left - node.x,
    y: e.clientY - rect.top - node.y
  }
  draggingNodeId.value = nodeId
}

const handleConnectStart = (e: MouseEvent, nodeId: string) => {
  const node = nodes.value.find(n => n.id === nodeId)
  if (!node || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  connectingEdge.value = {
    sourceId: nodeId,
    startX: node.x + node.width,
    startY: node.y + node.height / 2,
    endX: e.clientX - rect.left,
    endY: e.clientY - rect.top
  }
}

const handleEdgeClick = (edgeId: string) => {
  selectedEdgeId.value = edgeId
  selectedNodeId.value = null

  Modal.confirm({
    title: '删除连线',
    content: '确定要删除这条连线吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      const idx = edges.value.findIndex(ed => ed.id === edgeId)
      if (idx !== -1) {
        edges.value.splice(idx, 1)
        selectedEdgeId.value = null
        message.success('删除成功')
      }
    }
  })
}

const handleDeleteNode = (nodeId: string) => {
  nodeToDelete.value = nodeId
  deleteConfirmVisible.value = true
}

const confirmDelete = () => {
  if (!nodeToDelete.value) return

  const idx = nodes.value.findIndex(n => n.id === nodeToDelete.value)
  if (idx !== -1) {
    nodes.value.splice(idx, 1)
    edges.value = edges.value.filter(
      ed => ed.source !== nodeToDelete.value && ed.target !== nodeToDelete.value
    )
    selectedNodeId.value = null
    message.success('删除成功')
  }

  deleteConfirmVisible.value = false
  nodeToDelete.value = null
}

const getNodeCenter = (node: WfNode) => {
  return {
    x: node.x + node.width / 2,
    y: node.y + node.height / 2
  }
}

const getEdgePath = (edge: WfEdge): string => {
  const sourceNode = nodes.value.find(n => n.id === edge.source)
  const targetNode = nodes.value.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return ''

  const startX = sourceNode.x + sourceNode.width
  const startY = sourceNode.y + sourceNode.height / 2
  const endX = targetNode.x
  const endY = targetNode.y + targetNode.height / 2

  const midX = (startX + endX) / 2

  return `M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX - 10} ${endY}`
}

const getEdgeLabelPosition = (edge: WfEdge): { x: number; y: number } => {
  const sourceNode = nodes.value.find(n => n.id === edge.source)
  const targetNode = nodes.value.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return { x: 0, y: 0 }

  return {
    x: (sourceNode.x + sourceNode.width + targetNode.x) / 2,
    y: (sourceNode.y + sourceNode.height / 2 + targetNode.y + targetNode.height / 2) / 2 - 5
  }
}

const getTempEdgePath = (): string => {
  if (!connectingEdge.value) return ''
  const { startX, startY, endX, endY } = connectingEdge.value
  const midX = (startX + endX) / 2
  return `M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX} ${endY}`
}

const handleSave = () => {
  emit('save', {
    nodes: JSON.parse(JSON.stringify(nodes.value)),
    edges: JSON.parse(JSON.stringify(edges.value))
  })
  message.success('保存成功')
}

const handleClear = () => {
  Modal.confirm({
    title: '清空画布',
    content: '确定要清空画布吗？所有节点和连线都将被删除。',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      nodes.value = []
      edges.value = []
      selectedNodeId.value = null
      selectedEdgeId.value = null
      message.success('已清空')
    }
  })
}

const initDefaultFlow = () => {
  if (nodes.value.length === 0) {
    const startNode: WfNode = {
      id: generateId(),
      type: 'start',
      name: '开始',
      x: 50,
      y: 200,
      width: 80,
      height: 80,
      config: {}
    }

    const endNode: WfNode = {
      id: generateId(),
      type: 'end',
      name: '结束',
      x: 500,
      y: 200,
      width: 80,
      height: 80,
      config: {}
    }

    nodes.value = [startNode, endNode]
  }
}

watch(() => props.initialNodes, (val) => {
  if (val && val.length > 0) {
    nodes.value = JSON.parse(JSON.stringify(val))
  }
}, { immediate: true })

watch(() => props.initialEdges, (val) => {
  if (val && val.length > 0) {
    edges.value = JSON.parse(JSON.stringify(val))
  }
}, { immediate: true })

onMounted(() => {
  if (props.initialNodes?.length === 0) {
    initDefaultFlow()
  }
})
</script>

<style scoped lang="less">
.process-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;

  .designer-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;

    .toolbar-title {
      font-weight: 500;
      color: #333;
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

  .designer-body {
    display: flex;
    flex: 1;
    overflow: hidden;

    .canvas-container {
      flex: 1;
      position: relative;
      background:
        linear-gradient(#f0f0f0 1px, transparent 1px),
        linear-gradient(90deg, #f0f0f0 1px, transparent 1px);
      background-size: 20px 20px;
      overflow: hidden;
      cursor: default;

      .edge-layer {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;

        .edge-path {
          pointer-events: stroke;
          cursor: pointer;
          transition: stroke 0.3s;

          &:hover {
            stroke: #1890ff;
            stroke-width: 3;
          }

          &.active {
            stroke: #1890ff;
            stroke-width: 3;
          }
        }

        .edge-label {
          font-size: 12px;
          fill: #666;
          text-anchor: middle;
        }
      }

      .node {
        position: absolute;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        border: 2px solid #d9d9d9;
        border-radius: 8px;
        cursor: move;
        transition: all 0.3s;
        user-select: none;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
        }

        &.selected {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
        }

        &.start, &.end {
          border-radius: 50%;
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          color: #fff;
          border-color: #389e0d;

          .node-name {
            color: #fff;
          }
        }

        &.end {
          background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
          border-color: #cf1322;
        }

        &.parallelGateway, &.exclusiveGateway {
          transform: rotate(45deg);
          border-radius: 4px;

          .node-content {
            transform: rotate(-45deg);
          }
        }

        &.countersign {
          border-style: dashed;
        }

        .node-content {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;

          .node-icon {
            font-size: 16px;
          }

          .node-name {
            font-size: 12px;
            color: #333;
            text-align: center;
            max-width: 100px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .connect-point {
          position: absolute;
          right: -6px;
          top: 50%;
          transform: translateY(-50%);
          width: 12px;
          height: 12px;
          background: #1890ff;
          border: 2px solid #fff;
          border-radius: 50%;
          cursor: crosshair;
          z-index: 10;

          &:hover {
            transform: translateY(-50%) scale(1.2);
          }
        }

        .delete-btn {
          position: absolute;
          top: -8px;
          right: -8px;
          width: 18px;
          height: 18px;
          background: #ff4d4f;
          color: #fff;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 10px;
          cursor: pointer;
          z-index: 10;

          &:hover {
            background: #ff7875;
          }
        }
      }
    }

    .property-panel {
      width: 320px;
      border-left: 1px solid #e8e8e8;
      background: #fff;
      display: flex;
      flex-direction: column;

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #e8e8e8;
        font-weight: 500;
      }

      .panel-body {
        flex: 1;
        padding: 16px;
        overflow-y: auto;
      }
    }
  }
}
</style>
