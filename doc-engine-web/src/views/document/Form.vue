<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">新建公文</h2>
      <a-space>
        <a-button @click="handleBack">返回</a-button>
        <a-button type="primary" @click="handlePreview">
          <template #icon><EyeOutlined /></template>
          预览红头
        </a-button>
        <a-button type="primary" @click="handleSave">
          <template #icon><SaveOutlined /></template>
          保存公文
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="公文内容" :bordered="false">
          <a-form
            :model="formData"
            :rules="rules"
            layout="vertical"
            ref="formRef"
          >
            <a-row :gutter="16">
              <a-col :span="24">
                <a-form-item label="公文标题" name="docTitle">
                  <a-input
                    v-model:value="formData.docTitle"
                    placeholder="请输入公文标题"
                    size="large"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="发文字号" name="docNumber">
                  <a-input v-model:value="formData.docNumber" placeholder="请输入发文字号" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="成文日期" name="writtenDate">
                  <a-date-picker
                    v-model:value="formData.writtenDate"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    placeholder="请选择成文日期"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="保密等级" name="securityLevel">
                  <a-select v-model:value="formData.securityLevel" placeholder="请选择保密等级">
                    <a-select-option v-for="item in securityLevelOptions" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="紧急程度" name="urgencyLevel">
                  <a-select v-model:value="formData.urgencyLevel" placeholder="请选择紧急程度">
                    <a-select-option v-for="item in urgencyLevelOptions" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="主送机关" name="mainSendDept">
                  <a-input v-model:value="formData.mainSendDept" placeholder="请输入主送机关" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="抄送机关" name="copySendDept">
                  <a-input v-model:value="formData.copySendDept" placeholder="请输入抄送机关" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="签发人" name="signer">
                  <a-input v-model:value="formData.signer" placeholder="请输入签发人" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="附件" name="attachmentInfo">
                  <a-input v-model:value="formData.attachmentInfo" placeholder="请输入附件说明" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="正文内容" name="docContent">
                  <a-textarea
                    v-model:value="formData.docContent"
                    :rows="15"
                    placeholder="请输入公文正文内容..."
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="备注" name="remark">
                  <a-textarea
                    v-model:value="formData.remark"
                    :rows="3"
                    placeholder="请输入备注信息"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="模板信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="templateInfo">
            <a-descriptions-item label="模板名称">{{ templateInfo.templateName }}</a-descriptions-item>
            <a-descriptions-item label="模板类型">{{ templateInfo.templateType }}</a-descriptions-item>
            <a-descriptions-item label="模板分类">{{ templateInfo.templateCategory }}</a-descriptions-item>
            <a-descriptions-item label="模板版本">v{{ templateInfo.version }}</a-descriptions-item>
            <a-descriptions-item label="发文单位">{{ templateInfo.header?.unitName }}</a-descriptions-item>
            <a-descriptions-item label="发文字号前缀">{{ templateInfo.header?.documentNumberPrefix }}</a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 6 }" />
        </a-card>

        <a-card title="红头预览" :bordered="false">
          <div class="preview-container">
            <iframe
              v-if="previewHtml"
              :srcdoc="previewHtml"
              style="width: 100%; height: 600px; border: none; background: #fff;"
            />
            <a-spin v-else tip="加载预览中..." style="display: flex; justify-content: center; align-items: center; height: 600px;">
              <div></div>
            </a-spin>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EyeOutlined, SaveOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import { getTemplateDetail, createDocumentFromTemplate, getDocumentPreviewHtml } from '@/api/template'
import type { DocTemplateVO, DocDocumentCreateDTO } from '@/types/template'
import { securityLevelOptions, urgencyLevelOptions } from '@/types/template'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const templateInfo = ref<DocTemplateVO | null>(null)
const previewHtml = ref('')

const formData = reactive<DocDocumentCreateDTO>({
  templateId: Number(route.params.templateId),
  docTitle: '',
  docNumber: '',
  securityLevel: '普通',
  urgencyLevel: '普通',
  mainSendDept: '',
  copySendDept: '',
  signer: '',
  writtenDate: '',
  docContent: '',
  attachmentInfo: '',
  remark: ''
})

const rules = {
  docTitle: [{ required: true, message: '请输入公文标题', trigger: 'blur' }]
}

const loadTemplateInfo = async () => {
  try {
    const templateId = Number(route.params.templateId)
    templateInfo.value = await getTemplateDetail(templateId)
    if (templateInfo.value) {
      formData.docType = templateInfo.value.templateType
      formData.docNumber = templateInfo.value.header?.documentNumberPrefix
        ? `${templateInfo.value.header.documentNumberPrefix}${templateInfo.value.header.documentNumberYear || ''}X号`
        : ''
      updatePreview()
    }
  } catch (error) {
    console.error('加载模板信息失败:', error)
    message.error('加载模板信息失败')
  }
}

const updatePreview = async () => {
  try {
    const templateId = Number(route.params.templateId)
    previewHtml.value = await getDocumentPreviewHtml(templateId, formData)
  } catch (error) {
    console.error('生成预览失败:', error)
  }
}

const handlePreview = () => {
  updatePreview()
  message.success('预览已更新')
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    const result = await createDocumentFromTemplate(formData)
    message.success('公文创建成功')
    router.push('/document')
  } catch (error) {
    console.error('保存公文失败:', error)
  }
}

const handleBack = () => {
  router.push('/document')
}

watch(
  () => [formData.docTitle, formData.docNumber, formData.mainSendDept, formData.docContent, formData.signer],
  () => {
    updatePreview()
  },
  { deep: true }
)

onMounted(() => {
  loadTemplateInfo()
})
</script>

<style lang="less" scoped>
.preview-container {
  background: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}
</style>
