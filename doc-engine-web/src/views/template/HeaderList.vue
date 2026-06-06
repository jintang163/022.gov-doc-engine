<template>
  <div class="template-header-list">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <span>红头配置管理</span>
        </a-space>
      </template>

      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增红头配置
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :width="800"
      :mask-closable="false"
      :footer="null"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="24">
          <a-col :span="24">
            <a-form-item label="配置名称" name="headerName">
              <a-input v-model:value="formData.headerName" placeholder="请输入红头配置名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>单位名称配置</a-divider>

        <a-row :gutter="24">
          <a-col :span="16">
            <a-form-item label="发文单位名称" name="unitName">
              <a-input v-model:value="formData.unitName" placeholder="请输入发文单位名称" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="是否加粗">
              <a-switch
                v-model:checked="headerBoldChecked"
                checked-children="是"
                un-checked-children="否"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="单位名称字体" name="unitNameFont">
              <a-select
                v-model:value="formData.unitNameFont"
                placeholder="请选择字体"
                :options="fontOptions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="单位名称字号" name="unitNameFontSize">
              <a-input-number
                v-model:value="formData.unitNameFontSize"
                :min="12"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="单位名称颜色" name="unitNameFontColor">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                <a-input
                  v-model:value="formData.unitNameFontColor"
                  placeholder="#C60000"
                  style="flex: 1"
                />
                <input
                  type="color"
                  v-model="formData.unitNameFontColor"
                  style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="对齐方式" name="unitNameTextAlign">
              <a-select
                v-model:value="formData.unitNameTextAlign"
                placeholder="请选择对齐方式"
                :options="textAlignOptions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="上边距(px)" name="unitNameMarginTop">
              <a-input-number
                v-model:value="formData.unitNameMarginTop"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="下边距(px)" name="unitNameMarginBottom">
              <a-input-number
                v-model:value="formData.unitNameMarginBottom"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>发文字号配置</a-divider>

        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="是否显示发文字号">
              <a-switch
                v-model:checked="showDocNumberChecked"
                checked-children="显示"
                un-checked-children="隐藏"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="发文字号前缀" name="documentNumberPrefix">
              <a-input
                v-model:value="formData.documentNumberPrefix"
                placeholder="如：XX政办发"
                :disabled="!showDocNumberChecked"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="发文字号年份" name="documentNumberYear">
              <a-input
                v-model:value="formData.documentNumberYear"
                placeholder="如：〔2024〕"
                :disabled="!showDocNumberChecked"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24" v-if="showDocNumberChecked">
          <a-col :span="6">
            <a-form-item label="字体" name="documentNumberFont">
              <a-select
                v-model:value="formData.documentNumberFont"
                placeholder="请选择字体"
                :options="fontOptions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="字号" name="documentNumberFontSize">
              <a-input-number
                v-model:value="formData.documentNumberFontSize"
                :min="12"
                :max="72"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="颜色" name="documentNumberFontColor">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                <a-input
                  v-model:value="formData.documentNumberFontColor"
                  placeholder="#000000"
                  style="flex: 1"
                />
                <input
                  type="color"
                  v-model="formData.documentNumberFontColor"
                  style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="对齐方式" name="documentNumberTextAlign">
              <a-select
                v-model:value="formData.documentNumberTextAlign"
                placeholder="请选择"
                :options="textAlignOptions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="上边距(px)" name="documentNumberMarginTop">
              <a-input-number
                v-model:value="formData.documentNumberMarginTop"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="下边距(px)" name="documentNumberMarginBottom">
              <a-input-number
                v-model:value="formData.documentNumberMarginBottom"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>分隔线与五角星配置</a-divider>

        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="是否显示红色分隔线">
              <a-switch
                v-model:checked="showRedLineChecked"
                checked-children="显示"
                un-checked-children="隐藏"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="showRedLineChecked">
            <a-form-item label="分隔线宽度(%)" name="redLineWidth">
              <a-input-number
                v-model:value="formData.redLineWidth"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="showRedLineChecked">
            <a-form-item label="分隔线高度(px)" name="redLineHeight">
              <a-input-number
                v-model:value="formData.redLineHeight"
                :min="1"
                :max="20"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24" v-if="showRedLineChecked">
          <a-col :span="8">
            <a-form-item label="分隔线颜色" name="redLineColor">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                <a-input
                  v-model:value="formData.redLineColor"
                  placeholder="#C60000"
                  style="flex: 1"
                />
                <input
                  type="color"
                  v-model="formData.redLineColor"
                  style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="上边距(px)" name="redLineMarginTop">
              <a-input-number
                v-model:value="formData.redLineMarginTop"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="下边距(px)" name="redLineMarginBottom">
              <a-input-number
                v-model:value="formData.redLineMarginBottom"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-divider />
        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="是否显示五角星">
              <a-switch
                v-model:checked="showStarChecked"
                checked-children="显示"
                un-checked-children="隐藏"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="showStarChecked">
            <a-form-item label="五角星大小(px)" name="starSize">
              <a-input-number
                v-model:value="formData.starSize"
                :min="10"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="showStarChecked">
            <a-form-item label="五角星颜色" name="starColor">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                <a-input
                  v-model:value="formData.starColor"
                  placeholder="#C60000"
                  style="flex: 1"
                />
                <input
                  type="color"
                  v-model="formData.starColor"
                  style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                />
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>页面配置</a-divider>

        <a-row :gutter="24">
          <a-col :span="6">
            <a-form-item label="页面宽度(mm)" name="pageWidth">
              <a-input-number
                v-model:value="formData.pageWidth"
                :min="100"
                :max="500"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="页面高度(mm)" name="pageHeight">
              <a-input-number
                v-model:value="formData.pageHeight"
                :min="100"
                :max="500"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="左边距(mm)" name="pageMarginLeft">
              <a-input-number
                v-model:value="formData.pageMarginLeft"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="右边距(mm)" name="pageMarginRight">
              <a-input-number
                v-model:value="formData.pageMarginRight"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="上边距(mm)" name="pageMarginTop">
              <a-input-number
                v-model:value="formData.pageMarginTop"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="下边距(mm)" name="pageMarginBottom">
              <a-input-number
                v-model:value="formData.pageMarginBottom"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider />

        <a-form-item label="自定义CSS">
          <a-textarea
            v-model:value="formData.customCss"
            placeholder="请输入自定义CSS样式"
            :rows="4"
          />
        </a-form-item>

        <div style="text-align: right">
          <a-space>
            <a-button @click="handleModalCancel">取消</a-button>
            <a-button type="primary" :loading="saving" @click="handleModalSave">保存</a-button>
          </a-space>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import {
  getTemplateHeaderList,
  getTemplateHeaderDetail,
  saveTemplateHeader,
  updateTemplateHeader,
  deleteTemplateHeader
} from '@/api/template'
import type { DocTemplateHeaderDTO } from '@/types/template'

const loading = ref(false)
const dataSource = ref<DocTemplateHeaderDTO[]>([])
const modalVisible = ref(false)
const modalTitle = ref('新增红头配置')
const formRef = ref()
const saving = ref(false)
const isEdit = ref(false)
const editingId = ref<number>(0)

const headerBoldChecked = ref(true)
const showDocNumberChecked = ref(true)
const showRedLineChecked = ref(true)
const showStarChecked = ref(true)

const fontOptions = [
  { label: '宋体', value: '宋体' },
  { label: '黑体', value: '黑体' },
  { label: '楷体', value: '楷体' },
  { label: '仿宋', value: '仿宋' },
  { label: '微软雅黑', value: '微软雅黑' },
  { label: 'Arial', value: 'Arial' },
  { label: 'Times New Roman', value: 'Times New Roman' }
]

const textAlignOptions = [
  { label: '左对齐', value: 'left' },
  { label: '居中', value: 'center' },
  { label: '右对齐', value: 'right' }
]

const defaultForm: DocTemplateHeaderDTO = {
  headerName: '',
  unitName: '',
  unitNameFont: '宋体',
  unitNameFontSize: 54,
  unitNameFontColor: '#C60000',
  unitNameFontBold: 1,
  unitNameTextAlign: 'center',
  unitNameMarginTop: 0,
  unitNameMarginBottom: 10,
  showDocumentNumber: 1,
  documentNumberPrefix: '',
  documentNumberYear: '',
  documentNumberFont: '仿宋',
  documentNumberFontSize: 16,
  documentNumberFontColor: '#000000',
  documentNumberTextAlign: 'right',
  documentNumberMarginTop: 10,
  documentNumberMarginBottom: 10,
  showRedLine: 1,
  redLineWidth: 100,
  redLineHeight: 2,
  redLineColor: '#C60000',
  redLineMarginTop: 10,
  redLineMarginBottom: 10,
  showStar: 1,
  starSize: 30,
  starColor: '#C60000',
  pageWidth: 210,
  pageHeight: 297,
  pageMarginTop: 37,
  pageMarginBottom: 35,
  pageMarginLeft: 28,
  pageMarginRight: 26,
  customCss: ''
}

const formData = reactive<DocTemplateHeaderDTO>({ ...defaultForm })

const columns: TableColumnsType = [
  { title: '配置名称', dataIndex: 'headerName', key: 'headerName', width: 200 },
  { title: '发文单位', dataIndex: 'unitName', key: 'unitName' },
  { title: '发文字号前缀', dataIndex: 'documentNumberPrefix', key: 'documentNumberPrefix', width: 150 },
  { title: '页面大小', key: 'pageSize', width: 120,
    customRender: ({ record }) => `${record.pageWidth}×${record.pageHeight}mm`
  },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const rules = {
  headerName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  unitName: [{ required: true, message: '请输入发文单位名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTemplateHeaderList()
    if (res.code === 200 && res.data) {
      dataSource.value = res.data
    } else {
      message.error(res.message || '加载失败')
    }
  } catch (error) {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = 0
  Object.assign(formData, { ...defaultForm })
  headerBoldChecked.value = true
  showDocNumberChecked.value = true
  showRedLineChecked.value = true
  showStarChecked.value = true
  modalTitle.value = '新增红头配置'
  modalVisible.value = true
}

const handleEdit = async (record: DocTemplateHeaderDTO) => {
  isEdit.value = true
  editingId.value = record.id!
  try {
    const res = await getTemplateHeaderDetail(record.id!)
    if (res.code === 200 && res.data) {
      Object.assign(formData, { ...defaultForm, ...res.data })
      headerBoldChecked.value = formData.unitNameFontBold === 1
      showDocNumberChecked.value = formData.showDocumentNumber === 1
      showRedLineChecked.value = formData.showRedLine === 1
      showStarChecked.value = formData.showStar === 1
      modalTitle.value = '编辑红头配置'
      modalVisible.value = true
    } else {
      message.error(res.message || '获取详情失败')
    }
  } catch (error) {
    message.error('获取详情失败')
  }
}

const handleDelete = (record: DocTemplateHeaderDTO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除红头配置"${record.headerName}"吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteTemplateHeader(record.id!)
        if (res.code === 200) {
          message.success('删除成功')
          loadData()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleModalCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleModalSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true

    const saveData: DocTemplateHeaderDTO = {
      ...formData,
      unitNameFontBold: headerBoldChecked.value ? 1 : 0,
      showDocumentNumber: showDocNumberChecked.value ? 1 : 0,
      showRedLine: showRedLineChecked.value ? 1 : 0,
      showStar: showStarChecked.value ? 1 : 0
    }

    let res
    if (isEdit.value) {
      saveData.id = editingId.value
      res = await updateTemplateHeader(saveData)
    } else {
      res = await saveTemplateHeader(saveData)
    }

    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '保存成功')
      modalVisible.value = false
      loadData()
    } else {
      message.error(res.message || (isEdit.value ? '更新失败' : '保存失败'))
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    saving.value = false
  }
}

loadData()
</script>

<style scoped lang="less">
.template-header-list {
  padding: 16px;
}
</style>
