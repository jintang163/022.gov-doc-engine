<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">借阅申请</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-card title="档案信息" :bordered="false" style="margin-bottom: 16px;" v-if="archiveDetail">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="公文标题">{{ archiveDetail.docTitle }}</a-descriptions-item>
        <a-descriptions-item label="发文字号">{{ archiveDetail.docNumber }}</a-descriptions-item>
        <a-descriptions-item label="档案类型">
          <a-tag>{{ archiveDetail.archiveType }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="保密等级">
          <a-tag color="red" v-if="archiveDetail.securityLevel === '绝密'">{{ archiveDetail.securityLevel }}</a-tag>
          <a-tag color="orange" v-else-if="archiveDetail.securityLevel === '机密'">{{ archiveDetail.securityLevel }}</a-tag>
          <a-tag color="gold" v-else-if="archiveDetail.securityLevel === '秘密'">{{ archiveDetail.securityLevel }}</a-tag>
          <a-tag v-else>{{ archiveDetail.securityLevel }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="借阅信息" :bordered="false">
      <a-form
        :model="formData"
        :rules="rules"
        layout="vertical"
        ref="formRef"
      >
        <a-form-item label="借阅原因" name="borrowReason">
          <a-textarea
            v-model:value="formData.borrowReason"
            :rows="4"
            placeholder="请输入借阅原因"
          />
        </a-form-item>

        <a-form-item label="借阅方式" name="borrowType">
          <a-select v-model:value="formData.borrowType" placeholder="请选择借阅方式">
            <a-select-option v-for="item in borrowTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" name="startDate">
              <a-date-picker
                v-model:value="formData.startDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择开始日期"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" name="endDate">
              <a-date-picker
                v-model:value="formData.endDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择结束日期"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSubmit" :loading="submitting">
              <template #icon><SendOutlined /></template>
              提交申请
            </a-button>
            <a-button @click="handleBack">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { ArrowLeftOutlined, SendOutlined } from '@ant-design/icons-vue'
import { applyBorrow, getArchiveDetail } from '@/api/archive'
import type { DocArchiveVO, DocBorrowApplyDTO } from '@/types/archive'
import { borrowTypeOptions } from '@/types/archive'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const archiveDetail = ref<DocArchiveVO | null>(null)
const submitting = ref(false)

const formData = reactive<DocBorrowApplyDTO>({
  archiveId: Number(route.query.archiveId) || 0,
  borrowReason: '',
  borrowType: 'online',
  startDate: '',
  endDate: '',
  remark: ''
})

const rules = {
  borrowReason: [{ required: true, message: '请输入借阅原因', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const loadArchiveDetail = async () => {
  if (!formData.archiveId) {
    message.error('缺少档案ID参数')
    return
  }
  try {
    archiveDetail.value = await getArchiveDetail(formData.archiveId)
  } catch (error) {
    message.error('加载档案信息失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    message.warning('请填写必填项')
    return
  }

  if (formData.endDate < formData.startDate) {
    message.warning('结束日期不能早于开始日期')
    return
  }

  submitting.value = true
  try {
    await applyBorrow(formData)
    message.success('借阅申请提交成功')
    router.back()
  } catch (error) {
    message.error('借阅申请提交失败')
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadArchiveDetail()
})
</script>

<style lang="less" scoped>
.page-container {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
  }
}
</style>
