<template>
  <a-modal
    :open="open"
    :title="`印章授权管理 - ${sealName}`"
    :width="1000"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="grant-list">
      <div class="toolbar">
        <a-button type="primary" @click="handleAddGrant">
          <template #icon><PlusOutlined /></template>
          新增授权
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'grantType'">
            <a-tag :color="getGrantTypeColor(record.grantType)">
              {{ getGrantTypeLabel(record.grantType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getGrantStatusColor(record.status)">
              {{ getGrantStatusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm
              v-if="record.status === 1"
              title="确定要撤销该授权吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleRevoke(record.id)"
            >
              <a-button type="link" size="small" danger>
                撤销授权
              </a-button>
            </a-popconfirm>
            <span v-else style="color: #999">-</span>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="grantFormVisible"
      :title="grantFormTitle"
      :width="600"
      :confirm-loading="confirmLoading"
      @ok="handleGrantFormOk"
      @cancel="grantFormVisible = false"
    >
      <a-form
        ref="grantFormRef"
        :model="grantFormData"
        :rules="grantRules"
        layout="vertical"
      >
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="授权类型" name="grantType">
              <a-select
                v-model:value="grantFormData.grantType"
                placeholder="请选择授权类型"
                :options="grantTypeOptions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="授权目标ID" name="grantTargetId">
              <a-input v-model:value="grantFormData.grantTargetId" placeholder="请输入授权目标ID" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="授权目标名称" name="grantTargetName">
              <a-input v-model:value="grantFormData.grantTargetName" placeholder="请输入授权目标名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="授权开始时间" name="grantStartTime">
              <a-date-picker
                v-model:value="grantStartTime"
                show-time
                style="width: 100%"
                placeholder="请选择开始时间"
                valueFormat="YYYY-MM-DD HH:mm:ss"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="授权结束时间" name="grantEndTime">
              <a-date-picker
                v-model:value="grantEndTime"
                show-time
                style="width: 100%"
                placeholder="请选择结束时间"
                valueFormat="YYYY-MM-DD HH:mm:ss"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="签章次数限制" name="signLimit">
              <a-input-number
                v-model:value="grantFormData.signLimit"
                :min="0"
                style="width: 100%"
                placeholder="0表示不限制"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, type Ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getSealGrantList,
  saveSealGrant,
  revokeSealGrant,
  getGrantTypeLabel,
  getGrantTypeColor,
  getGrantStatusLabel,
  getGrantStatusColor
} from '@/api/signature'
import type { DocSealGrantVO, DocSealGrantDTO } from '@/types/signature'
import { grantTypeOptions } from '@/types/signature'

const props = defineProps<{
  open: boolean
  sealId: number
  sealName: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const loading = ref(false)
const confirmLoading = ref(false)
const dataSource = ref<DocSealGrantVO[]>([])
const grantFormVisible = ref(false)
const grantFormRef = ref<FormInstance>()

const grantStartTime = ref<dayjs.Dayjs | null>(null)
const grantEndTime = ref<dayjs.Dayjs | null>(null)

const grantFormTitle = computed(() => '新增授权')

const grantFormData = reactive<DocSealGrantDTO>({
  sealId: 0,
  grantType: 'USER',
  grantTargetId: 0,
  grantTargetName: '',
  grantStartTime: '',
  grantEndTime: '',
  signLimit: 0
})

const grantRules = {
  grantType: [{ required: true, message: '请选择授权类型', trigger: 'change' }],
  grantTargetId: [{ required: true, message: '请输入授权目标ID', trigger: 'blur' }],
  grantTargetName: [{ required: true, message: '请输入授权目标名称', trigger: 'blur' }],
  grantStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  grantEndTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const columns = [
  {
    title: '授权类型',
    dataIndex: 'grantType',
    key: 'grantType',
    width: 120
  },
  {
    title: '授权目标名称',
    dataIndex: 'grantTargetName',
    key: 'grantTargetName',
    width: 200
  },
  {
    title: '授权开始时间',
    dataIndex: 'grantStartTime',
    key: 'grantStartTime',
    width: 180
  },
  {
    title: '授权结束时间',
    dataIndex: 'grantEndTime',
    key: 'grantEndTime',
    width: 180
  },
  {
    title: '签章次数限制',
    dataIndex: 'signLimit',
    key: 'signLimit',
    width: 140,
    customRender: ({ text }: { text: number }) => {
      return text === 0 ? '不限制' : text
    }
  },
  {
    title: '已签章次数',
    dataIndex: 'signCount',
    key: 'signCount',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right'
  }
]

const fetchData = async () => {
  if (!props.sealId) return
  loading.value = true
  try {
    const res = await getSealGrantList(props.sealId)
    if (res.code === 200) {
      dataSource.value = res.data
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetGrantForm = () => {
  Object.assign(grantFormData, {
    sealId: props.sealId,
    grantType: 'USER',
    grantTargetId: 0,
    grantTargetName: '',
    grantStartTime: '',
    grantEndTime: '',
    signLimit: 0
  })
  grantStartTime.value = null
  grantEndTime.value = null
  grantFormRef.value?.clearValidate()
}

const handleAddGrant = () => {
  resetGrantForm()
  grantFormVisible.value = true
}

const handleGrantFormOk = async () => {
  try {
    grantFormData.grantStartTime = grantStartTime.value ? grantStartTime.value.format('YYYY-MM-DD HH:mm:ss') : ''
    grantFormData.grantEndTime = grantEndTime.value ? grantEndTime.value.format('YYYY-MM-DD HH:mm:ss') : ''

    await grantFormRef.value?.validate()

    if (grantStartTime.value && grantEndTime.value && grantStartTime.value.isAfter(grantEndTime.value)) {
      message.warning('结束时间必须晚于开始时间')
      return
    }

    confirmLoading.value = true

    const saveData: DocSealGrantDTO = {
      ...grantFormData,
      sealId: props.sealId
    }
    const res = await saveSealGrant(saveData)
    if (res.code === 200) {
      message.success('授权成功')
      grantFormVisible.value = false
      fetchData()
    } else {
      message.error(res.message || '授权失败')
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    confirmLoading.value = false
  }
}

const handleRevoke = async (id: number) => {
  loading.value = true
  try {
    const res = await revokeSealGrant(id)
    if (res.code === 200) {
      message.success('撤销成功')
      fetchData()
    } else {
      message.error(res.message || '撤销失败')
    }
  } catch (error) {
    message.error('撤销失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('update:open', false)
}

watch(
  () => props.open,
  (val) => {
    if (val && props.sealId) {
      fetchData()
    }
  }
)
</script>

<style scoped lang="less">
.grant-list {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
