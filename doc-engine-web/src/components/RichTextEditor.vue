<template>
  <div class="rich-text-editor">
    <div
      class="editor-container"
      :style="{ height: containerHeight }"
    >
      <Toolbar
        v-if="!readOnly"
        class="editor-toolbar"
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        mode="default"
      />
      <Editor
        class="editor-content"
        v-model="valueHtml"
        :defaultConfig="editorConfig"
        :mode="mode"
        @onCreated="handleCreated"
        @onChange="handleChange"
        @onDestroyed="handleDestroyed"
        @onMaxLength="handleMaxLength"
      />
    </div>
    <div v-if="maxLength" class="editor-footer">
      <span :class="{ 'over-limit': isOverLimit }">
        {{ currentLength }} / {{ maxLength }}
      </span>
    </div>

    <a-modal
      v-model:open="previewVisible"
      title="内容预览"
      :width="900"
      :footer="null"
    >
      <div class="preview-content" v-html="valueHtml"></div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onBeforeUnmount, shallowRef } from 'vue'
import { message } from 'ant-design-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import request from '@/utils/request'

interface Props {
  modelValue?: string
  placeholder?: string
  height?: number | string
  readOnly?: boolean
  maxLength?: number
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'change', html: string, text: string): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容...',
  height: 500,
  readOnly: false,
  maxLength: undefined
})

const emit = defineEmits<Emits>()

const editorRef = shallowRef<IDomEditor | null>(null)
const valueHtml = ref(props.modelValue)
const previewVisible = ref(false)
const currentLength = ref(0)

const mode = computed(() => props.readOnly ? 'simple' : 'default')

const containerHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`
  }
  return props.height
})

const isOverLimit = computed(() => {
  if (!props.maxLength) return false
  return currentLength.value > props.maxLength
})

const toolbarConfig: Partial<IToolbarConfig> = {
  toolbarKeys: [
    'undo',
    'redo',
    '|',
    'headerSelect',
    '|',
    'bold',
    'italic',
    'underline',
    'through',
    'color',
    'bgColor',
    '|',
    'fontFamily',
    'fontSize',
    '|',
    'justifyLeft',
    'justifyCenter',
    'justifyRight',
    'justifyJustify',
    '|',
    'lineHeight',
    'indent',
    'delIndent',
    '|',
    'bulletedList',
    'numberedList',
    '|',
    'insertTable',
    'deleteTable',
    'insertTableRow',
    'deleteTableRow',
    'insertTableCol',
    'deleteTableCol',
    'tableHeader',
    'tableFullWidth',
    'mergeTableCell',
    'splitTableCell',
    '|',
    'uploadImage',
    'insertLink',
    'divider',
    '|',
    'clearStyle',
    'fullScreen',
    'previewCustom'
  ],
  excludeKeys: [],
  insertKeys: {
    index: 22,
    keys: ['uploadAttachment']
  }
}

const editorConfig: Partial<IEditorConfig> = reactive({
  placeholder: props.placeholder,
  readOnly: props.readOnly,
  MENU_CONF: {
    uploadImage: {
      customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        handleImageUpload(file, insertFn)
      },
      allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp'],
      maxFileSize: 10 * 1024 * 1024,
      base64LimitSize: 10 * 1024 * 1024
    },
    insertLink: {
      parseLinkText(text: string) {
        return text
      }
    }
  },
  EXTEND_CONF: {
    attachmentMenu: {
      customUpload(file: File, insertFn: (url: string, fileName: string) => void) {
        handleAttachmentUpload(file, insertFn)
      }
    }
  },
  hoverbarKeys: {
    link: {
      menuKeys: ['editLink', 'unLink', 'viewLink']
    },
    image: {
      menuKeys: ['imageWidth30', 'imageWidth50', 'imageWidth100', 'imageFloatNone', 'imageFloatLeft', 'imageFloatRight', 'editImage', 'viewImageLink', 'deleteImage']
    },
    table: {
      menuKeys: ['tableHeader', 'tableFullWidth', 'insertTableRow', 'deleteTableRow', 'insertTableCol', 'deleteTableCol', 'deleteTable', 'mergeTableCell', 'splitTableCell']
    }
  },
  onChange(editor: IDomEditor) {
    currentLength.value = editor.getText().replace(/\s/g, '').length
  }
})

watch(() => props.placeholder, (val) => {
  editorConfig.placeholder = val
})

watch(() => props.readOnly, (val) => {
  editorConfig.readOnly = val
  if (editorRef.value) {
    if (val) {
      editorRef.value.disable()
    } else {
      editorRef.value.enable()
    }
  }
})

watch(() => props.modelValue, (val) => {
  if (val !== valueHtml.value) {
    valueHtml.value = val || ''
  }
})

watch(valueHtml, (val) => {
  emit('update:modelValue', val)
})

const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
  registerCustomMenu(editor)
  if (props.readOnly) {
    editor.disable()
  }
  currentLength.value = editor.getText().replace(/\s/g, '').length
}

const handleChange = (editor: IDomEditor) => {
  const html = editor.getHtml()
  const text = editor.getText()
  emit('change', html, text)
}

const handleDestroyed = () => {
  editorRef.value = null
}

const handleMaxLength = (editor: IDomEditor) => {
  message.warning(`内容超出最大字符数限制 ${props.maxLength} 字`)
}

const handleImageUpload = async (
  file: File,
  insertFn: (url: string, alt?: string, href?: string) => void
) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await request<any>({
      url: '/doc/document/image/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const isWangEditorFormat = typeof res.errno !== 'undefined'
    const success = isWangEditorFormat ? (res.errno === 0) : (res.code === 200)
    const responseData = isWangEditorFormat ? res : (res.data || res)

    if (success && responseData && responseData.data) {
      const imageUrl = responseData.data.url || responseData.data
      insertFn(imageUrl, file.name, imageUrl)
      message.success('图片上传成功')
    } else {
      message.error(res.message || res.msg || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64 = e.target?.result as string
      insertFn(base64, file.name, base64)
      message.success('图片已插入（本地预览）')
    }
    reader.readAsDataURL(file)
  }
}

const handleAttachmentUpload = async (
  file: File,
  insertFn: (url: string, fileName: string) => void
) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await request<any>({
      url: '/doc/document/attachment/upload-editor',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const isWangEditorFormat = typeof res.errno !== 'undefined'
    const success = isWangEditorFormat ? (res.errno === 0) : (res.code === 200)
    const responseData = isWangEditorFormat ? res : (res.data || res)

    if (success && responseData && responseData.data) {
      const attachmentUrl = responseData.data.url || responseData.data
      insertFn(attachmentUrl, file.name)
      message.success('附件上传成功')
    } else {
      message.error(res.message || res.msg || '附件上传失败')
    }
  } catch (error) {
    console.error('附件上传失败:', error)
    const fileSize = formatFileSize(file.size)
    const mockUrl = `#attachment-${Date.now()}`
    const attachmentHtml = `<p><a href="${mockUrl}" data-attachment="true" data-file-name="${file.name}" data-file-size="${fileSize}">📎 ${file.name} (${fileSize})</a></p>`
    
    if (editorRef.value) {
      editorRef.value.dangerouslyInsertHtml(attachmentHtml)
    }
    message.success('附件已插入（本地预览）')
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const registerCustomMenu = (editor: IDomEditor) => {
  const { Boot } = require('@wangeditor/editor')
  
  const uploadAttachmentMenu = {
    key: 'uploadAttachment',
    title: '上传附件',
    iconSvg: '<svg viewBox="0 0 1024 1024"><path d="M768 320V192c0-106.039-85.961-192-192-192S384 85.961 384 192v512c0 70.692 57.308 128 128 128s128-57.308 128-128V384h-64v320c0 35.346-28.654 64-64 64s-64-28.654-64-64V192c0-70.692 57.308-128 128-128s128 57.308 128 128v128h-64z"/><path d="M320 832c0 35.346 28.654 64 64 64s64-28.654 64-64V640h-64v192z"/></svg>',
    showToast: false,
    tag: 'button',
    active: false,
    getValue() {
      return ''
    },
    isActive() {
      return false
    },
    isDisabled() {
      return editor.isDisabled()
    },
    exec() {
      const input = document.createElement('input')
      input.type = 'file'
      input.multiple = true
      input.accept = '*/*'
      input.onchange = async (e: Event) => {
        const target = e.target as HTMLInputElement
        const files = target.files
        if (files && files.length > 0) {
          for (let i = 0; i < files.length; i++) {
            const file = files[i]
            const uploadConfig = editorConfig.EXTEND_CONF?.attachmentMenu
            if (uploadConfig?.customUpload) {
              uploadConfig.customUpload(file, (url: string, fileName: string) => {
                const fileSize = formatFileSize(file.size)
                const attachmentHtml = `<p><a href="${url}" data-attachment="true" data-file-name="${fileName}" data-file-size="${fileSize}">📎 ${fileName} (${fileSize})</a></p>`
                editor.dangerouslyInsertHtml(attachmentHtml)
              })
            }
          }
        }
      }
      input.click()
    }
  }

  const previewMenu = {
    key: 'previewCustom',
    title: '预览',
    iconSvg: '<svg viewBox="0 0 1024 1024"><path d="M942.2 486.2C847.4 286.5 654.2 192 512 192S176.6 286.5 81.8 486.2a8.15 8.15 0 0 0 0 51.6C176.6 737.5 369.8 832 512 832s335.4-94.5 430.2-294.2a8.15 8.15 0 0 0 0-51.6zM512 704c-123.8 0-232.4-65.1-293.6-192C279.6 385.1 388.2 320 512 320s232.4 65.1 293.6 192c-61.2 126.9-169.8 192-293.6 192z"/><path d="M512 416c-53 0-96 43-96 96s43 96 96 96 96-43 96-96-43-96-96-96zm0 128c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z"/></svg>',
    showToast: false,
    tag: 'button',
    active: false,
    getValue() {
      return ''
    },
    isActive() {
      return false
    },
    isDisabled() {
      return false
    },
    exec() {
      previewVisible.value = true
    }
  }

  try {
    Boot.registerMenu(uploadAttachmentMenu)
    Boot.registerMenu(previewMenu)
  } catch (e) {
    console.warn('自定义菜单已注册或注册失败:', e)
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style lang="less" scoped>
.rich-text-editor {
  display: flex;
  flex-direction: column;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;

  .editor-container {
    display: flex;
    flex-direction: column;
    min-height: 300px;
  }

  .editor-toolbar {
    border-bottom: 1px solid #e8e8e8;
    background-color: #fafafa;
    z-index: 100;
  }

  .editor-content {
    flex: 1;
    overflow-y: auto;
    background-color: #fff;
  }

  .editor-footer {
    padding: 4px 12px;
    text-align: right;
    font-size: 12px;
    color: #999;
    border-top: 1px solid #e8e8e8;
    background-color: #fafafa;

    .over-limit {
      color: #ff4d4f;
    }
  }

  .preview-content {
    max-height: 70vh;
    overflow-y: auto;
    padding: 24px;
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }
}
</style>

<style lang="less">
.w-e-text-container {
  font-family: '仿宋', 'FangSong', 'STFangsong', serif !important;
  font-size: 16px !important;
  line-height: 1.8 !important;
  color: #333 !important;
}

.w-e-text-container p {
  margin: 0 0 16px 0 !important;
  text-indent: 2em !important;
}

.w-e-text-container h1 {
  font-family: '黑体', 'SimHei', sans-serif !important;
  font-size: 22px !important;
  font-weight: bold !important;
  text-align: center !important;
  text-indent: 0 !important;
  margin: 24px 0 16px 0 !important;
}

.w-e-text-container h2 {
  font-family: '黑体', 'SimHei', sans-serif !important;
  font-size: 18px !important;
  font-weight: bold !important;
  text-indent: 0 !important;
  margin: 20px 0 12px 0 !important;
}

.w-e-text-container h3 {
  font-family: '楷体', 'KaiTi', 'STKaiti', serif !important;
  font-size: 16px !important;
  font-weight: bold !important;
  text-indent: 2em !important;
  margin: 16px 0 8px 0 !important;
}

.w-e-text-container table {
  border-collapse: collapse !important;
  width: 100% !important;
  margin: 16px 0 !important;
  text-indent: 0 !important;
}

.w-e-text-container table td,
.w-e-text-container table th {
  border: 1px solid #333 !important;
  padding: 8px 12px !important;
  text-align: center !important;
  vertical-align: middle !important;
}

.w-e-text-container table th {
  background-color: #f5f5f5 !important;
  font-weight: bold !important;
}

.w-e-text-container a[data-attachment="true"] {
  color: #1890ff !important;
  text-decoration: none !important;
  padding: 2px 8px !important;
  background-color: #e6f7ff !important;
  border-radius: 4px !important;
  display: inline-block !important;
  margin: 4px 0 !important;
  text-indent: 0 !important;
}

.w-e-text-container a[data-attachment="true"]:hover {
  color: #40a9ff !important;
  background-color: #bae7ff !important;
}

.w-e-text-container img {
  max-width: 100% !important;
  height: auto !important;
  margin: 16px auto !important;
  display: block !important;
  text-indent: 0 !important;
}

.w-e-text-container ul,
.w-e-text-container ol {
  margin: 16px 0 !important;
  padding-left: 2em !important;
  text-indent: 0 !important;
}

.w-e-text-container ul li,
.w-e-text-container ol li {
  text-indent: 0 !important;
  margin: 8px 0 !important;
}

.w-e-text-container blockquote {
  border-left: 4px solid #d9d9d9 !important;
  padding-left: 16px !important;
  margin: 16px 0 !important;
  color: #666 !important;
  text-indent: 0 !important;
}

.w-e-text-container hr {
  border: none !important;
  border-top: 1px solid #d9d9d9 !important;
  margin: 24px 0 !important;
  text-indent: 0 !important;
}

.w-e-full-screen-container {
  z-index: 9999 !important;
}

.w-e-toolbar {
  flex-wrap: wrap !important;
}

.w-e-bar-item {
  margin: 2px !important;
}

.w-e-menu {
  border-radius: 4px !important;
}

.w-e-menu:hover {
  background-color: #e6f7ff !important;
}

.w-e-menu-active {
  background-color: #bae7ff !important;
  color: #1890ff !important;
}

.w-e-modal {
  z-index: 10000 !important;
}

.w-e-text-container pre {
  background-color: #f5f5f5 !important;
  padding: 16px !important;
  border-radius: 4px !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 14px !important;
  overflow-x: auto !important;
  text-indent: 0 !important;
}

.w-e-text-container code {
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 14px !important;
  background-color: #f5f5f5 !important;
  padding: 2px 6px !important;
  border-radius: 4px !important;
  text-indent: 0 !important;
}
</style>
