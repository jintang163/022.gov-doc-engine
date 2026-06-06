<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">公文管理</h2>
      <a-space>
        <a-button type="primary" @click="handleCreateFromTemplate">
          <template #icon><PlusOutlined /></template>
          从模板新建公文
        </a-button>
      </a-space>
    </div>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="false"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small">查看</a-button>
              <a-button type="link" size="small">编辑</a-button>
              <a-button type="link" size="small">预览</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="dataSource.length === 0" description="暂无公文数据，请从模板新建公文" />
    </a-card>

    <a-modal
      v-model:open="templateModalVisible"
      title="选择公文模板"
      width="800px"
      :footer="null"
    >
      <a-table
        :columns="templateColumns"
        :data-source="availableTemplates"
        :loading="templateLoading"
        :pagination="false"
        row-key="id"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="handleSelectTemplate(record)">
              选择此模板
            </a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { listAvailableTemplates } from '@/api/template'
import type { DocTemplateVO, DocDocument } from '@/types/template'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<DocDocument[]>([])
const templateModalVisible = ref(false)
const templateLoading = ref(false)
const availableTemplates = ref<DocTemplateVO[]>([])

const statusOptions = [
  { label: '草稿', value: 'draft', color: 'default' },
  { label: '待审核', value: 'pending', color: 'processing' },
  { label: '审批中', value: 'reviewing', color: 'processing' },
  { label: '已通过', value: 'approved', color: 'success' },
  { label: '已驳回', value: 'rejected', color: 'error' },
  { label: '已归档', value: 'archived', color: 'default' }
]

const getStatusLabel = (status: string) => {
  const item = statusOptions.find((s) => s.value === status)
  return item?.label || status
}

const getStatusColor = (status: string) => {
  const item = statusOptions.find((s) => s.value === status)
  return item?.color || 'default'
}

const columns = [
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', width: 200 },
  { title: '发文字号', dataIndex: 'docNumber', key: 'docNumber', width: 180 },
  { title: '公文类型', dataIndex: 'docType', key: 'docType', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const templateColumns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 150 },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 200 },
  { title: '模板类型', dataIndex: 'templateType', key: 'templateType', width: 100 },
  { title: '模板分类', dataIndex: 'templateCategory', key: 'templateCategory', width: 100 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 80 },
  { title: '操作', key: 'action', width: 120 }
]

const loadAvailableTemplates = async () => {
  templateLoading.value = true
  try {
    availableTemplates.value = await listAvailableTemplates()
  } catch (error) {
    console.error('加载可用模板失败:', error)
  } finally {
    templateLoading.value = false
  }
}

const handleCreateFromTemplate = () => {
  loadAvailableTemplates()
  templateModalVisible.value = true
}

const handleSelectTemplate = (template: DocTemplateVO) => {
  templateModalVisible.value = false
  router.push(`/document/create/${template.id}`)
  message.success(`已选择模板：${template.templateName}`)
}

onMounted(() => {
  loading.value = false
})
</script>
