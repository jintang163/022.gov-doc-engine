<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">{{ isEditMode ? '编辑公文' : '新建公文' }}</h2>
      <div class="header-right">
        <div class="save-status" v-if="!isEditMode">
          <a-tag v-if="saveStatus === 'saving'" color="blue">
            <template #icon><LoadingOutlined spin /></template>
            保存中...
          </a-tag>
          <a-tag v-else-if="saveStatus === 'saved'" color="green">
            <template #icon><CheckCircleOutlined /></template>
            已保存 {{ lastSaveTime ? '于 ' + formatTime(lastSaveTime) : '' }}
          </a-tag>
          <a-tag v-else-if="saveStatus === 'error'" color="red">
            <template #icon><CloseCircleOutlined /></template>
            保存失败
          </a-tag>
          <a-tag v-else-if="saveStatus === 'unsaved'" color="orange">
            <template #icon><EditOutlined /></template>
            有未保存的更改
          </a-tag>
        </div>
        <a-space>
          <a-button @click="handleBack">
            <template #icon><ArrowLeftOutlined /></template>
            返回
          </a-button>
          <a-button @click="handleSaveDraft" :loading="savingDraft" v-if="!isEditMode">
            <template #icon><SaveOutlined /></template>
            保存草稿
          </a-button>
          <a-button @click="handleValidate" :loading="validating">
            <template #icon><AuditOutlined /></template>
            校验
          </a-button>
          <a-dropdown>
            <a-button>
              <template #icon><PlusOutlined /></template>
              插入占位符
              <template #icon><DownOutlined /></template>
            </a-button>
            <template #overlay>
              <a-menu @click="handleInsertPlaceholder">
                <a-menu-item v-for="(label, key) in presetPlaceholders" :key="key">
                  {{ label }} ({{ key }})
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button @click="handleReplacePlaceholders" :loading="replacing">
            <template #icon><SwapOutlined /></template>
            一键替换占位符
          </a-button>
          <a-button type="primary" @click="handlePreview" :loading="previewing">
            <template #icon><EyeOutlined /></template>
            预览红头
          </a-button>
          <a-button type="primary" @click="handleSaveDocument" :loading="savingDocument">
            <template #icon><CheckOutlined /></template>
            {{ isEditMode ? '更新公文' : '保存公文' }}
          </a-button>
        </a-space>
      </div>
    </div>

    <a-tabs v-model:activeKey="activeTab" class="main-tabs">
      <a-tab-pane key="content" tab="公文内容">
        <a-row :gutter="24">
          <a-col :span="14">
            <a-card title="基本信息" :bordered="false">
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
                    <a-form-item label="附件说明" name="attachmentInfo">
                      <a-input v-model:value="formData.attachmentInfo" placeholder="请输入附件说明" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="24">
                    <a-form-item label="正文内容" name="docContent">
                      <RichTextEditor
                        v-model="formData.docContent"
                        ref="richTextEditorRef"
                        :height="500"
                        placeholder="请输入公文正文内容..."
                        @change="handleContentChange"
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
          </a-col>
        </a-row>
      </a-tab-pane>

      <a-tab-pane key="attachment" tab="附件管理">
        <a-card :bordered="false">
          <template v-if="documentId > 0">
            <AttachmentManager
              :document-id="documentId"
              :disabled="false"
              @change="handleAttachmentChange"
            />
          </template>
          <a-alert
            v-else
            message="请先保存公文后再上传附件"
            description="附件需要关联到已创建的公文，请先点击\"保存公文\"创建公文，然后再上传附件。"
            type="warning"
            show-icon
          />
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="preview" tab="红头预览">
        <a-card :bordered="false">
          <div class="preview-container">
            <iframe
              v-if="previewHtml"
              :srcdoc="previewHtml"
              style="width: 100%; height: 800px; border: none; background: #fff;"
            />
            <a-spin v-else tip="加载预览中..." style="display: flex; justify-content: center; align-items: center; height: 800px;">
              <div></div>
            </a-spin>
          </div>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="validationModalVisible"
      title="公文校验结果"
      :width="600"
      :footer="null"
    >
      <div v-if="validationResult">
        <a-alert
          v-if="validationResult.valid"
          message="校验通过"
          description="公文内容符合规范要求。"
          type="success"
          show-icon
          style="margin-bottom: 16px;"
        />
        <a-alert
          v-else
          message="校验不通过"
          description="请根据以下提示修改后重新提交。"
          type="error"
          show-icon
          style="margin-bottom: 16px;"
        />

        <a-divider orientation="left" v-if="validationResult.errors.length > 0">
          <span style="color: #ff4d4f; font-weight: bold;">错误 ({{ validationResult.errors.length }})</span>
        </a-divider>
        <ul v-if="validationResult.errors.length > 0" class="validation-list">
          <li v-for="(error, index) in validationResult.errors" :key="'error-' + index" class="error-item">
            <CloseCircleOutlined style="color: #ff4d4f; margin-right: 8px;" />
            {{ error }}
          </li>
        </ul>

        <a-divider orientation="left" v-if="validationResult.warnings.length > 0">
          <span style="color: #faad14; font-weight: bold;">警告 ({{ validationResult.warnings.length }})</span>
        </a-divider>
        <ul v-if="validationResult.warnings.length > 0" class="validation-list">
          <li v-for="(warning, index) in validationResult.warnings" :key="'warning-' + index" class="warning-item">
            <ExclamationCircleOutlined style="color: #faad14; margin-right: 8px;" />
            {{ warning }}
          </li>
        </ul>
      </div>
    </a-modal>

    <a-modal
      v-model:open="placeholderResultVisible"
      title="占位符替换结果"
      :width="600"
      :footer="null"
    >
      <div v-if="placeholderResult">
        <a-alert
          message="替换完成"
          :description="'共找到 ' + placeholderResult.foundPlaceholders.length + ' 个占位符，成功替换 ' + placeholderResult.replacedPlaceholders.length + ' 个'"
          type="success"
          show-icon
          style="margin-bottom: 16px;"
        />

        <a-divider orientation="left" v-if="placeholderResult.replacedPlaceholders.length > 0">
          已替换
        </a-divider>
        <ul v-if="placeholderResult.replacedPlaceholders.length > 0" class="placeholder-list">
          <li v-for="(p, index) in placeholderResult.replacedPlaceholders" :key="'replaced-' + index">
            <CheckCircleOutlined style="color: #52c41a; margin-right: 8px;" />
            <span class="placeholder-key">{{ p }}</span>
            <span class="placeholder-arrow">→</span>
            <span class="placeholder-value">{{ placeholderResult.replacementMap[p] }}</span>
          </li>
        </ul>

        <a-divider orientation="left" v-if="placeholderResult.missingPlaceholders.length > 0">
          <span style="color: #faad14; font-weight: bold;">缺失的占位符值 ({{ placeholderResult.missingPlaceholders.length }})</span>
        </a-divider>
        <ul v-if="placeholderResult.missingPlaceholders.length > 0" class="placeholder-list">
          <li v-for="(p, index) in placeholderResult.missingPlaceholders" :key="'missing-' + index">
            <ExclamationCircleOutlined style="color: #faad14; margin-right: 8px;" />
            <span class="placeholder-key">{{ p }}</span>
            <span class="placeholder-note">（未找到对应的值，保持原样）</span>
          </li>
        </ul>
      </div>
    </a-modal>

    <a-modal
      v-model:open="draftRestoreVisible"
      title="发现未保存的草稿"
      :width="500"
      @ok="handleRestoreDraft"
      @cancel="handleDiscardDraft"
      okText="恢复草稿"
      cancelText="放弃草稿"
      :confirm-loading="restoringDraft"
    >
      <div v-if="latestDraft">
        <p>发现该模板有一份最近保存的草稿：</p>
        <a-descriptions :column="1" size="small" bordered>
          <a-descriptions-item label="标题">{{ latestDraft.docTitle || '（无标题）' }}</a-descriptions-item>
          <a-descriptions-item label="保存时间">{{ formatTime(latestDraft.lastSaveTime || latestDraft.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="类型">{{ latestDraft.autoSave ? '自动保存' : '手动保存' }}</a-descriptions-item>
        </a-descriptions>
        <p style="margin-top: 16px; color: #faad14;">
          <ExclamationCircleOutlined /> 是否要恢复这份草稿？恢复后将覆盖当前输入的内容。
        </p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted, computed, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  EyeOutlined,
  SaveOutlined,
  ArrowLeftOutlined,
  LoadingOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EditOutlined,
  AuditOutlined,
  PlusOutlined,
  DownOutlined,
  SwapOutlined,
  CheckOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import type { IDomEditor } from '@wangeditor/editor'
import RichTextEditor from '@/components/RichTextEditor.vue'
import AttachmentManager from '@/components/AttachmentManager.vue'
import {
  getTemplateDetail,
  createDocumentFromTemplate,
  getDocumentPreviewHtml,
  getDocumentDetail,
  updateDocument,
  validateDocument,
  getLatestDraft,
  saveDraft,
  autoSaveDraft,
  deleteDraft,
  replacePlaceholders,
  getPresetPlaceholders
} from '@/api/template'
import type {
  DocTemplateVO,
  DocDocumentCreateDTO,
  DocDocumentUpdateDTO,
  DocDraftSaveDTO,
  DocDocumentValidationDTO,
  DocPlaceholderReplaceDTO,
  DocValidationResultVO,
  DocPlaceholderResultVO,
  DocAttachment,
  DocDraft
} from '@/types/template'
import { securityLevelOptions, urgencyLevelOptions } from '@/types/template'

const route = useRoute()
const router = useRouter()

type SaveStatus = 'saved' | 'saving' | 'error' | 'unsaved' | 'idle'

const formRef = ref<FormInstance>()
const richTextEditorRef = ref<InstanceType<typeof RichTextEditor> | null>(null)
const templateInfo = ref<DocTemplateVO | null>(null)
const previewHtml = ref('')
const activeTab = ref('content')

const isEditMode = computed(() => !!route.params.id)
const documentId = ref<number>(0)
const draftId = ref<number | null>(null)

const formData = reactive<DocDocumentCreateDTO>({
  templateId: Number(route.params.templateId) || 0,
  docTitle: '',
  docNumber: '',
  docType: '',
  securityLevel: '普通',
  urgencyLevel: '普通',
  mainSendDept: '',
  copySendDept: '',
  signer: '',
  signDate: '',
  writtenDate: '',
  docContent: '',
  attachmentInfo: '',
  fieldData: {},
  remark: ''
})

const rules = {
  docTitle: [{ required: true, message: '请输入公文标题', trigger: 'blur' }]
}

const saveStatus = ref<SaveStatus>('idle')
const lastSaveTime = ref<string>('')
const latestDraft = ref<DocDraft | null>(null)
const draftRestoreVisible = ref(false)
const restoringDraft = ref(false)

const validationModalVisible = ref(false)
const validationResult = ref<DocValidationResultVO | null>(null)
const validating = ref(false)

const presetPlaceholders = ref<Record<string, string>>({})
const placeholderResultVisible = ref(false)
const placeholderResult = ref<DocPlaceholderResultVO | null>(null)
const replacing = ref(false)

const savingDraft = ref(false)
const savingDocument = ref(false)
const previewing = ref(false)

const autoSaveInterval = ref<number | null>(null)
const hasUnsavedChanges = ref(false)
const originalFormData = ref<string>('')

const debounce = <T extends (...args: any[]) => any>(fn: T, delay: number): ((...args: Parameters<T>) => void) => {
  let timeoutId: number | null = null
  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    timeoutId = window.setTimeout(() => fn(...args), delay)
  }
}

const formatTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const getFormDataSnapshot = (): string => {
  return JSON.stringify(formData)
}

const checkUnsavedChanges = (): boolean => {
  return hasUnsavedChanges.value || getFormDataSnapshot() !== originalFormData.value
}

const handleContentChange = () => {
  markAsUnsaved()
}

const markAsUnsaved = () => {
  if (saveStatus.value !== 'saving') {
    saveStatus.value = 'unsaved'
  }
  hasUnsavedChanges.value = true
}

const debouncedAutoSave = debounce(async () => {
  if (!isEditMode.value && checkUnsavedChanges()) {
    await handleAutoSave()
  }
}, 1000)

const handleAutoSave = async () => {
  if (isEditMode.value) return

  saveStatus.value = 'saving'

  try {
    const draftData: DocDraftSaveDTO = {
      id: draftId.value || undefined,
      documentId: documentId.value > 0 ? documentId.value : undefined,
      templateId: formData.templateId,
      docTitle: formData.docTitle,
      docNumber: formData.docNumber,
      docType: formData.docType,
      securityLevel: formData.securityLevel,
      urgencyLevel: formData.urgencyLevel,
      mainSendDept: formData.mainSendDept,
      copySendDept: formData.copySendDept,
      signer: formData.signer,
      signDate: formData.signDate,
      writtenDate: formData.writtenDate,
      docContent: formData.docContent,
      attachmentInfo: formData.attachmentInfo,
      fieldData: formData.fieldData,
      autoSave: 1,
      remark: formData.remark
    }

    const result = await autoSaveDraft(draftData)
    draftId.value = (result as any).data || result
    saveStatus.value = 'saved'
    lastSaveTime.value = new Date().toISOString()
    hasUnsavedChanges.value = false
    originalFormData.value = getFormDataSnapshot()
  } catch (error) {
    console.error('自动保存失败:', error)
    saveStatus.value = 'error'
  }
}

const startAutoSaveTimer = () => {
  if (autoSaveInterval.value) {
    clearInterval(autoSaveInterval.value)
  }
  autoSaveInterval.value = window.setInterval(() => {
    if (!isEditMode.value && checkUnsavedChanges()) {
      handleAutoSave()
    }
  }, 30000)
}

const loadTemplateInfo = async () => {
  try {
    const templateId = Number(route.params.templateId) || formData.templateId
    if (!templateId) return

    templateInfo.value = await getTemplateDetail(templateId)
    if (templateInfo.value) {
      formData.templateId = templateId
      formData.docType = templateInfo.value.templateType
      if (!formData.docNumber) {
        formData.docNumber = templateInfo.value.header?.documentNumberPrefix
          ? `${templateInfo.value.header.documentNumberPrefix}${templateInfo.value.header.documentNumberYear || ''}X号`
          : ''
      }
      if (!isEditMode.value) {
        updatePreview()
      }
    }
  } catch (error) {
    console.error('加载模板信息失败:', error)
    message.error('加载模板信息失败')
  }
}

const loadDocumentDetail = async (id: number) => {
  try {
    const doc = await getDocumentDetail(id)
    documentId.value = id

    formData.templateId = doc.templateId
    formData.docTitle = doc.docTitle || ''
    formData.docNumber = doc.docNumber || ''
    formData.docType = doc.docType || ''
    formData.securityLevel = doc.securityLevel || '普通'
    formData.urgencyLevel = doc.urgencyLevel || '普通'
    formData.mainSendDept = doc.mainSendDept || ''
    formData.copySendDept = doc.copySendDept || ''
    formData.signer = doc.signer || ''
    formData.signDate = doc.signDate || ''
    formData.writtenDate = doc.writtenDate || ''
    formData.docContent = doc.docContent || ''
    formData.attachmentInfo = doc.attachmentInfo || ''
    formData.fieldData = doc.fieldData || {}
    formData.remark = doc.remark || ''

    originalFormData.value = getFormDataSnapshot()
    saveStatus.value = 'saved'
    lastSaveTime.value = doc.updateTime || doc.createTime

    await loadTemplateInfo()
    updatePreview()
  } catch (error) {
    console.error('加载公文详情失败:', error)
    message.error('加载公文详情失败')
  }
}

const loadLatestDraft = async () => {
  if (isEditMode.value) return

  try {
    const templateId = Number(route.params.templateId) || formData.templateId
    if (!templateId) return

    const draft = await getLatestDraft(templateId)
    if (draft && draft.id) {
      latestDraft.value = draft
      draftRestoreVisible.value = true
    }
  } catch (error) {
    console.error('加载草稿失败:', error)
  }
}

const handleRestoreDraft = async () => {
  if (!latestDraft.value) return

  restoringDraft.value = true
  try {
    draftId.value = latestDraft.value.id
    formData.docTitle = latestDraft.value.docTitle || ''
    formData.docNumber = latestDraft.value.docNumber || ''
    formData.docType = latestDraft.value.docType || ''
    formData.securityLevel = latestDraft.value.securityLevel || '普通'
    formData.urgencyLevel = latestDraft.value.urgencyLevel || '普通'
    formData.mainSendDept = latestDraft.value.mainSendDept || ''
    formData.copySendDept = latestDraft.value.copySendDept || ''
    formData.signer = latestDraft.value.signer || ''
    formData.signDate = latestDraft.value.signDate || ''
    formData.writtenDate = latestDraft.value.writtenDate || ''
    formData.docContent = latestDraft.value.docContent || ''
    formData.attachmentInfo = latestDraft.value.attachmentInfo || ''
    formData.remark = latestDraft.value.remark || ''

    try {
      formData.fieldData = latestDraft.value.fieldData
        ? JSON.parse(latestDraft.value.fieldData)
        : {}
    } catch {
      formData.fieldData = {}
    }

    originalFormData.value = getFormDataSnapshot()
    saveStatus.value = 'saved'
    lastSaveTime.value = latestDraft.value.lastSaveTime || latestDraft.value.createTime
    hasUnsavedChanges.value = false

    draftRestoreVisible.value = false
    message.success('草稿已恢复')
    updatePreview()
  } catch (error) {
    console.error('恢复草稿失败:', error)
    message.error('恢复草稿失败')
  } finally {
    restoringDraft.value = false
  }
}

const handleDiscardDraft = async () => {
  if (latestDraft.value?.id) {
    try {
      await deleteDraft(latestDraft.value.id)
    } catch (error) {
      console.error('删除草稿失败:', error)
    }
  }
  draftRestoreVisible.value = false
}

const loadPresetPlaceholders = async () => {
  try {
    const result = await getPresetPlaceholders()
    presetPlaceholders.value = (result as any).data || result
  } catch (error) {
    console.error('加载预置占位符失败:', error)
  }
}

const getEditorInstance = (): IDomEditor | null => {
  if (richTextEditorRef.value) {
    return (richTextEditorRef.value as any).editorRef || null
  }
  return null
}

const handleInsertPlaceholder = ({ key }: { key: string }) => {
  const editor = getEditorInstance()
  if (editor) {
    editor.insertText(key)
    markAsUnsaved()
  } else {
    formData.docContent = (formData.docContent || '') + key
    markAsUnsaved()
  }
}

const handleReplacePlaceholders = async () => {
  if (!formData.docContent) {
    message.warning('正文内容为空，无需替换')
    return
  }

  replacing.value = true
  try {
    const replaceData: DocPlaceholderReplaceDTO = {
      content: formData.docContent
    }

    const result = await replacePlaceholders(replaceData)
    placeholderResult.value = (result as any).data || result

    if (placeholderResult.value.content) {
      formData.docContent = placeholderResult.value.content
      markAsUnsaved()
      updatePreview()
    }

    placeholderResultVisible.value = true
  } catch (error) {
    console.error('替换占位符失败:', error)
    message.error('替换占位符失败')
  } finally {
    replacing.value = false
  }
}

const handleValidate = async (): Promise<boolean> => {
  validating.value = true
  try {
    const validateData: DocDocumentValidationDTO = {
      templateId: formData.templateId,
      docTitle: formData.docTitle,
      docContent: formData.docContent,
      documentData: { ...formData }
    }

    const result = await validateDocument(validateData)
    validationResult.value = (result as any).data || result
    validationModalVisible.value = true

    return validationResult.value.valid
  } catch (error) {
    console.error('校验失败:', error)
    message.error('校验失败')
    return false
  } finally {
    validating.value = false
  }
}

const updatePreview = async () => {
  try {
    const templateId = formData.templateId || Number(route.params.templateId)
    if (!templateId) return

    previewHtml.value = await getDocumentPreviewHtml(templateId, formData)
  } catch (error) {
    console.error('生成预览失败:', error)
  }
}

const handlePreview = () => {
  activeTab.value = 'preview'
  previewing.value = true
  updatePreview().finally(() => {
    previewing.value = false
  })
  message.success('预览已更新')
}

const handleSaveDraft = async () => {
  if (isEditMode.value) return

  savingDraft.value = true
  saveStatus.value = 'saving'

  try {
    const draftData: DocDraftSaveDTO = {
      id: draftId.value || undefined,
      documentId: documentId.value > 0 ? documentId.value : undefined,
      templateId: formData.templateId,
      docTitle: formData.docTitle,
      docNumber: formData.docNumber,
      docType: formData.docType,
      securityLevel: formData.securityLevel,
      urgencyLevel: formData.urgencyLevel,
      mainSendDept: formData.mainSendDept,
      copySendDept: formData.copySendDept,
      signer: formData.signer,
      signDate: formData.signDate,
      writtenDate: formData.writtenDate,
      docContent: formData.docContent,
      attachmentInfo: formData.attachmentInfo,
      fieldData: formData.fieldData,
      autoSave: 0,
      remark: formData.remark
    }

    const result = await saveDraft(draftData)
    draftId.value = (result as any).data || result
    saveStatus.value = 'saved'
    lastSaveTime.value = new Date().toISOString()
    hasUnsavedChanges.value = false
    originalFormData.value = getFormDataSnapshot()
    message.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    saveStatus.value = 'error'
    message.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const handleSaveDocument = async () => {
  try {
    await formRef.value?.validate()
  } catch (error) {
    message.warning('请填写必填项')
    return
  }

  const isValid = await handleValidate()
  if (!isValid) {
    message.error('校验不通过，请修改后重新保存')
    return
  }

  savingDocument.value = true
  try {
    if (isEditMode.value) {
      const updateData: DocDocumentUpdateDTO = {
        id: documentId.value,
        docTitle: formData.docTitle,
        docNumber: formData.docNumber,
        docType: formData.docType,
        securityLevel: formData.securityLevel,
        urgencyLevel: formData.urgencyLevel,
        mainSendDept: formData.mainSendDept,
        copySendDept: formData.copySendDept,
        signer: formData.signer,
        signDate: formData.signDate,
        writtenDate: formData.writtenDate,
        docContent: formData.docContent,
        attachmentInfo: formData.attachmentInfo,
        fieldData: formData.fieldData,
        remark: formData.remark
      }

      await updateDocument(updateData)
      message.success('公文更新成功')

      if (draftId.value) {
        try {
          await deleteDraft(draftId.value)
        } catch (e) {
          console.warn('删除草稿失败:', e)
        }
      }

      hasUnsavedChanges.value = false
      originalFormData.value = getFormDataSnapshot()
      saveStatus.value = 'saved'
      lastSaveTime.value = new Date().toISOString()
    } else {
      const createData: DocDocumentCreateDTO = {
        templateId: formData.templateId,
        docTitle: formData.docTitle,
        docNumber: formData.docNumber,
        docType: formData.docType,
        securityLevel: formData.securityLevel,
        urgencyLevel: formData.urgencyLevel,
        mainSendDept: formData.mainSendDept,
        copySendDept: formData.copySendDept,
        signer: formData.signer,
        signDate: formData.signDate,
        writtenDate: formData.writtenDate,
        docContent: formData.docContent,
        attachmentInfo: formData.attachmentInfo,
        fieldData: formData.fieldData,
        remark: formData.remark
      }

      const result = await createDocumentFromTemplate(createData)
      const doc = (result as any).data || result
      documentId.value = doc.id

      message.success('公文创建成功，现在可以上传附件了')

      if (draftId.value) {
        try {
          await deleteDraft(draftId.value)
          draftId.value = null
        } catch (e) {
          console.warn('删除草稿失败:', e)
        }
      }

      hasUnsavedChanges.value = false
      originalFormData.value = getFormDataSnapshot()
      saveStatus.value = 'saved'
      lastSaveTime.value = new Date().toISOString()

      Modal.confirm({
        title: '公文创建成功',
        content: '是否切换到附件管理页面上传附件？',
        okText: '去上传',
        cancelText: '稍后再说',
        onOk: () => {
          activeTab.value = 'attachment'
        }
      })
    }
  } catch (error) {
    console.error('保存公文失败:', error)
    message.error('保存公文失败')
  } finally {
    savingDocument.value = false
  }
}

const handleAttachmentChange = (attachments: DocAttachment[]) => {
  console.log('附件列表变更:', attachments)
}

const handleBack = () => {
  if (checkUnsavedChanges()) {
    Modal.confirm({
      title: '您有未保存的更改',
      content: '确定要离开吗？未保存的更改将会丢失。',
      okText: '确定离开',
      okType: 'danger',
      cancelText: '继续编辑',
      onOk: () => {
        router.push('/document')
      }
    })
  } else {
    router.push('/document')
  }
}

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (checkUnsavedChanges()) {
    e.preventDefault()
    e.returnValue = '您有未保存的更改，确定要离开吗？'
    return e.returnValue
  }
}

watch(
  () => [formData.docTitle, formData.docNumber, formData.mainSendDept, formData.docContent, formData.signer],
  () => {
    markAsUnsaved()
    updatePreview()
  },
  { deep: true }
)

onMounted(async () => {
  if (isEditMode.value) {
    const docId = Number(route.params.id)
    if (docId) {
      await loadDocumentDetail(docId)
    }
  } else {
    await loadTemplateInfo()
    await loadPresetPlaceholders()
    await loadLatestDraft()
    originalFormData.value = getFormDataSnapshot()
    startAutoSaveTimer()
  }

  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  if (autoSaveInterval.value) {
    clearInterval(autoSaveInterval.value)
  }
  window.removeEventListener('beforeunload', handleBeforeUnload)
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

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .save-status {
    display: flex;
    align-items: center;
  }
}

.main-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  :deep(.ant-tabs-nav) {
    margin-bottom: 0;
  }

  :deep(.ant-tabs-content-holder) {
    padding-top: 16px;
  }
}

.preview-container {
  background: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}

.validation-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 4px;
    font-size: 14px;
    line-height: 1.6;
  }

  .error-item {
    background: #fff2f0;
    border: 1px solid #ffccc7;
    color: #ff4d4f;
  }

  .warning-item {
    background: #fffbe6;
    border: 1px solid #ffe58f;
    color: #d48806;
  }
}

.placeholder-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 4px;
    font-size: 14px;
    line-height: 1.6;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .placeholder-key {
    font-family: 'Consolas', 'Monaco', monospace;
    background: #f5f5f5;
    padding: 2px 8px;
    border-radius: 4px;
    color: #1890ff;
  }

  .placeholder-arrow {
    color: #8c8c8c;
  }

  .placeholder-value {
    color: #52c41a;
    font-weight: 500;
  }

  .placeholder-note {
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
