<template>
  <div class="attachment-manager">
    <a-upload-dragger
      v-if="!disabled"
      :multiple="true"
      :accept="accept"
      :before-upload="handleBeforeUpload"
      :custom-request="handleCustomRequest"
      :show-upload-list="false"
      @drop="handleDrop"
    >
      <p class="ant-upload-drag-icon">
        <inbox-outlined />
      </p>
      <p class="ant-upload-text">点击或拖拽文件到此处上传</p>
      <p class="ant-upload-hint">
        支持多文件上传，单个文件最大 {{ maxSize }}MB，最多 {{ maxCount }} 个文件
      </p>
    </a-upload-dragger>

    <div v-if="uploadingList.length > 0" class="uploading-list">
      <a-list size="small">
        <a-list-item v-for="file in uploadingList" :key="file.uid">
          <a-list-item-meta>
            <template #title>
              <span>{{ file.name }}</span>
            </template>
            <template #description>
              <a-progress :percent="file.percent" :show-info="false" size="small" />
              <span class="progress-text">{{ file.percent }}%</span>
            </template>
          </a-list-item-meta>
          <template #actions>
            <a-button type="text" danger size="small" @click="cancelUpload(file.uid)">
              <template #icon><close-outlined /></template>
              取消
            </a-button>
          </template>
        </a-list-item>
      </a-list>
    </div>

    <a-table
      :data-source="attachmentList"
      :pagination="false"
      :row-key="(record: DocAttachment) => record.id"
      class="attachment-table"
      :class="{ 'dragging': isDragging }"
      @row="onRow"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'sort'">
          <div class="sort-handle" v-if="!disabled">
            <holder-outlined />
          </div>
        </template>
        <template v-else-if="column.key === 'icon'">
          <component :is="getFileIcon(record.fileExt)" class="file-icon" />
        </template>
        <template v-else-if="column.key === 'name'">
          <div class="name-cell">
            <a-input
              v-if="editingId === record.id"
              v-model:value="editingName"
              size="small"
              @blur="saveRename(record)"
              @keyup.enter="saveRename(record)"
              @keyup.esc="cancelRename"
              ref="inputRef"
            />
            <span v-else class="name-text" :title="record.attachmentName">
              {{ record.attachmentName }}
            </span>
            <a-button
              v-if="!disabled"
              type="text"
              size="small"
              class="rename-btn"
              @click.stop="startRename(record)"
            >
              <template #icon><edit-outlined /></template>
            </a-button>
          </div>
        </template>
        <template v-else-if="column.key === 'size'">
          {{ formatFileSize(record.fileSize) }}
        </template>
        <template v-else-if="column.key === 'time'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button
              type="text"
              size="small"
              @click.stop="handlePreview(record)"
              :disabled="!canPreview(record.fileExt)"
            >
              <template #icon><eye-outlined /></template>
              预览
            </a-button>
            <a-button type="text" size="small" @click.stop="handleDownload(record)">
              <template #icon><download-outlined /></template>
              下载
            </a-button>
            <a-popconfirm
              title="确定要删除该附件吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record)"
            >
              <a-button
                type="text"
              danger
                size="small"
                :disabled="disabled"
              >
                <template #icon><delete-outlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>

      <a-table-column key="sort" width="40" />
      <a-table-column key="icon" width="50" />
      <a-table-column key="name" title="附件名称" />
      <a-table-column key="size" title="大小" width="100" />
      <a-table-column key="time" title="上传时间" width="180" />
      <a-table-column key="action" title="操作" width="200" />
    </a-table>

    <a-empty
      v-if="attachmentList.length === 0 && uploadingList.length === 0"
      description="暂无附件"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    />

    <a-modal
      v-model:open="previewVisible"
      :title="previewTitle"
      width="80%"
      :footer="null"
      :mask-closable="true"
    >
      <div class="preview-content">
        <img
          v-if="previewType === 'image'"
          :src="previewUrl"
          alt="preview"
        />
        <iframe
          v-else-if="previewType === 'pdf'"
          :src="previewUrl"
          class="pdf-preview"
        />
        <div v-else class="no-preview">
          <file-unknown-outlined class="no-preview-icon" />
          <p>该文件类型暂不支持在线预览</p>
          <a-button type="primary" @click="handleDownload(previewAttachment!)">
            <template #icon><download-outlined /></template>
            下载文件
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { message, Empty, Upload } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import request from '@/utils/request'
import {
  InboxOutlined,
  CloseOutlined,
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EditOutlined,
  HolderOutlined,
  FileImageOutlined,
  FileTextOutlined,
  FileExcelOutlined,
  FilePptOutlined,
  FileZipOutlined,
  FileUnknownOutlined,
  FileOutlined
} from '@ant-design/icons-vue'
import type { DocAttachment } from '@/types/template'
import {
  getAttachmentList,
  uploadAttachment,
  deleteAttachment,
  updateAttachment,
  downloadAttachment,
  getAttachmentPreviewUrl
} from '@/api/template'

interface Props {
  documentId: number
  disabled?: boolean
  maxCount?: number
  maxSize?: number
  accept?: string
}

interface Emits {
  (e: 'change', attachments: DocAttachment[]): void
  (e: 'uploadSuccess', attachment: DocAttachment): void
  (e: 'uploadError', file: File, error: Error): void
  (e: 'delete', attachment: DocAttachment): void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  maxCount: 10,
  maxSize: 50,
  accept: ''
})

const emit = defineEmits<Emits>()

interface UploadingFile {
  uid: string
  name: string
  file: File
  percent: number
  abortController?: AbortController
}

const attachmentList = ref<DocAttachment[]>([])
const uploadingList = reactive<UploadingFile[]>([])
const loading = ref(false)
const editingId = ref<number | null>(null)
const editingName = ref('')
const inputRef = ref()
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewType = ref<'image' | 'pdf' | 'other'>('other')
const previewAttachment = ref<DocAttachment | null>(null)
const isDragging = ref(false)
const dragIndex = ref(-1)

const loadAttachmentList = async () => {
  if (!props.documentId) return
  loading.value = true
  try {
    const res = await getAttachmentList(props.documentId)
    attachmentList.value = (res as any).data || []
    emit('change', attachmentList.value)
  } catch (error) {
    console.error('加载附件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDateTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const getFileIcon = (ext: string) => {
  const imageExts = ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp']
  const docExts = ['doc', 'docx', 'pdf', 'txt', 'md']
  const excelExts = ['xls', 'xlsx', 'csv']
  const pptExts = ['ppt', 'pptx']
  const zipExts = ['zip', 'rar', '7z', 'tar', 'gz']

  const lowerExt = (ext || '').toLowerCase()

  if (imageExts.includes(lowerExt)) return FileImageOutlined
  if (docExts.includes(lowerExt)) return FileTextOutlined
  if (excelExts.includes(lowerExt)) return FileExcelOutlined
  if (pptExts.includes(lowerExt)) return FilePptOutlined
  if (zipExts.includes(lowerExt)) return FileZipOutlined
  return FileOutlined
}

const canPreview = (ext: string): boolean => {
  const previewExts = ['png', 'jpg', 'jpeg', 'gif', 'webp', 'pdf']
  return previewExts.includes((ext || '').toLowerCase())
}

const getPreviewType = (ext: string): 'image' | 'pdf' | 'other' => {
  const imageExts = ['png', 'jpg', 'jpeg', 'gif', 'webp']
  const lowerExt = (ext || '').toLowerCase()
  if (imageExts.includes(lowerExt)) return 'image'
  if (lowerExt === 'pdf') return 'pdf'
  return 'other'
}

const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
  const fileSize = (file as File).size / 1024 / 1024
  if (fileSize > props.maxSize) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return Upload.LIST_IGNORE
  }
  if (attachmentList.value.length + uploadingList.length >= props.maxCount) {
    message.error(`最多只能上传 ${props.maxCount} 个文件`)
    return Upload.LIST_IGNORE
  }
  return true
}

const generateUid = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

const handleCustomRequest: UploadProps['customRequest'] = async ({ file, onProgress, onError }) => {
  const fileObj = file as File
  const uid = generateUid()
  const controller = new AbortController()

  const uploadingFile: UploadingFile = {
    uid,
    name: fileObj.name,
    file: fileObj,
    percent: 0,
    abortController: controller
  }

  uploadingList.push(uploadingFile)

  try {
    const formData = new FormData()
    formData.append('file', fileObj)

    const onUploadProgress = (progressEvent: any) => {
      const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      const idx = uploadingList.findIndex(item => item.uid === uid)
      if (idx !== -1) {
        uploadingList[idx].percent = percent
      }
      onProgress?.({ percent })
    }

    const res = await request<DocAttachment>({
      url: `/doc/document/attachment/upload/${props.documentId}`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress,
      signal: controller.signal
    })
    const attachment = (res as any).data

    const idx = uploadingList.findIndex(item => item.uid === uid)
    if (idx !== -1) {
      uploadingList.splice(idx, 1)
    }

    attachmentList.value.push(attachment)
    emit('uploadSuccess', attachment)
    emit('change', [...attachmentList.value])
    message.success('上传成功')
  } catch (error: any) {
    const idx = uploadingList.findIndex(item => item.uid === uid)
    if (idx !== -1) {
      uploadingList.splice(idx, 1)
    }
    onError?.(error)
    emit('uploadError', fileObj, error)
    message.error('上传失败: ' + (error.message || '未知错误'))
  }
}

const handleDrop = () => {
}

const cancelUpload = (uid: string) => {
  const idx = uploadingList.findIndex(item => item.uid === uid)
  if (idx !== -1) {
    uploadingList[idx].abortController?.abort()
    uploadingList.splice(idx, 1)
  }
}

const handleDelete = async (attachment: DocAttachment) => {
  try {
    await deleteAttachment(attachment.id)
    const idx = attachmentList.value.findIndex(item => item.id === attachment.id)
    if (idx !== -1) {
      attachmentList.value.splice(idx, 1)
    }
    emit('delete', attachment)
    emit('change', [...attachmentList.value])
    message.success('删除成功')
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleDownload = async (attachment: DocAttachment) => {
  try {
    const res = await downloadAttachment(attachment.id)
    const blob = (res as any).data as Blob
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = attachment.attachmentName + '.' + attachment.fileExt
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败:', error)
    message.error('下载失败')
  }
}

const handlePreview = (attachment: DocAttachment) => {
  previewAttachment.value = attachment
  previewTitle.value = attachment.attachmentName
  previewType.value = getPreviewType(attachment.fileExt)
  previewUrl.value = getAttachmentPreviewUrl(attachment.id)
  previewVisible.value = true
}

const startRename = (attachment: DocAttachment) => {
  editingId.value = attachment.id
  editingName.value = attachment.attachmentName
  nextTick(() => {
    inputRef.value?.focus()
    inputRef.value?.select()
  })
}

const saveRename = async (attachment: DocAttachment) => {
  if (editingName.value.trim() === '') {
    message.error('附件名称不能为空')
    return
  }
  if (editingName.value === attachment.attachmentName) {
    cancelRename()
    return
  }
  try {
    await updateAttachment({
      id: attachment.id,
      documentId: attachment.documentId,
      attachmentName: editingName.value.trim()
    })
    attachment.attachmentName = editingName.value.trim()
    emit('change', [...attachmentList.value])
    message.success('重命名成功')
  } catch (error) {
    console.error('重命名失败:', error)
  } finally {
    cancelRename()
  }
}

const cancelRename = () => {
  editingId.value = null
  editingName.value = ''
}

const onRow = (record: DocAttachment, index: number) => {
  return {
    draggable: !props.disabled,
    onDragstart: (e: DragEvent) => {
      isDragging.value = true
      dragIndex.value = index
      e.dataTransfer!.effectAllowed = 'move'
    },
    onDragend: () => {
      isDragging.value = false
      dragIndex.value = -1
    },
    onDragover: (e: DragEvent) => {
      e.preventDefault()
    },
    onDrop: async (e: DragEvent) => {
      e.preventDefault()
      if (dragIndex.value === -1 || dragIndex.value === index) return

      const newList = [...attachmentList.value]
      const [removed] = newList.splice(dragIndex.value, 1)
      newList.splice(index, 0, removed)

      for (let i = 0; i < newList.length; i++) {
        newList[i].sortOrder = i
      }

      attachmentList.value = newList
      emit('change', [...newList])

      try {
        await updateAttachment({
          id: removed.id,
          documentId: removed.documentId,
          attachmentName: removed.attachmentName,
          sortOrder: index
        })
        message.success('排序成功')
      } catch (error) {
        console.error('排序失败:', error)
      }
    }
  }
}

watch(() => props.documentId, () => {
  if (props.documentId) {
    loadAttachmentList()
  }
})

onMounted(() => {
  if (props.documentId) {
    loadAttachmentList()
  }
})
</script>

<style scoped lang="less">
.attachment-manager {
  .uploading-list {
    margin-top: 16px;

    .progress-text {
      font-size: 12px;
      color: #999;
      margin-left: 8px;
    }
  }

  .attachment-table {
    margin-top: 16px;

    &.dragging {
      cursor: move;
    }

    :deep(.ant-table-row) {
      cursor: default;
    }

    .sort-handle {
      cursor: move;
      color: #999;
      text-align: center;
      width: 100%;

      &:hover {
        color: #1890ff;
      }
    }

    .file-icon {
      font-size: 24px;
      color: #1890ff;
    }

    .name-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .name-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .rename-btn {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }

    :deep(.ant-table-row:hover) {
      .rename-btn {
        opacity: 1;
      }
    }
  }

  .preview-content {
    text-align: center;

    img {
      max-width: 100%;
      max-height: 70vh;
    }

    .pdf-preview {
      width: 100%;
      height: 70vh;
      border: none;
    }

    .no-preview {
      padding: 40px 0;

      .no-preview-icon {
        font-size: 64px;
        color: #999;
        margin-bottom: 16px;
      }

      p {
        color: #999;
        margin-bottom: 24px;
      }
    }
  }
}
</style>
