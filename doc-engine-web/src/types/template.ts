export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
}

export interface DocTemplateVO {
  id: number
  templateCode: string
  templateName: string
  templateType: string
  templateCategory: string
  version: number
  isCurrentVersion: number
  parentTemplateId: number
  status: number
  statusName: string
  description: string
  unitCode: string
  unitName: string
  securityLevel: string
  urgencyLevel: string
  permissionRoles: string
  permissionUsers: string
  permissionDepts: string
  templateFilePath: string
  templateFileName: string
  templateVariables: string
  headerId: number
  remark: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  header?: DocTemplateHeaderDTO
  fields: DocTemplateFieldDTO[]
}

export interface DocTemplateHeaderDTO {
  id?: number
  headerType?: string
  headerName: string
  unitName: string
  unitNameFont?: string
  unitNameFontSize: number
  unitNameFontColor?: string
  unitNameFontBold?: number
  unitNameTextAlign?: string
  unitNameMarginTop?: number
  unitNameMarginBottom?: number
  showDocumentNumber?: number
  documentNumberPrefix?: string
  documentNumberYear?: string
  documentNumberFont?: string
  documentNumberFontSize?: number
  documentNumberFontColor?: string
  documentNumberTextAlign?: string
  documentNumberMarginTop?: number
  documentNumberMarginBottom?: number
  showRedLine?: number
  redLineWidth?: number
  redLineHeight?: number
  redLineColor?: string
  redLineMarginTop?: number
  redLineMarginBottom?: number
  showStar?: number
  starSize?: number
  starColor?: string
  pageWidth?: number
  pageHeight?: number
  pageMarginTop?: number
  pageMarginBottom?: number
  pageMarginLeft?: number
  pageMarginRight?: number
  customCss?: string
  remark?: string
}

export interface DocTemplateFieldDTO {
  id?: number
  templateId?: number
  fieldKey: string
  fieldName: string
  fieldType: string
  fieldGroup?: string
  isRequired?: number
  isPreset?: number
  defaultValue?: string
  placeholder?: string
  fieldOptions?: string
  sortOrder?: number
  validationRule?: string
  fontFamily?: string
  fontSize?: number
  fontColor?: string
  fontBold?: number
  textAlign?: string
  marginTop?: number
  marginBottom?: number
  marginLeft?: number
  marginRight?: number
  lineHeight?: number
  textIndent?: number
  remark?: string
}

export interface DocTemplateSaveDTO {
  id?: number
  templateCode: string
  templateName: string
  templateType: string
  templateCategory?: string
  version?: number
  isCurrentVersion?: number
  parentTemplateId?: number
  status?: number
  description?: string
  unitCode?: string
  unitName?: string
  securityLevel?: string
  urgencyLevel?: string
  permissionRoles?: string
  permissionUsers?: string
  permissionDepts?: string
  templateFilePath?: string
  templateFileName?: string
  templateVariables?: string
  headerId?: number
  remark?: string
  fields?: DocTemplateFieldDTO[]
}

export interface DocTemplateQueryDTO {
  templateCode?: string
  templateName?: string
  templateType?: string
  templateCategory?: string
  status?: number
  isCurrentVersion?: number
  unitCode?: string
  pageNum?: number
  pageSize?: number
}

export interface DocDocumentCreateDTO {
  templateId: number
  docTitle: string
  docNumber?: string
  docType?: string
  securityLevel?: string
  urgencyLevel?: string
  mainSendDept?: string
  copySendDept?: string
  signer?: string
  signDate?: string
  writtenDate?: string
  docContent?: string
  attachmentInfo?: string
  fieldData?: Record<string, any>
  remark?: string
}

export interface DocDocument {
  id: number
  templateId: number
  docTitle: string
  docNumber: string
  docType: string
  status: string
  createTime: string
  [key: string]: any
}

export const templateTypeOptions = [
  { label: '上行文', value: '上行文' },
  { label: '下行文', value: '下行文' },
  { label: '平行文', value: '平行文' },
  { label: '内部文', value: '内部文' }
]

export const templateCategoryOptions = [
  { label: '请示', value: '请示' },
  { label: '报告', value: '报告' },
  { label: '通知', value: '通知' },
  { label: '批复', value: '批复' },
  { label: '函', value: '函' },
  { label: '决定', value: '决定' },
  { label: '命令', value: '命令' },
  { label: '公告', value: '公告' },
  { label: '通告', value: '通告' },
  { label: '意见', value: '意见' },
  { label: '通报', value: '通报' },
  { label: '纪要', value: '纪要' }
]

export const statusOptions = [
  { label: '草稿', value: 0, color: 'default' },
  { label: '已发布', value: 1, color: 'success' },
  { label: '已停用', value: 2, color: 'error' }
]

export const fieldTypeOptions = [
  { label: '单行输入', value: 'input' },
  { label: '多行输入', value: 'textarea' },
  { label: '下拉选择', value: 'select' },
  { label: '日期选择', value: 'date' },
  { label: '时间选择', value: 'datetime' },
  { label: '单选框', value: 'radio' },
  { label: '复选框', value: 'checkbox' },
  { label: '富文本', value: 'editor' },
  { label: '文件上传', value: 'upload' }
]

export const fieldGroupOptions = [
  { label: '首部', value: 'header' },
  { label: '主体', value: 'main' },
  { label: '尾部', value: 'footer' },
  { label: '附件', value: 'attachment' }
]

export const textAlignOptions = [
  { label: '左对齐', value: 'left' },
  { label: '居中', value: 'center' },
  { label: '右对齐', value: 'right' }
]

export const fontOptions = [
  { label: '仿宋', value: '仿宋' },
  { label: '宋体', value: '宋体' },
  { label: '黑体', value: '黑体' },
  { label: '楷体', value: '楷体' },
  { label: '微软雅黑', value: '微软雅黑' }
]

export const securityLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '秘密', value: '秘密' },
  { label: '机密', value: '机密' },
  { label: '绝密', value: '绝密' }
]

export const urgencyLevelOptions = [
  { label: '普通', value: '普通' },
  { label: '加急', value: '加急' },
  { label: '特急', value: '特急' }
]
