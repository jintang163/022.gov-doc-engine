<template>
  <div class="opinion-template-selector">
    <a-tabs v-model:activeKey="activeType" size="small" @change="handleTypeChange">
      <a-tab-pane key="agree" tab="同意常用语" />
      <a-tab-pane key="reject" tab="驳回常用语" />
      <a-tab-pane key="general" tab="通用常用语" />
    </a-tabs>

    <div class="template-list">
      <div
        v-for="template in filteredTemplates"
        :key="template.id"
        class="template-item"
        :class="{ 'is-personal': template.isPersonal }"
        @click="handleSelect(template)"
      >
        <div class="template-content">
          <component :is="getTypeIcon(template.type)" class="template-icon" />
          <span class="template-text">{{ template.content }}</span>
        </div>
        <div class="template-actions">
          <a-tag v-if="template.isPersonal" color="blue" size="small">个人</a-tag>
          <a-button
            v-if="template.isPersonal && template.userId === currentUserId"
            type="text"
            size="small"
            danger
            class="delete-btn"
            @click.stop="handleDelete(template)"
          >
            <template #icon><delete-outlined /></template>
          </a-button>
        </div>
      </div>
      <a-empty
        v-if="filteredTemplates.length === 0"
        description="暂无常用语"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
        :image-style="{ height: 60 }"
      />
    </div>

    <div class="add-section">
      <a-input
        v-model:value="newTemplateContent"
        placeholder="添加个人常用语..."
        size="small"
        @keyup.enter="handleAdd"
      >
        <template #suffix>
          <a-button type="primary" size="small" :disabled="!newTemplateContent.trim()" @click="handleAdd">
            <template #icon><plus-outlined /></template>
            添加
          </a-button>
        </template>
      </a-input>
    </div>

    <a-popconfirm
      v-model:open="deleteConfirmVisible"
      title="确定要删除这个常用语吗？"
      ok-text="确定"
      cancel-text="取消"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message, Empty } from 'ant-design-vue'
import type { WfOpinionTemplateVO, WfOpinionTemplateSaveDTO } from '@/types/workflow'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  MessageOutlined,
  DeleteOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'

interface Props {
  templateType?: string
  currentUserId?: string
}

interface Emits {
  (e: 'select', content: string): void
  (e: 'add', template: WfOpinionTemplateSaveDTO): void
  (e: 'delete', id: number): void
}

const props = withDefaults(defineProps<Props>(), {
  templateType: 'general',
  currentUserId: ''
})

const emit = defineEmits<Emits>()

const activeType = ref<'agree' | 'reject' | 'general'>('general')
const newTemplateContent = ref('')
const deleteConfirmVisible = ref(false)
const templateToDelete = ref<WfOpinionTemplateVO | null>(null)

const defaultTemplates = ref<WfOpinionTemplateVO[]>([
  { id: 1, type: 'agree', typeName: '同意', content: '同意，按此执行', isPersonal: false, createTime: '2024-01-01' },
  { id: 2, type: 'agree', typeName: '同意', content: '同意，请尽快办理', isPersonal: false, createTime: '2024-01-01' },
  { id: 3, type: 'agree', typeName: '同意', content: '情况属实，同意', isPersonal: false, createTime: '2024-01-01' },
  { id: 4, type: 'agree', typeName: '同意', content: '同意，请相关部门配合', isPersonal: false, createTime: '2024-01-01' },
  { id: 5, type: 'reject', typeName: '驳回', content: '资料不全，请补充后重新提交', isPersonal: false, createTime: '2024-01-01' },
  { id: 6, type: 'reject', typeName: '驳回', content: '不符合相关规定，不予通过', isPersonal: false, createTime: '2024-01-01' },
  { id: 7, type: 'reject', typeName: '驳回', content: '请修改后重新提交', isPersonal: false, createTime: '2024-01-01' },
  { id: 8, type: 'general', typeName: '通用', content: '请先与相关部门沟通', isPersonal: false, createTime: '2024-01-01' },
  { id: 9, type: 'general', typeName: '通用', content: '请按规定流程办理', isPersonal: false, createTime: '2024-01-01' },
  { id: 10, type: 'general', typeName: '通用', content: '请提供更多详细信息', isPersonal: false, createTime: '2024-01-01' }
])

const personalTemplates = ref<WfOpinionTemplateVO[]>([])

const allTemplates = computed(() => {
  return [...defaultTemplates.value, ...personalTemplates.value]
})

const filteredTemplates = computed(() => {
  return allTemplates.value.filter(t => t.type === activeType.value)
})

const getTypeIcon = (type: string) => {
  const icons: Record<string, any> = {
    agree: CheckCircleOutlined,
    reject: CloseCircleOutlined,
    general: MessageOutlined
  }
  return icons[type] || MessageOutlined
}

const handleTypeChange = (key: string) => {
  activeType.value = key as 'agree' | 'reject' | 'general'
}

const handleSelect = (template: WfOpinionTemplateVO) => {
  emit('select', template.content)
}

const handleAdd = () => {
  const content = newTemplateContent.value.trim()
  if (!content) {
    message.warning('请输入常用语内容')
    return
  }

  const newTemplate: WfOpinionTemplateSaveDTO = {
    type: activeType.value,
    content,
    isPersonal: true
  }

  const newId = Date.now()
  const newTemplateVO: WfOpinionTemplateVO = {
    ...newTemplate,
    id: newId,
    typeName: getTypeName(activeType.value),
    userId: props.currentUserId,
    createTime: new Date().toISOString()
  }

  personalTemplates.value.push(newTemplateVO)
  emit('add', newTemplate)
  newTemplateContent.value = ''
  message.success('添加成功')
}

const handleDelete = (template: WfOpinionTemplateVO) => {
  templateToDelete.value = template
  deleteConfirmVisible.value = true
}

const confirmDelete = () => {
  if (!templateToDelete.value) return

  const idx = personalTemplates.value.findIndex(t => t.id === templateToDelete.value!.id)
  if (idx !== -1) {
    personalTemplates.value.splice(idx, 1)
    emit('delete', templateToDelete.value.id)
    message.success('删除成功')
  }

  deleteConfirmVisible.value = false
  templateToDelete.value = null
}

const getTypeName = (type: string): string => {
  const names: Record<string, string> = {
    agree: '同意',
    reject: '驳回',
    general: '通用'
  }
  return names[type] || '通用'
}

watch(() => props.templateType, (val) => {
  if (val && ['agree', 'reject', 'general'].includes(val)) {
    activeType.value = val as 'agree' | 'reject' | 'general'
  }
}, { immediate: true })
</script>

<style scoped lang="less">
.opinion-template-selector {
  .template-list {
    max-height: 200px;
    overflow-y: auto;
    margin-bottom: 12px;
    padding: 4px;

    .template-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      margin-bottom: 4px;
      background: #fafafa;
      border: 1px solid transparent;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #f0f5ff;
        border-color: #1890ff;

        .delete-btn {
          opacity: 1;
        }
      }

      &.is-personal {
        background: #f6ffed;
        border-color: #b7eb8f;
      }

      .template-content {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;
        min-width: 0;

        .template-icon {
          color: #8c8c8c;
          flex-shrink: 0;
        }

        .template-text {
          font-size: 13px;
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .template-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;

        .delete-btn {
          opacity: 0;
          transition: opacity 0.2s;
          padding: 0;
        }
      }
    }
  }

  .add-section {
    padding-top: 12px;
    border-top: 1px dashed #e8e8e8;
  }
}
</style>
