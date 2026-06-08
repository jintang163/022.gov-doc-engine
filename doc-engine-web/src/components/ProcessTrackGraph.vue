<template>
  <div class="process-track-graph" ref="graphContainerRef">
    <div class="graph-toolbar">
      <a-space>
        <a-button size="small" @click="handleZoomIn">
          <template #icon><zoom-in-outlined /></template>
          放大
        </a-button>
        <a-button size="small" @click="handleZoomOut">
          <template #icon><zoom-out-outlined /></template>
          缩小
        </a-button>
        <a-button size="small" @click="handleReset">
          <template #icon><reload-outlined /></template>
          重置
        </a-button>
      </a-space>
      <a-space>
        <span class="zoom-info">缩放：{{ Math.round(scale * 100) }}%</span>
      </a-space>
    </div>

    <div
      class="graph-container"
      ref="svgContainerRef"
      @wheel.prevent="handleWheel"
      @mousedown="handlePanStart"
      @mousemove="handlePanMove"
      @mouseup="handlePanEnd"
      @mouseleave="handlePanEnd"
    >
      <svg
        class="graph-svg"
        :style="{
          transform: `scale(${scale}) translate(${translate.x / scale}px, ${translate.y / scale}px)`,
          transformOrigin: '0 0'
        }"
      >
        <defs>
          <marker
            id="track-arrowhead"
            markerWidth="10"
            markerHeight="7"
            refX="9"
            refY="3.5"
            orient="auto"
          >
            <polygon points="0 0, 10 3.5, 0 7" fill="#bfbfbf" />
          </marker>
          <marker
            id="track-arrowhead-active"
            markerWidth="10"
            markerHeight="7"
            refX="9"
            refY="3.5"
            orient="auto"
          >
            <polygon points="0 0, 10 3.5, 0 7" fill="#1890ff" />
          </marker>
          <filter id="glow">
            <feGaussianBlur stdDeviation="3" result="coloredBlur" />
            <feMerge>
              <feMergeNode in="coloredBlur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
          <style>
            @keyframes pulse {
              0%, 100% { opacity: 1; }
              50% { opacity: 0.5; }
            }
            .node-active {
              animation: pulse 2s ease-in-out infinite;
            }
          </style>
        </defs>

        <g class="edges-group">
          <template v-for="edge in graphData?.edges" :key="edge.id">
            <path
              :d="getEdgePath(edge)"
              :class="['edge-path', getEdgeClass(edge)]"
              stroke="#bfbfbf"
              stroke-width="2"
              fill="none"
              marker-end="url(#track-arrowhead)"
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
        </g>

        <g class="nodes-group">
          <template v-for="node in graphData?.nodes" :key="node.id">
            <g
              :class="['node-group', getNodeClass(node)]"
              :transform="`translate(${node.x}, ${node.y})`"
              @mouseenter="handleNodeHover(node, $event)"
              @mouseleave="handleNodeLeave"
              @click="handleNodeClick(node)"
            >
              <template v-if="node.type === 'start' || node.type === 'end'">
                <ellipse
                  :cx="node.width / 2"
                  :cy="node.height / 2"
                  :rx="node.width / 2"
                  :ry="node.height / 2"
                  :class="['node-shape', `status-${node.status}`]"
                />
              </template>
              <template v-else-if="node.type === 'parallelGateway' || node.type === 'exclusiveGateway'">
                <rect
                  :x="node.width / 2 - (node.width * 0.707) / 2"
                  :y="node.height / 2 - (node.height * 0.707) / 2"
                  :width="node.width * 0.707"
                  :height="node.height * 0.707"
                  :class="['node-shape', `status-${node.status}`]"
                  transform="rotate(45, 30, 30)"
                />
              </template>
              <template v-else-if="node.type === 'countersign'">
                <rect
                  :width="node.width"
                  :height="node.height"
                  :rx="8"
                  :ry="8"
                  :class="['node-shape', `status-${node.status}`, 'countersign-shape']"
                />
              </template>
              <template v-else>
                <rect
                  :width="node.width"
                  :height="node.height"
                  :rx="8"
                  :ry="8"
                  :class="['node-shape', `status-${node.status}`]"
                />
              </template>

              <text
                :x="node.width / 2"
                :y="node.height / 2 - 8"
                class="node-name"
                text-anchor="middle"
              >
                {{ node.name }}
              </text>
              <text
                v-if="node.duration"
                :x="node.width / 2"
                :y="node.height / 2 + 12"
                class="node-duration"
                text-anchor="middle"
              >
                {{ node.duration }}
              </text>
            </g>
          </template>
        </g>
      </svg>

      <div
        v-if="hoveredNode"
        class="node-tooltip"
        :style="{ left: tooltipPosition.x + 'px', top: tooltipPosition.y + 'px' }"
      >
        <div class="tooltip-header">
          <span class="tooltip-title">{{ hoveredNode.name }}</span>
          <a-tag :color="getStatusTagColor(hoveredNode.status)">
            {{ getStatusText(hoveredNode.status) }}
          </a-tag>
        </div>
        <div v-if="hoveredNode.duration" class="tooltip-duration">
          耗时：{{ hoveredNode.duration }}
        </div>
        <div v-if="hoveredNode.histories && hoveredNode.histories.length > 0" class="tooltip-histories">
          <a-divider style="margin: 8px 0" />
          <div class="histories-title">审批历史</div>
          <a-timeline size="small">
            <a-timeline-item
              v-for="(history, idx) in hoveredNode.histories"
              :key="history.id"
              :color="getTimelineColor(history.action)"
            >
              <div class="history-item">
                <div class="history-header">
                  <span class="history-operator">{{ history.operatorName }}</span>
                  <a-tag :color="getActionTagColor(history.action)" size="small">
                    {{ history.actionName }}
                  </a-tag>
                </div>
                <div v-if="history.opinion" class="history-opinion">
                  {{ history.opinion }}
                </div>
                <div class="history-time">{{ history.operateTime }}</div>
                <div v-if="history.duration" class="history-duration">
                  耗时：{{ history.duration }}
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>
    </div>

    <a-empty
      v-if="!graphData || !graphData.nodes || graphData.nodes.length === 0"
      description="暂无流程图数据"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { WfProcessGraphVO, WfGraphNode, WfGraphEdge, NodeType } from '@/types/workflow'
import { Empty } from 'ant-design-vue'
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'

interface Props {
  graphData?: WfProcessGraphVO | null
  processInstanceId?: string
}

interface Emits {
  (e: 'node-click', node: WfGraphNode): void
}

const props = withDefaults(defineProps<Props>(), {
  graphData: null,
  processInstanceId: ''
})

const emit = defineEmits<Emits>()

const graphContainerRef = ref<HTMLDivElement | null>(null)
const svgContainerRef = ref<HTMLDivElement | null>(null)
const scale = ref(1)
const translate = ref({ x: 0, y: 0 })
const isPanning = ref(false)
const lastPanPos = ref({ x: 0, y: 0 })
const hoveredNode = ref<WfGraphNode | null>(null)
const tooltipPosition = ref({ x: 0, y: 0 })

const handleZoomIn = () => {
  scale.value = Math.min(scale.value + 0.1, 2)
}

const handleZoomOut = () => {
  scale.value = Math.max(scale.value - 0.1, 0.5)
}

const handleReset = () => {
  scale.value = 1
  translate.value = { x: 0, y: 0 }
}

const handleWheel = (e: WheelEvent) => {
  if (e.deltaY < 0) {
    handleZoomIn()
  } else {
    handleZoomOut()
  }
}

const handlePanStart = (e: MouseEvent) => {
  if ((e.target as HTMLElement).closest('.node-group')) return
  isPanning.value = true
  lastPanPos.value = { x: e.clientX, y: e.clientY }
}

const handlePanMove = (e: MouseEvent) => {
  if (!isPanning.value) return
  const dx = e.clientX - lastPanPos.value.x
  const dy = e.clientY - lastPanPos.value.y
  translate.value = {
    x: translate.value.x + dx,
    y: translate.value.y + dy
  }
  lastPanPos.value = { x: e.clientX, y: e.clientY }
}

const handlePanEnd = () => {
  isPanning.value = false
}

const getNodeClass = (node: WfGraphNode): string => {
  const classes = []
  if (node.status === 'active') {
    classes.push('node-active')
  }
  return classes.join(' ')
}

const getEdgeClass = (edge: WfGraphEdge): string => {
  if (!props.graphData) return ''
  const sourceNode = props.graphData.nodes.find(n => n.id === edge.source)
  const targetNode = props.graphData.nodes.find(n => n.id === edge.target)
  if (sourceNode?.status === 'completed' && targetNode?.status === 'completed') {
    return 'edge-completed'
  }
  if (sourceNode?.status === 'completed' && targetNode?.status === 'active') {
    return 'edge-active'
  }
  return ''
}

const getEdgePath = (edge: WfGraphEdge): string => {
  if (!props.graphData) return ''
  const sourceNode = props.graphData.nodes.find(n => n.id === edge.source)
  const targetNode = props.graphData.nodes.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return ''

  const startX = sourceNode.x + sourceNode.width
  const startY = sourceNode.y + sourceNode.height / 2
  const endX = targetNode.x
  const endY = targetNode.y + targetNode.height / 2

  const midX = (startX + endX) / 2

  return `M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX - 10} ${endY}`
}

const getEdgeLabelPosition = (edge: WfGraphEdge): { x: number; y: number } => {
  if (!props.graphData) return { x: 0, y: 0 }
  const sourceNode = props.graphData.nodes.find(n => n.id === edge.source)
  const targetNode = props.graphData.nodes.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return { x: 0, y: 0 }

  return {
    x: (sourceNode.x + sourceNode.width + targetNode.x) / 2,
    y: (sourceNode.y + sourceNode.height / 2 + targetNode.y + targetNode.height / 2) / 2 - 5
  }
}

const handleNodeHover = (node: WfGraphNode, e: MouseEvent) => {
  hoveredNode.value = node
  if (svgContainerRef.value) {
    const rect = svgContainerRef.value.getBoundingClientRect()
    tooltipPosition.value = {
      x: e.clientX - rect.left + 15,
      y: e.clientY - rect.top + 15
    }
  }
}

const handleNodeLeave = () => {
  hoveredNode.value = null
}

const handleNodeClick = (node: WfGraphNode) => {
  emit('node-click', node)
}

const getStatusTagColor = (status: string): string => {
  const colors: Record<string, string> = {
    completed: 'green',
    active: 'blue',
    pending: 'default'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string): string => {
  const texts: Record<string, string> = {
    completed: '已完成',
    active: '进行中',
    pending: '未开始'
  }
  return texts[status] || status
}

const getActionTagColor = (action: string): string => {
  const colors: Record<string, string> = {
    approve: 'green',
    reject: 'red',
    back: 'orange',
    transfer: 'blue',
    addSign: 'purple',
    terminate: 'red'
  }
  return colors[action] || 'default'
}

const getTimelineColor = (action: string): string => {
  const colors: Record<string, string> = {
    approve: 'green',
    reject: 'red',
    back: 'orange',
    transfer: 'blue',
    addSign: 'purple',
    terminate: 'red'
  }
  return colors[action] || 'gray'
}
</script>

<style scoped lang="less">
.process-track-graph {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;

  .graph-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;

    .zoom-info {
      font-size: 12px;
      color: #666;
    }
  }

  .graph-container {
    flex: 1;
    position: relative;
    overflow: hidden;
    background:
      linear-gradient(#f5f5f5 1px, transparent 1px),
      linear-gradient(90deg, #f5f5f5 1px, transparent 1px);
    background-size: 20px 20px;
    cursor: grab;

    &:active {
      cursor: grabbing;
    }

    .graph-svg {
      width: 100%;
      height: 100%;
      min-width: 800px;
      min-height: 600px;
      transition: transform 0.1s ease-out;

      .edge-path {
        transition: stroke 0.3s;

        &.edge-completed {
          stroke: #52c41a;
        }

        &.edge-active {
          stroke: #1890ff;
        }
      }

      .edge-label {
        font-size: 12px;
        fill: #666;
        text-anchor: middle;
      }

      .node-group {
        cursor: pointer;

        .node-shape {
          stroke-width: 2;
          transition: all 0.3s;

          &.status-completed {
            fill: #f6ffed;
            stroke: #52c41a;
          }

          &.status-active {
            fill: #e6f7ff;
            stroke: #1890ff;
            stroke-width: 3;
            filter: url(#glow);
          }

          &.status-pending {
            fill: #fafafa;
            stroke: #d9d9d9;
          }

          &.countersign-shape {
            stroke-dasharray: 5, 5;
          }
        }

        &:hover .node-shape {
          filter: url(#glow);
        }

        .node-name {
          font-size: 12px;
          font-weight: 500;
          fill: #333;
        }

        .node-duration {
          font-size: 10px;
          fill: #8c8c8c;
        }
      }
    }

    .node-tooltip {
      position: absolute;
      z-index: 1000;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      padding: 12px;
      min-width: 280px;
      max-width: 360px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      pointer-events: none;

      .tooltip-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .tooltip-title {
          font-weight: 500;
          font-size: 14px;
        }
      }

      .tooltip-duration {
        font-size: 12px;
        color: #8c8c8c;
      }

      .histories-title {
        font-size: 12px;
        font-weight: 500;
        color: #666;
        margin-bottom: 8px;
      }

      .history-item {
        .history-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;

          .history-operator {
            font-weight: 500;
            font-size: 13px;
          }
        }

        .history-opinion {
          font-size: 12px;
          color: #666;
          margin-bottom: 4px;
          word-break: break-all;
        }

        .history-time {
          font-size: 11px;
          color: #8c8c8c;
        }

        .history-duration {
          font-size: 11px;
          color: #8c8c8c;
          margin-top: 2px;
        }
      }
    }
  }
}
</style>
