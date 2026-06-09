<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">归档管理</h2>
      <a-space>
        <a-button type="primary" @click="archiveModalVisible = true">
          <template #icon><PlusOutlined /></template>
          手动归档
        </a-button>
      </a-space>
    </div>

    <a-card style="margin-bottom: 16px">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="标题/发文字号/内容/发文单位"
            allow-clear
            @press-enter="handleSearch"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-col>
        <a-col :span="4">
          <a-date-picker
            v-model:value="queryForm.archiveYearValue"
            picker="year"
            placeholder="归档年度"
            style="width: 100%"
            @change="handleYearChange"
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.archiveType"
            placeholder="文种分类"
            :options="archiveTypeOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.securityLevel"
            placeholder="密级"
            :options="archiveSecurityLevelOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="queryForm.status"
            placeholder="状态"
            :options="archiveStatusOptions"
            allow-clear
            style="width: 100%"
          />
        </a-col>
        <a-col :span="2">
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handlePageChange"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'securityLevel'">
            <a-tag :color="getSecurityColor(record.securityLevel)">
              {{ record.securityLevel }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                <EyeOutlined />
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleBorrow(record)">
                <FileProtectOutlined />
                借阅
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="archiveModalVisible"
      title="手动归档"
      @ok="handleArchiveSubmit"
      :confirm-loading="archiveSubmitting"
    >
      <a-form :model="archiveForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="公文" required>
          <a-select
            v-model:value="archiveForm.docId"
            placeholder="请选择未归档公文"
            show-search
            :filter-option="filterDocOption"
            :loading="unarchivedLoading"
            style="width: 100%"
          >
            <a-select-option
              v-for="doc in unarchivedDocs"
              :key="doc.id"
              :value="doc.id"
              :label="doc.docTitle"
            >
              {{ doc.docTitle }} ({{ doc.docNumber }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="文种分类">
          <a-select
            v-model:value="archiveForm.archiveType"
            placeholder="请选择文种分类"
            :options="archiveTypeOptions"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="存放位置">
          <a-input v-model:value="archiveForm.archiveLocation" placeholder="请输入存放位置" />
        </a-form-item>
        <a-form-item label="保管期限">
          <a-select
            v-model:value="archiveForm.retentionPeriod"
            placeholder="请选择保管期限"
            :options="retentionPeriodOptions"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="archiveForm.remark" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined, EyeOutlined, FileProtectOutlined } from '@ant-design/icons-vue'
import { getArchivePage, manualArchive } from '@/api/archive'
import { getDocumentPage } from '@/api/template'
import type { DocArchiveVO, DocArchiveQueryDTO, DocArchiveDTO } from '@/types/archive'
import { archiveStatusOptions, archiveTypeOptions, archiveSecurityLevelOptions, retentionPeriodOptions } from '@/types/archive'
import type { DocDocument } from '@/types/template'
import type { Dayjs } from 'dayjs'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<DocArchiveVO[]>([])
const archiveModalVisible = ref(false)
const archiveSubmitting = ref(false)
const unarchivedLoading = ref(false)
const unarchivedDocs = ref<DocDocument[]>([])

const queryForm = reactive({
  keyword: '',
  archiveYear: undefined as number | undefined,
  archiveYearValue: undefined as Dayjs | undefined,
  archiveType: undefined as string | undefined,
  securityLevel: undefined as string | undefined,
  status: undefined as string | undefined
})

const archiveForm = reactive<{
  docId: number | undefined
  archiveType: string | undefined
  archiveLocation: string | undefined
  retentionPeriod: string | undefined
  remark: string | undefined
}>({
  docId: undefined,
  archiveType: undefined,
  archiveLocation: undefined,
  retentionPeriod: undefined,
  remark: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const getStatusLabel = (status: string) => {
  const item = archiveStatusOptions.find((s) => s.value === status)
  return item?.label || status
}

const getStatusColor = (status: string) => {
  const item = archiveStatusOptions.find((s) => s.value === status)
  return item?.color || 'default'
}

const getSecurityColor = (level: string) => {
  const map: Record<string, string> = {
    '绝密': 'red',
    '机密': 'orange',
    '秘密': 'gold',
    '普通': 'green'
  }
  return map[level] || 'default'
}

const columns = [
  { title: '归档编号', dataIndex: 'archiveNo', key: 'archiveNo', width: 150 },
  { title: '公文标题', dataIndex: 'docTitle', key: 'docTitle', width: 200, ellipsis: true },
  { title: '发文字号', dataIndex: 'docNumber', key: 'docNumber', width: 160 },
  { title: '文种分类', dataIndex: 'archiveType', key: 'archiveType', width: 100 },
  { title: '密级', dataIndex: 'securityLevel', key: 'securityLevel', width: 80 },
  { title: '归档年度', dataIndex: 'archiveYear', key: 'archiveYear', width: 100 },
  { title: '归档方式', dataIndex: 'archiveMethodName', key: 'archiveMethodName', width: 100 },
  { title: '保管期限', dataIndex: 'retentionPeriodName', key: 'retentionPeriodName', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '归档日期', dataIndex: 'archiveDate', key: 'archiveDate', width: 120 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const loadArchiveList = async () => {
  loading.value = true
  try {
    const params: DocArchiveQueryDTO = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    if (queryForm.keyword) params.keyword = queryForm.keyword
    if (queryForm.archiveYear) params.archiveYear = queryForm.archiveYear
    if (queryForm.archiveType) params.archiveType = queryForm.archiveType
    if (queryForm.securityLevel) params.securityLevel = queryForm.securityLevel
    if (queryForm.status) params.status = queryForm.status
    const result = await getArchivePage(params)
    dataSource.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    message.error('加载归档列表失败')
  } finally {
    loading.value = false
  }
}

const loadUnarchivedDocs = async () => {
  unarchivedLoading.value = true
  try {
    const result = await getDocumentPage({ pageNum: 1, pageSize: 200 })
    unarchivedDocs.value = (result.list || []).filter(
      (doc: DocDocument) => doc.status !== 'archived'
    )
  } catch (error) {
    message.error('加载未归档公文失败')
  } finally {
    unarchivedLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadArchiveList()
}

const handleYearChange = (value: Dayjs | null) => {
  queryForm.archiveYear = value ? value.year() : undefined
}

const handlePageChange = (paginationInfo: { current: number; pageSize: number }) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  loadArchiveList()
}

const handleView = (record: DocArchiveVO) => {
  router.push(`/archive/${record.id}`)
}

const handleBorrow = (record: DocArchiveVO) => {
  router.push(`/borrow/apply?archiveId=${record.id}`)
}

const filterDocOption = (input: string, option: any) => {
  return option.label?.toLowerCase().includes(input.toLowerCase())
}

const handleArchiveSubmit = async () => {
  if (!archiveForm.docId) {
    message.warning('请选择未归档公文')
    return
  }
  archiveSubmitting.value = true
  try {
    const data: DocArchiveDTO = {
      docId: archiveForm.docId,
      archiveType: archiveForm.archiveType,
      archiveLocation: archiveForm.archiveLocation,
      retentionPeriod: archiveForm.retentionPeriod,
      remark: archiveForm.remark
    }
    await manualArchive(data)
    message.success('归档成功')
    archiveModalVisible.value = false
    resetArchiveForm()
    loadArchiveList()
  } catch (error) {
    message.error('归档失败')
  } finally {
    archiveSubmitting.value = false
  }
}

const resetArchiveForm = () => {
  archiveForm.docId = undefined
  archiveForm.archiveType = undefined
  archiveForm.archiveLocation = undefined
  archiveForm.retentionPeriod = undefined
  archiveForm.remark = undefined
}

onMounted(() => {
  loadArchiveList()
  loadUnarchivedDocs()
})
</script>
