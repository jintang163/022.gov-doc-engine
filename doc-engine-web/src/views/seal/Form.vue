<template>
  <a-modal
    :open="open"
    :title="modalTitle"
    :width="800"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      layout="vertical"
    >
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="印章名称" name="sealName">
            <a-input v-model:value="formData.sealName" placeholder="请输入印章名称" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="印章类型" name="sealType">
            <a-select
              v-model:value="formData.sealType"
              placeholder="请选择印章类型"
              :options="sealTypeOptions"
              @change="handleSealTypeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="印章编码" name="sealCode">
            <a-input v-model:value="formData.sealCode" placeholder="请输入印章编码" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所属单位" name="ownerUnitName">
            <a-input v-model:value="formData.ownerUnitName" placeholder="请输入所属单位" />
          </a-form-item>
        </a-col>
        <a-col v-if="formData.sealType === 'DEPT'" :span="12">
          <a-form-item label="所属部门" name="ownerDeptName">
            <a-input v-model:value="formData.ownerDeptName" placeholder="请输入所属部门" />
          </a-form-item>
        </a-col>
        <template v-if="formData.sealType === 'SIGNATURE'">
          <a-col :span="12">
            <a-form-item label="持有人用户ID" name="ownerUserId">
              <a-input v-model:value="formData.ownerUserId" placeholder="请输入持有人用户ID" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="持有人姓名" name="ownerUserName">
              <a-input v-model:value="formData.ownerUserName" placeholder="请输入持有人姓名" />
            </a-form-item>
          </a-col>
        </template>
        <a-col :span="12">
          <a-form-item label="印章宽度(px)" name="sealWidth">
            <a-input-number v-model:value="formData.sealWidth" :min="1" :max="500" style="width: 100%" placeholder="请输入印章宽度" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="印章高度(px)" name="sealHeight">
            <a-input-number v-model:value="formData.sealHeight" :min="1" :max="500" style="width: 100%" placeholder="请输入印章高度" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="算法" name="algorithm">
            <a-select
              v-model:value="formData.algorithm"
              placeholder="请选择算法"
              :options="algorithmOptions"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="密码" name="password">
            <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="备注" name="remark">
            <a-textarea
              v-model:value="formData.remark"
              placeholder="请输入备注"
              :rows="3"
              show-count
              :max-length="500"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  saveSeal,
  updateSeal,
  getSealDetail
} from '@/api/signature'
import type {
  DocSealVO,
  DocSealCreateDTO,
  DocSealUpdateDTO
} from '@/types/signature'
import { sealTypeOptions, algorithmOptions } from '@/types/signature'

const props = defineProps<{
  open: boolean
  editData: DocSealVO | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)

const isEdit = computed(() => !!props.editData)
const modalTitle = computed(() => (isEdit.value ? '编辑印章' : '新建印章'))

const formData = reactive<DocSealCreateDTO & { id?: number }>({
  sealName: '',
  sealType: '',
  sealCode: '',
  ownerUnitName: '',
  ownerDeptName: '',
  ownerUserId: '',
  ownerUserName: '',
  sealWidth: 100,
  sealHeight: 100,
  algorithm: 'SM2',
  password: '',
  remark: ''
})

const rules = {
  sealName: [{ required: true, message: '请输入印章名称', trigger: 'blur' }],
  sealType: [{ required: true, message: '请选择印章类型', trigger: 'change' }]
}

const handleSealTypeChange = () => {
  if (formData.sealType !== 'DEPT') {
    formData.ownerDeptName = ''
  }
  if (formData.sealType !== 'SIGNATURE') {
    formData.ownerUserId = ''
    formData.ownerUserName = ''
  }
}

const loadDetail = async (id: number) => {
  try {
    const res = await getSealDetail(id)
    if (res.code === 200 && res.data) {
      const data = res.data as DocSealVO
      Object.assign(formData, {
        id: data.id,
        sealName: data.sealName,
        sealType: data.sealType,
        sealCode: data.sealCode,
        ownerUnitName: data.ownerUnitName,
        ownerDeptName: data.ownerDeptName,
        ownerUserId: data.ownerUserId,
        ownerUserName: data.ownerUserName,
        sealWidth: data.sealWidth,
        sealHeight: data.sealHeight,
        algorithm: data.algorithm || 'SM2',
        password: data.password,
        remark: data.remark
      })
    } else {
      message.error(res.message || '获取印章详情失败')
    }
  } catch (error) {
    message.error('获取印章详情失败')
  }
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    sealName: '',
    sealType: '',
    sealCode: '',
    ownerUnitName: '',
    ownerDeptName: '',
    ownerUserId: '',
    ownerUserName: '',
    sealWidth: 100,
    sealHeight: 100,
    algorithm: 'SM2',
    password: '',
    remark: ''
  })
  formRef.value?.clearValidate()
}

const handleOk = async () => {
  try {
    await formRef.value?.validate()
    confirmLoading.value = true

    if (isEdit.value && formData.id) {
      const updateData: DocSealUpdateDTO = { ...formData } as DocSealUpdateDTO
      const res = await updateSeal(updateData)
      if (res.code === 200) {
        message.success('更新成功')
        emit('update:open', false)
        emit('success')
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const saveData: DocSealCreateDTO = { ...formData }
      const res = await saveSeal(saveData)
      if (res.code === 200) {
        message.success('保存成功')
        emit('update:open', false)
        emit('success')
      } else {
        message.error(res.message || '保存失败')
      }
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  emit('update:open', false)
}

watch(
  () => props.open,
  (val) => {
    if (val) {
      resetForm()
      if (isEdit.value && props.editData?.id) {
        loadDetail(props.editData.id)
      }
    }
  }
)
</script>
