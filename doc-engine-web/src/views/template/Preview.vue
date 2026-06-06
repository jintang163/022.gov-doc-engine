<template>
  <div class="template-preview">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <a-button @click="handleBack">
            <template #icon><ArrowLeftOutlined /></template>
            返回
          </a-button>
          <span>公文模板预览</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新预览
          </a-button>
          <a-button @click="handleExportHtml">
            <template #icon><DownloadOutlined /></template>
            导出HTML
          </a-button>
          <a-button @click="handleOpenNewWindow">
            <template #icon><ExportOutlined /></template>
            在新窗口打开
          </a-button>
        </a-space>
      </template>

      <div class="preview-layout">
        <div class="info-panel">
          <a-card title="模板基本信息" :bordered="false" size="small">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="模板编码">
                {{ templateDetail?.templateCode }}
              </a-descriptions-item>
              <a-descriptions-item label="模板名称">
                {{ templateDetail?.templateName }}
              </a-descriptions-item>
              <a-descriptions-item label="模板类型">
                {{ templateDetail?.templateType }}
              </a-descriptions-item>
              <a-descriptions-item label="模板分类">
                {{ templateDetail?.templateCategory }}
              </a-descriptions-item>
              <a-descriptions-item label="版本号">
                {{ templateDetail?.version }}
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="getStatusColor(templateDetail?.status)">
                  {{ getStatusLabel(templateDetail?.status) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ templateDetail?.createTime }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <a-card title="红头样式摘要" :bordered="false" size="small" style="margin-top: 16px;">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="单位名称">
                {{ templateDetail?.header?.unitName }}
              </a-descriptions-item>
              <a-descriptions-item label="发文字号前缀">
                {{ templateDetail?.header?.documentNumberPrefix }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </div>

        <div class="preview-panel">
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
            <a-tab-pane key="template" tab="模板预览">
              <div class="preview-container">
                <iframe
                  v-if="useIframe"
                  :src="previewUrl"
                  class="preview-iframe"
                  frameborder="0"
                ></iframe>
                <div
                  v-else
                  class="preview-html"
                  v-html="previewHtml"
                ></div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="document" tab="模拟公文预览">
              <div class="document-preview-layout">
                <a-card title="模拟数据" :bordered="false" size="small" class="document-form">
                  <a-form
                    :model="documentForm"
                    :label-col="{ span: 6 }"
                    :wrapper-col="{ span: 18 }"
                    layout="horizontal"
                  >
                    <a-form-item label="公文标题" required>
                      <a-input
                        v-model:value="documentForm.docTitle"
                        placeholder="请输入公文标题"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="发文字号">
                      <a-input
                        v-model:value="documentForm.docNumber"
                        placeholder="请输入发文字号"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="主送机关">
                      <a-input
                        v-model:value="documentForm.mainSendDept"
                        placeholder="请输入主送机关"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="抄送机关">
                      <a-input
                        v-model:value="documentForm.copySendDept"
                        placeholder="请输入抄送机关"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="签发人">
                      <a-input
                        v-model:value="documentForm.signer"
                        placeholder="请输入签发人"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="成文日期">
                      <a-date-picker
                        v-model:value="documentForm.writtenDate"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                      />
                    </a-form-item>
                    <a-form-item label="正文内容">
                      <a-textarea
                        v-model:value="documentForm.docContent"
                        placeholder="请输入正文内容"
                        :rows="10"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item label="附件">
                      <a-input
                        v-model:value="documentForm.attachmentInfo"
                        placeholder="请输入附件信息"
                        allow-clear
                      />
                    </a-form-item>
                    <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
                      <a-space>
                        <a-button type="primary" @click="handleDocumentPreview">
                          <template #icon><EyeOutlined /></template>
                          预览
                        </a-button>
                        <a-button @click="handleResetForm">
                          <template #icon><ReloadOutlined /></template>
                          重置
                        </a-button>
                      </a-space>
                    </a-form-item>
                  </a-form>
                </a-card>
                <div class="document-preview-container">
                  <div
                    class="preview-html"
                    v-html="documentPreviewHtml"
                  ></div>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  DownloadOutlined,
  ExportOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import {
  getTemplateDetail,
  getTemplatePreviewHtml,
  getDocumentPreviewHtml,
  getTemplatePreviewUrl
} from '@/api/template'
import type { DocTemplateVO, DocDocumentCreateDTO } from '@/types/template'
import { statusOptions } from '@/types/template'

const route = useRoute()
const router = useRouter()

const templateId = computed(() => Number(route.params.id))
const useIframe = ref(true)
const activeTab = ref('template')
const loading = ref(false)
const templateDetail = ref<DocTemplateVO | null>(null)
const previewHtml = ref('')
const documentPreviewHtml = ref('')

const previewUrl = computed(() => getTemplatePreviewUrl(templateId.value))

const documentForm = reactive<DocDocumentCreateDTO & { writtenDate?: Dayjs }>({
  templateId: templateId.value,
  docTitle: '',
  docNumber: '',
  mainSendDept: '',
  copySendDept: '',
  signer: '',
  writtenDate: undefined,
  docContent: '',
  attachmentInfo: ''
})

const getStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.label : '未知'
}

const getStatusColor = (status?: number) => {
  if (status === undefined || status === null) return 'default'
  const item = statusOptions.find((opt) => opt.value === status)
  return item ? item.color : 'default'
}

const fetchTemplateDetail = async () => {
  loading.value = true
  try {
    const res = await getTemplateDetail(templateId.value)
    if (res.code === 0) {
      templateDetail.value = res.data
    } else {
      message.error(res.message || '获取模板信息失败')
    }
  } catch (error) {
    message.error('获取模板信息失败')
  } finally {
    loading.value = false
  }
}

const fetchTemplatePreviewHtml = async () => {
  try {
    const res = await getTemplatePreviewHtml(templateId.value)
    if (res.code === 0) {
      previewHtml.value = res.data
    } else {
      message.error(res.message || '获取模板预览失败')
    }
  } catch (error) {
    message.error('获取模板预览失败')
  }
}

const fetchDocumentPreviewHtml = async () => {
  if (!documentForm.docTitle) {
    message.warning('请输入公文标题')
    return
  }

  const data: DocDocumentCreateDTO = {
    templateId: templateId.value,
    docTitle: documentForm.docTitle,
    docNumber: documentForm.docNumber,
    mainSendDept: documentForm.mainSendDept,
    copySendDept: documentForm.copySendDept,
    signer: documentForm.signer,
    writtenDate: documentForm.writtenDate
      ? dayjs(documentForm.writtenDate).format('YYYY-MM-DD')
      : undefined,
    docContent: documentForm.docContent,
    attachmentInfo: documentForm.attachmentInfo
  }

  try {
    const res = await getDocumentPreviewHtml(templateId.value, data)
    if (res.code === 0) {
      documentPreviewHtml.value = res.data
    } else {
      message.error(res.message || '获取公文预览失败')
    }
  } catch (error) {
    message.error('获取公文预览失败')
  }
}

const handleBack = () => {
  router.push('/template')
}

const handleRefresh = () => {
  if (activeTab.value === 'template') {
    if (useIframe.value) {
      const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
      if (iframe) {
        iframe.contentWindow?.location.reload()
      }
    } else {
      fetchTemplatePreviewHtml()
    }
  } else {
    fetchDocumentPreviewHtml()
  }
  message.success('刷新成功')
}

const handleExportHtml = () => {
  let htmlContent = ''
  if (activeTab.value === 'template') {
    htmlContent = previewHtml.value
  } else {
    htmlContent = documentPreviewHtml.value
  }

  if (!htmlContent) {
    message.warning('暂无预览内容可导出')
    return
  }

  const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${templateDetail.value?.templateName || 'preview'}.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('导出成功')
}

const handleOpenNewWindow = () => {
  window.open(previewUrl.value, '_blank')
}

const handleTabChange = (key: string) => {
  if (key === 'template' && !useIframe.value && !previewHtml.value) {
    fetchTemplatePreviewHtml()
  }
}

const handleDocumentPreview = () => {
  fetchDocumentPreviewHtml()
}

const handleResetForm = () => {
  documentForm.docTitle = ''
  documentForm.docNumber = ''
  documentForm.mainSendDept = ''
  documentForm.copySendDept = ''
  documentForm.signer = ''
  documentForm.writtenDate = undefined
  documentForm.docContent = ''
  documentForm.attachmentInfo = ''
  documentPreviewHtml.value = ''
}

onMounted(() => {
  fetchTemplateDetail()
  if (!useIframe.value) {
    fetchTemplatePreviewHtml()
  }
})
</script>

<style scoped lang="less">
.template-preview {
  padding: 16px;

  .preview-layout {
    display: flex;
    gap: 16px;
    height: calc(100vh - 200px);
    min-height: 600px;
  }

  .info-panel {
    width: 320px;
    flex-shrink: 0;
  }

  .preview-panel {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .preview-container {
    height: 100%;
    min-height: 500px;
    background-color: #f0f2f5;
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    overflow: auto;
  }

  .preview-iframe {
    width: 100%;
    height: 100%;
    min-height: 800px;
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .preview-html {
    width: 100%;
    max-width: 800px;
    min-height: 800px;
    background-color: #fff;
    padding: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    overflow: auto;

    :deep(img) {
      max-width: 100%;
      height: auto;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
    }

    :deep(p) {
      margin: 0 0 1em 0;
    }
  }

  .document-preview-layout {
    display: flex;
    gap: 16px;
    height: 100%;
  }

  .document-form {
    width: 400px;
    flex-shrink: 0;
    overflow-y: auto;
  }

  .document-preview-container {
    flex: 1;
    min-width: 0;
    background-color: #f0f2f5;
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    overflow: auto;
  }
}
</style>
