<template>
  <div class="template-list">
    <a-card title="公文模板管理" :bordered="false">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item label="模板编码">
          <a-input
            v-model:value="queryForm.templateCode"
            placeholder="请输入模板编码"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="模板名称">
          <a-input
            v-model:value="queryForm.templateName"
            placeholder="请输入模板名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="模板类型">
          <a-select
            v-model:value="queryForm.templateType"
            placeholder="请选择模板类型"
            allow-clear
            :options="templateTypeOptions"
          />
        </a-form-item>
        <a-form-item label="模板分类">
          <a-select
            v-model:value="queryForm.templateCategory"
            placeholder="请选择模板分类"
            allow-clear
            :options="templateCategoryOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            :options="statusOptions"
          />
        </a-form-item>
        <a-form-item label="是否当前版本">
          <a-select
            v-model:value="queryForm.isCurrentVersion"
            placeholder="请选择"
            allow-clear
            :options="[
              { label: '是', value: 1 },
              { label: '否', value: 0 }
            ]"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新建模板
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'isCurrentVersion'">
            <a-tag :color="record.isCurrentVersion === 1 ? 'success' : 'default'">
              {{ record.isCurrentVersion === 1 ? '是' : '否' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handleEdit(record.id)"
              >
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handlePreview(record.id)">
                预览
              </a-button>
              <a-button type="link" size="small" @click="handleCreateVersion(record.id)">
                创建新版本
              </a-button>
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handlePublish(record.id)"
              >
                发布
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleDisable(record.id)"
              >
                停用
              </a-button>
              <a-popconfirm
                v-if="record.status === 0"
                title="确定要删除该模板吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import {
  getTemplatePage,
  deleteTemplate,
  publishTemplate,
  disableTemplate,
  createNewVersion
} from '@/api/template'
import type { DocTemplateVO, DocTemplateQueryDTO } from '@/types/template'
import {
  templateTypeOptions,
  templateCategoryOptions,
  statusOptions
} from '@/types/template'

const router = useRouter()

const loading = ref(false)
const dataSource = ref<DocTemplateVO[]>([])

const queryForm = reactive<DocTemplateQueryDTO>({
  templateCode: undefined,
  templateName: undefined,
  templateType: undefined,
  templateCategory: undefined,
  status: undefined,
  isCurrentVersion: undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const columns = [
  {
    title: '模板编码',
    dataIndex: 'templateCode',
    key: 'templateCode',
    width: 150
  },
  {
    title: '模板名称',
    dataIndex: 'templateName',
    key: 'templateName',
    width: 200
  },
  {
    title: '模板类型',
    dataIndex: 'templateType',
    key: 'templateType',
    width: 120
  },
  {
    title: '模板分类',
    dataIndex: 'templateCategory',
    key: 'templateCategory',
    width: 120
  },
  {
    title: '版本号',
    dataIndex: 'version',
    key: 'version',
    width: 100
  },
  {
    title: '是否当前版本',
    dataIndex: 'isCurrentVersion',
    key: 'isCurrentVersion',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 320,
    fixed: 'right'
  }
]

const getStatusLabel = (status: number) => {
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

const getStatusColor = (status: number) => {
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTemplatePage(queryForm)
    if (res.code === 0) {
      dataSource.value = res.data.list
      pagination.total = res.data.total
      pagination.current = res.data.pageNum
      pagination.pageSize = res.data.pageSize
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryForm.templateCode = undefined
  queryForm.templateName = undefined
  queryForm.templateType = undefined
  queryForm.templateCategory = undefined
  queryForm.status = undefined
  queryForm.isCurrentVersion = undefined
  handleSearch()
}

const handleTableChange = (pag: TablePaginationConfig) => {
  queryForm.pageNum = pag.current
  queryForm.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  router.push('/template/edit')
}

const handleEdit = (id: number) => {
  router.push(`/template/edit/${id}`)
}

const handlePreview = (id: number) => {
  router.push(`/template/preview/${id}`)
}

const handleCreateVersion = async (id: number) => {
  loading.value = true
  try {
    const res = await createNewVersion(id)
    if (res.code === 0) {
      message.success('创建新版本成功')
      const newId = res.data
      router.push(`/template/edit/${newId}`)
    } else {
      message.error(res.message || '创建新版本失败')
    }
  } catch (error) {
    message.error('创建新版本失败')
  } finally {
    loading.value = false
  }
}

const handlePublish = async (id: number) => {
  loading.value = true
  try {
    const res = await publishTemplate(id)
    if (res.code === 0) {
      message.success('发布成功')
      fetchData()
    } else {
      message.error(res.message || '发布失败')
    }
  } catch (error) {
    message.error('发布失败')
  } finally {
    loading.value = false
  }
}

const handleDisable = async (id: number) => {
  loading.value = true
  try {
    const res = await disableTemplate(id)
    if (res.code === 0) {
      message.success('停用成功')
      fetchData()
    } else {
      message.error(res.message || '停用失败')
    }
  } catch (error) {
    message.error('停用失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  loading.value = true
  try {
    const res = await deleteTemplate(id)
    if (res.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.template-list {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
