import request from '@/utils/request'
import type {
  DocTemplateVO,
  DocTemplateSaveDTO,
  DocTemplateQueryDTO,
  PageResult,
  DocDocumentCreateDTO,
  DocDocument
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
