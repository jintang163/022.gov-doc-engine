import request from '@/utils/request'
import type {
  DocTemplateVO,
  DocTemplateSaveDTO,
  DocTemplateQueryDTO,
  PageResult,
  DocDocumentCreateDTO,
  DocDocument,
  DocTemplateHeaderDTO,
  DocAttachment,
  DocAttachmentDTO,
  DocDraft,
  DocDraftSaveDTO,
  DocPlaceholderReplaceDTO,
  DocPlaceholderResultVO,
  DocDocumentValidationDTO,
  DocValidationResultVO,
  DocDocumentUpdateDTO
} from '@/types/template'

export const getTemplatePage = (params: DocTemplateQueryDTO) => {
  return request<PageResult<DocTemplateVO>>({
    url: '/doc/template/page',
    method: 'get',
    params
  })
}

export const getTemplateDetail = (id: number) => {
  return request<DocTemplateVO>({
    url: `/doc/template/${id}`,
    method: 'get'
  })
}

export const saveTemplate = (data: DocTemplateSaveDTO) => {
  return request<number>({
    url: '/doc/template',
    method: 'post',
    data
  })
}

export const updateTemplate = (data: DocTemplateSaveDTO) => {
  return request<void>({
    url: '/doc/template',
    method: 'put',
    data
  })
}

export const deleteTemplate = (id: number) => {
  return request<void>({
    url: `/doc/template/${id}`,
    method: 'delete'
  })
}

export const createNewVersion = (id: number) => {
  return request<number>({
    url: `/doc/template/version/${id}`,
    method: 'post'
  })
}

export const publishTemplate = (id: number) => {
  return request<void>({
    url: `/doc/template/publish/${id}`,
    method: 'post'
  })
}

export const disableTemplate = (id: number) => {
  return request<void>({
    url: `/doc/template/disable/${id}`,
    method: 'post'
  })
}

export const listAvailableTemplates = () => {
  return request<DocTemplateVO[]>({
    url: '/doc/template/available',
    method: 'get'
  })
}

export const createDocumentFromTemplate = (data: DocDocumentCreateDTO) => {
  return request<DocDocument>({
    url: '/doc/template/document/create',
    method: 'post',
    data
  })
}

export const getTemplatePreviewHtml = (templateId: number) => {
  return request<string>({
    url: `/doc/preview/template/html/${templateId}`,
    method: 'get'
  })
}

export const getDocumentPreviewHtml = (templateId: number, data: DocDocumentCreateDTO) => {
  return request<string>({
    url: `/doc/preview/document/html/${templateId}`,
    method: 'post',
    data
  })
}

export const getTemplatePreviewUrl = (templateId: number) => {
  return `/api/doc/preview/template/html-view/${templateId}`
}

export const getDocumentPreviewUrl = (templateId: number) => {
  return `/api/doc/preview/document/html-view/${templateId}`
}

export const uploadWordTemplate = (templateId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request<string>({
    url: `/doc/template/upload-word/${templateId}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const extractVariables = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request<string[]>({
    url: '/doc/template/extract-variables',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const generateWordDocument = (templateId: number, data: DocDocumentCreateDTO) => {
  return request<Blob>({
    url: `/doc/template/generate-word/${templateId}`,
    method: 'post',
    data,
    responseType: 'blob'
  })
}

export const getTemplateHeaderList = () => {
  return request<DocTemplateHeaderDTO[]>({
    url: '/doc/template-header/list',
    method: 'get'
  })
}

export const getTemplateHeaderDetail = (id: number) => {
  return request<DocTemplateHeaderDTO>({
    url: `/doc/template-header/${id}`,
    method: 'get'
  })
}

export const saveTemplateHeader = (data: DocTemplateHeaderDTO) => {
  return request<number>({
    url: '/doc/template-header',
    method: 'post',
    data
  })
}

export const updateTemplateHeader = (data: DocTemplateHeaderDTO) => {
  return request<void>({
    url: '/doc/template-header',
    method: 'put',
    data
  })
}

export const deleteTemplateHeader = (id: number) => {
  return request<void>({
    url: `/doc/template-header/${id}`,
    method: 'delete'
  })
}

export const getDocumentPage = (params: { pageNum: number; pageSize: number; keyword?: string }) => {
  return request<PageResult<DocDocument>>({
    url: '/doc/document/page',
    method: 'get',
    params
  })
}

export const getDocumentDetail = (id: number) => {
  return request<DocDocument>({
    url: `/doc/document/${id}`,
    method: 'get'
  })
}

export const updateDocument = (data: DocDocumentUpdateDTO) => {
  return request<void>({
    url: '/doc/document',
    method: 'put',
    data
  })
}

export const getAttachmentList = (documentId: number) => {
  return request<DocAttachment[]>({
    url: `/doc/document/attachment/list/${documentId}`,
    method: 'get'
  })
}

export const getAttachmentDetail = (id: number) => {
  return request<DocAttachment>({
    url: `/doc/document/attachment/${id}`,
    method: 'get'
  })
}

export const saveAttachment = (data: DocAttachmentDTO) => {
  return request<number>({
    url: '/doc/document/attachment',
    method: 'post',
    data
  })
}

export const updateAttachment = (data: DocAttachmentDTO) => {
  return request<void>({
    url: '/doc/document/attachment',
    method: 'put',
    data
  })
}

export const deleteAttachment = (id: number) => {
  return request<void>({
    url: `/doc/document/attachment/${id}`,
    method: 'delete'
  })
}

export const uploadAttachment = (documentId: number, file: File, attachmentName?: string) => {
  const formData = new FormData()
  formData.append('file', file)
  if (attachmentName) {
    formData.append('attachmentName', attachmentName)
  }
  return request<DocAttachment>({
    url: `/doc/document/attachment/upload/${documentId}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadAttachment = (id: number) => {
  return request<Blob>({
    url: `/doc/document/attachment/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

export const getAttachmentPreviewUrl = (id: number) => {
  return `/api/doc/document/attachment/preview/${id}`
}

export const getDraftPage = (params: { pageNum: number; pageSize: number }) => {
  return request<PageResult<DocDraft>>({
    url: '/doc/document/draft/page',
    method: 'get',
    params
  })
}

export const getDraftListByTemplate = (templateId: number) => {
  return request<DocDraft[]>({
    url: `/doc/document/draft/list/${templateId}`,
    method: 'get'
  })
}

export const getDraftDetail = (id: number) => {
  return request<DocDraft>({
    url: `/doc/document/draft/${id}`,
    method: 'get'
  })
}

export const getLatestDraft = (templateId: number) => {
  return request<DocDraft>({
    url: `/doc/document/draft/latest/${templateId}`,
    method: 'get'
  })
}

export const saveDraft = (data: DocDraftSaveDTO) => {
  return request<number>({
    url: '/doc/document/draft',
    method: 'post',
    data
  })
}

export const autoSaveDraft = (data: DocDraftSaveDTO) => {
  return request<number>({
    url: '/doc/document/draft/auto',
    method: 'post',
    data
  })
}

export const deleteDraft = (id: number) => {
  return request<void>({
    url: `/doc/document/draft/${id}`,
    method: 'delete'
  })
}

export const replacePlaceholders = (data: DocPlaceholderReplaceDTO) => {
  return request<DocPlaceholderResultVO>({
    url: '/doc/document/placeholder/replace',
    method: 'post',
    data
  })
}

export const getPresetPlaceholders = () => {
  return request<Record<string, string>>({
    url: '/doc/document/placeholder/preset',
    method: 'get'
  })
}

export const validateDocument = (data: DocDocumentValidationDTO) => {
  return request<DocValidationResultVO>({
    url: '/doc/document/validate',
    method: 'post',
    data
  })
}
