<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">收文登记</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-card title="收文信息" :bordered="false">
      <a-form
        :model="formData"
        :rules="rules"
        layout="vertical"
        ref="formRef"
      >
        <a-form-item label="公文标题" name="docTitle">
          <a-input
            v-model:value="formData.docTitle"
            placeholder="请输入公文标题"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="来文单位" name="sourceUnit">
              <a-input
                v-model:value="formData.sourceUnit"
                placeholder="请输入来文单位"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="来文字号" name="sourceDocNumber">
              <a-input
                v-model:value="formData.sourceDocNumber"
                placeholder="请输入来文字号"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="文种分类" name="docType">
              <a-select v-model:value="formData.docType" placeholder="请选择文种分类" allowClear>
                <a-select-option v-for="item in incomingDocTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="密级" name="securityLevel">
              <a-select v-model:value="formData.securityLevel" placeholder="请选择密级" allowClear>
                <a-select-option v-for="item in incomingSecurityLevelOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="缓急程度" name="urgencyLevel">
              <a-select v-model:value="formData.urgencyLevel" placeholder="请选择缓急程度" allowClear>
                <a-select-option v-for="item in incomingUrgencyLevelOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="收文日期" name="receivedDate">
              <a-date-picker
                v-model:value="formData.receivedDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择收文日期"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="收文方式" name="receivedMethod">
              <a-select v-model:value="formData.receivedMethod" placeholder="请选择收文方式" allowClear>
                <a-select-option v-for="item in receivedMethodOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="份数" name="copies">
              <a-input-number
                v-model:value="formData.copies"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="页数" name="pages">
              <a-input-number
                v-model:value="formData.pages"
                :min="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="主题词" name="keyword">
          <a-input
            v-model:value="formData.keyword"
            placeholder="请输入主题词"
          />
        </a-form-item>

        <a-form-item label="摘要" name="abstractText">
          <a-textarea
            v-model:value="formData.abstractText"
            :rows="3"
            placeholder="请输入摘要"
          />
        </a-form-item>

        <a-form-item label="公文正文" name="docContent">
          <a-textarea
            v-model:value="formData.docContent"
            :rows="4"
            placeholder="请输入公文正文"
          />
        </a-form-item>

        <a-form-item label="附件信息" name="attachmentInfo">
          <a-input
            v-model:value="formData.attachmentInfo"
            placeholder="请输入附件信息"
          />
        </a-form-item>

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
              <template #icon><SaveOutlined /></template>
              提交登记
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
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons-vue'
import { registerIncoming } from '@/api/incoming'
import type { DocIncomingDTO } from '@/types/incoming'
import { incomingDocTypeOptions, incomingSecurityLevelOptions, incomingUrgencyLevelOptions, receivedMethodOptions } from '@/types/incoming'

const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive<DocIncomingDTO>({
  docTitle: '',
  sourceUnit: undefined,
  sourceDocNumber: undefined,
  docType: undefined,
  securityLevel: undefined,
  urgencyLevel: undefined,
  receivedDate: '',
  receivedMethod: undefined,
  copies: 1,
  pages: undefined,
  keyword: undefined,
  abstractText: undefined,
  docContent: undefined,
  attachmentInfo: undefined,
  remark: undefined
})

const rules = {
  docTitle: [{ required: true, message: '请输入公文标题', trigger: 'blur' }],
  receivedDate: [{ required: true, message: '请选择收文日期', trigger: 'change' }]
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    message.warning('请填写必填项')
    return
  }

  submitting.value = true
  try {
    await registerIncoming(formData)
    message.success('收文登记成功')
    router.push('/incoming')
  } catch (error) {
    message.error('收文登记失败')
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {})
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
