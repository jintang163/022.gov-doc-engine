<template>
  <div class="seal-list">
    <a-card title="印章管理" :bordered="false">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item label="印章名称">
          <a-input
            v-model:value="queryForm.sealName"
            placeholder="请输入印章名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="印章类型">
          <a-select
            v-model:value="queryForm.sealType"
            placeholder="请选择印章类型"
            allow-clear
            :options="sealTypeSelectOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            :options="sealStatusSelectOptions"
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
              新建印章
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
          <template v-if="column.key === 'sealImage'">
            <img
              :src="getSealImageUrl(record.id)"
              width="60"
              height="60"
              style="object-fit: contain"
              @error="handleImageError"
            />
          </template>
          <template v-if="column.key === 'sealType'">
            <a-tag :color="getSealTypeColor(record.sealType)">
              {{ getSealTypeLabel(record.sealType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getSealStatusColor(record.status)">
              {{ getSealStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-upload
                :show-upload-list="false"
                :before-upload="(file) => handleBeforeUploadImage(file, record.id)"
                :custom-request="({ file }) => handleUploadImage(file as File, record.id)"
                accept="image/*"
              >
                <a-button type="link" size="small">
                  <template #icon><UploadOutlined /></template>
                  上传图片
                </a-button>
              </a-upload>
              <a-button type="link" size="small" @click="handleViewGrant(record)">
                <template #icon><TeamOutlined /></template>
                查看授权
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该印章吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <Form
      v-model:open="formModalVisible"
      :edit-data="currentEditData"
      @success="handleFormSuccess"
    />

    <GrantList
      v-model:open="grantModalVisible"
      :seal-id="currentSealId"
      :seal-name="currentSealName"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  UploadOutlined,
  TeamOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import {
  getSealPage,
  deleteSeal,
  uploadSealImage,
  getSealImageUrl,
  getSealTypeLabel,
  getSealTypeColor,
  getSealStatusLabel,
  getSealStatusColor
} from '@/api/signature'
import type { DocSealVO, DocSealQueryDTO } from '@/types/signature'
import { sealTypeOptions, sealStatusOptions } from '@/types/signature'
import Form from './Form.vue'
import GrantList from './GrantList.vue'

const loading = ref(false)
const dataSource = ref<DocSealVO[]>([])
const formModalVisible = ref(false)
const grantModalVisible = ref(false)
const currentEditData = ref<DocSealVO | null>(null)
const currentSealId = ref<number>(0)
const currentSealName = ref('')

const queryForm = reactive<DocSealQueryDTO>({
  sealName: undefined,
  sealType: undefined,
  status: undefined,
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

const sealTypeSelectOptions = sealTypeOptions.map((opt) => ({
  label: opt.label,
  value: opt.value
}))

const sealStatusSelectOptions = sealStatusOptions.map((opt) => ({
  label: opt.label,
  value: opt.value
}))

const columns = [
  {
    title: '印章图片',
    dataIndex: 'sealImage',
    key: 'sealImage',
    width: 100
  },
  {
    title: '印章名称',
    dataIndex: 'sealName',
    key: 'sealName',
    width: 150
  },
  {
    title: '印章编码',
    dataIndex: 'sealCode',
    key: 'sealCode',
    width: 150
  },
  {
    title: '印章类型',
    dataIndex: 'sealType',
    key: 'sealType',
    width: 120
  },
  {
    title: '所属单位',
    dataIndex: 'ownerUnitName',
    key: 'ownerUnitName',
    width: 150
  },
  {
    title: '所属部门',
    dataIndex: 'ownerDeptName',
    key: 'ownerDeptName',
    width: 150
  },
  {
    title: '持有人',
    dataIndex: 'ownerUserName',
    key: 'ownerUserName',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '算法',
    dataIndex: 'algorithm',
    key: 'algorithm',
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
    width: 280,
    fixed: 'right'
  }
]

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIGZpbGw9IiNGNUY1RjUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzk5OTlEOSIgZm9udC1zaXplPSIxMiI+5Y2w56uv5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSealPage(queryForm)
    if (res.code === 200) {
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
  queryForm.sealName = undefined
  queryForm.sealType = undefined
  queryForm.status = undefined
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
  currentEditData.value = null
  formModalVisible.value = true
}

const handleEdit = (record: DocSealVO) => {
  currentEditData.value = record
  formModalVisible.value = true
}

const handleFormSuccess = () => {
  fetchData()
}

const handleDelete = async (id: number) => {
  loading.value = true
  try {
    const res = await deleteSeal(id)
    if (res.code === 200) {
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

const handleBeforeUploadImage: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type?.startsWith('image/') || /\.(jpg|jpeg|png|gif|bmp)$/i.test(file.name || '')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

const handleUploadImage = async (file: File, sealId: number) => {
  loading.value = true
  try {
    const res = await uploadSealImage(sealId, file)
    if (res.code === 200) {
      message.success('上传成功')
      fetchData()
    } else {
      message.error(res.message || '上传失败')
    }
  } catch (error) {
    message.error('上传失败')
  } finally {
    loading.value = false
  }
}

const handleViewGrant = (record: DocSealVO) => {
  currentSealId.value = record.id
  currentSealName.value = record.sealName
  grantModalVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.seal-list {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
