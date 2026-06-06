<template>
  <div class="template-form">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <a-button @click="handleBack">
            <template #icon><ArrowLeftOutlined /></template>
            返回
          </a-button>
          <span>{{ pageTitle }}</span>
        </a-space>
      </template>

      <a-steps :current="currentStep" class="form-steps">
        <a-step title="基本信息" />
        <a-step title="红头样式" />
        <a-step title="字段配置" />
        <a-step title="完成" />
      </a-steps>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
        class="form-content"
      >
        <div v-show="currentStep === 0" class="step-content">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="模板编码" name="templateCode">
                <a-input v-model:value="formData.templateCode" placeholder="请输入模板编码" :disabled="isEdit" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="模板名称" name="templateName">
                <a-input v-model:value="formData.templateName" placeholder="请输入模板名称" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="模板类型" name="templateType">
                <a-select
                  v-model:value="formData.templateType"
                  placeholder="请选择模板类型"
                  :options="templateTypeOptions"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="模板分类" name="templateCategory">
                <a-select
                  v-model:value="formData.templateCategory"
                  placeholder="请选择模板分类"
                  :options="templateCategoryOptions"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="版本号">
                <a-input-number v-model:value="formData.version" :min="1" disabled style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="适用单位">
                <a-input v-model:value="formData.unitName" placeholder="请输入适用单位" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="保密等级" name="securityLevel">
                <a-select
                  v-model:value="formData.securityLevel"
                  placeholder="请选择保密等级"
                  :options="securityLevelOptions"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="紧急程度" name="urgencyLevel">
                <a-select
                  v-model:value="formData.urgencyLevel"
                  placeholder="请选择紧急程度"
                  :options="urgencyLevelOptions"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="模板说明" name="description">
                <a-textarea
                  v-model:value="formData.description"
                  placeholder="请输入模板说明"
                  :rows="3"
                  show-count
                  :max-length="500"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-divider>权限配置</a-divider>

          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item label="可用角色">
                <a-textarea
                  v-model:value="formData.permissionRoles"
                  placeholder="请输入角色编码，多个用逗号分隔"
                  :rows="2"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="可用用户">
                <a-textarea
                  v-model:value="formData.permissionUsers"
                  placeholder="请输入用户账号，多个用逗号分隔"
                  :rows="2"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="可用部门">
                <a-textarea
                  v-model:value="formData.permissionDepts"
                  placeholder="请输入部门编码，多个用逗号分隔"
                  :rows="2"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </div>

        <div v-show="currentStep === 1" class="step-content">
          <a-collapse v-model:active-key="headerActiveKeys" accordion>
            <a-collapse-panel key="unit" header="单位名称配置">
              <a-row :gutter="24">
                <a-col :span="16">
                  <a-form-item label="发文单位名称" name="header.unitName">
                    <a-input v-model:value="formData.header.unitName" placeholder="请输入发文单位名称" />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="是否加粗">
                    <a-switch
                      v-model:checked="headerBoldChecked"
                      checked-children="是"
                      un-checked-children="否"
                      @change="handleHeaderBoldChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="单位名称字体" name="header.unitNameFont">
                    <a-select
                      v-model:value="formData.header.unitNameFont"
                      placeholder="请选择字体"
                      :options="fontOptions"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="单位名称字号" name="header.unitNameFontSize">
                    <a-input-number
                      v-model:value="formData.header.unitNameFontSize"
                      :min="12"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="单位名称颜色" name="header.unitNameFontColor">
                    <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                      <a-input
                        v-model:value="formData.header.unitNameFontColor"
                        placeholder="#C60000"
                        style="flex: 1"
                      />
                      <input
                        type="color"
                        v-model="formData.header.unitNameFontColor"
                        style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                      />
                    </div>
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="对齐方式" name="header.unitNameTextAlign">
                    <a-select
                      v-model:value="formData.header.unitNameTextAlign"
                      placeholder="请选择对齐方式"
                      :options="textAlignOptions"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="上边距(px)" name="header.unitNameMarginTop">
                    <a-input-number
                      v-model:value="formData.header.unitNameMarginTop"
                      :min="0"
                      :max="200"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="下边距(px)" name="header.unitNameMarginBottom">
                    <a-input-number
                      v-model:value="formData.header.unitNameMarginBottom"
                      :min="0"
                      :max="200"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-collapse-panel>

            <a-collapse-panel key="docNumber" header="发文字号配置">
              <a-row :gutter="24">
                <a-col :span="8">
                  <a-form-item label="是否显示发文字号">
                    <a-switch
                      v-model:checked="showDocNumberChecked"
                      checked-children="显示"
                      un-checked-children="隐藏"
                      @change="handleShowDocNumberChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="发文字号前缀" name="header.documentNumberPrefix">
                    <a-input
                      v-model:value="formData.header.documentNumberPrefix"
                      placeholder="如：XX政办发"
                      :disabled="!showDocNumberChecked"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="发文字号年份" name="header.documentNumberYear">
                    <a-input
                      v-model:value="formData.header.documentNumberYear"
                      placeholder="如：〔2024〕"
                      :disabled="!showDocNumberChecked"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="24" v-if="showDocNumberChecked">
                <a-col :span="6">
                  <a-form-item label="字体" name="header.documentNumberFont">
                    <a-select
                      v-model:value="formData.header.documentNumberFont"
                      placeholder="请选择字体"
                      :options="fontOptions"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="字号" name="header.documentNumberFontSize">
                    <a-input-number
                      v-model:value="formData.header.documentNumberFontSize"
                      :min="12"
                      :max="72"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="颜色" name="header.documentNumberFontColor">
                    <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                      <a-input
                        v-model:value="formData.header.documentNumberFontColor"
                        placeholder="#000000"
                        style="flex: 1"
                      />
                      <input
                        type="color"
                        v-model="formData.header.documentNumberFontColor"
                        style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                      />
                    </div>
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="对齐方式" name="header.documentNumberTextAlign">
                    <a-select
                      v-model:value="formData.header.documentNumberTextAlign"
                      placeholder="请选择"
                      :options="textAlignOptions"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="上边距(px)" name="header.documentNumberMarginTop">
                    <a-input-number
                      v-model:value="formData.header.documentNumberMarginTop"
                      :min="0"
                      :max="200"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="下边距(px)" name="header.documentNumberMarginBottom">
                    <a-input-number
                      v-model:value="formData.header.documentNumberMarginBottom"
                      :min="0"
                      :max="200"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-collapse-panel>

            <a-collapse-panel key="divider" header="分隔线与五角星配置">
              <a-row :gutter="24">
                <a-col :span="8">
                  <a-form-item label="是否显示红色分隔线">
                    <a-switch
                      v-model:checked="showRedLineChecked"
                      checked-children="显示"
                      un-checked-children="隐藏"
                      @change="handleShowRedLineChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="showRedLineChecked">
                  <a-form-item label="分隔线宽度(%)" name="header.redLineWidth">
                    <a-input-number
                      v-model:value="formData.header.redLineWidth"
                      :min="1"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="showRedLineChecked">
                  <a-form-item label="分隔线高度(px)" name="header.redLineHeight">
                    <a-input-number
                      v-model:value="formData.header.redLineHeight"
                      :min="1"
                      :max="20"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="24" v-if="showRedLineChecked">
                <a-col :span="8">
                  <a-form-item label="分隔线颜色" name="header.redLineColor">
                    <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                      <a-input
                        v-model:value="formData.header.redLineColor"
                        placeholder="#C60000"
                        style="flex: 1"
                      />
                      <input
                        type="color"
                        v-model="formData.header.redLineColor"
                        style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                      />
                    </div>
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="上边距(px)" name="header.redLineMarginTop">
                    <a-input-number
                      v-model:value="formData.header.redLineMarginTop"
                      :min="0"
                      :max="200"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="下边距(px)" name="header.redLineMarginBottom">
                    <a-input-number
                      v-model:value="formData.header.redLineMarginBottom"
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
                      @change="handleShowStarChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="showStarChecked">
                  <a-form-item label="五角星大小(px)" name="header.starSize">
                    <a-input-number
                      v-model:value="formData.header.starSize"
                      :min="10"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" v-if="showStarChecked">
                  <a-form-item label="五角星颜色" name="header.starColor">
                    <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                      <a-input
                        v-model:value="formData.header.starColor"
                        placeholder="#C60000"
                        style="flex: 1"
                      />
                      <input
                        type="color"
                        v-model="formData.header.starColor"
                        style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                      />
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
            </a-collapse-panel>

            <a-collapse-panel key="page" header="页面配置">
              <a-row :gutter="24">
                <a-col :span="6">
                  <a-form-item label="页面宽度(mm)" name="header.pageWidth">
                    <a-input-number
                      v-model:value="formData.header.pageWidth"
                      :min="100"
                      :max="500"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="页面高度(mm)" name="header.pageHeight">
                    <a-input-number
                      v-model:value="formData.header.pageHeight"
                      :min="100"
                      :max="500"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="左边距(mm)" name="header.pageMarginLeft">
                    <a-input-number
                      v-model:value="formData.header.pageMarginLeft"
                      :min="0"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="右边距(mm)" name="header.pageMarginRight">
                    <a-input-number
                      v-model:value="formData.header.pageMarginRight"
                      :min="0"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="上边距(mm)" name="header.pageMarginTop">
                    <a-input-number
                      v-model:value="formData.header.pageMarginTop"
                      :min="0"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="下边距(mm)" name="header.pageMarginBottom">
                    <a-input-number
                      v-model:value="formData.header.pageMarginBottom"
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
                  v-model:value="formData.header.customCss"
                  placeholder="请输入自定义CSS样式"
                  :rows="6"
                  :auto-size="{ minRows: 4, maxRows: 10 }"
                />
              </a-form-item>
            </a-collapse-panel>
          </a-collapse>

          <div style="margin-top: 16px; text-align: right">
            <a-button type="primary" @click="handlePreviewHeader" :disabled="!isEdit">
              <template #icon><EyeOutlined /></template>
              预览红头样式
            </a-button>
          </div>
        </div>

        <div v-show="currentStep === 2" class="step-content">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="handleAddField">
              <template #icon><PlusOutlined /></template>
              添加自定义字段
            </a-button>
            <a-tag color="blue" style="margin-left: 16px">预置字段不可删除，可编辑</a-tag>
          </div>

          <a-table
            :data-source="formData.fields"
            :columns="fieldColumns"
            :pagination="false"
            row-key="fieldKey"
            :scroll="{ y: 500 }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'sort'">
                <a-space size="small">
                  <a-button
                    type="text"
                    size="small"
                    :disabled="index === 0"
                    @click="handleMoveField(index, -1)"
                  >
                    <ArrowUpOutlined />
                  </a-button>
                  <a-button
                    type="text"
                    size="small"
                    :disabled="index === (formData.fields?.length || 0) - 1"
                    @click="handleMoveField(index, 1)"
                  >
                    <ArrowDownOutlined />
                  </a-button>
                  <span style="margin-left: 8px">{{ record.sortOrder || index + 1 }}</span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'fieldKey'">
                <a-input
                  v-model:value="record.fieldKey"
                  :disabled="record.isPreset === 1"
                  placeholder="字段键名"
                />
              </template>
              <template v-else-if="column.key === 'fieldName'">
                <a-input
                  v-model:value="record.fieldName"
                  placeholder="字段名称"
                />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select
                  v-model:value="record.fieldType"
                  :options="fieldTypeOptions"
                  style="width: 100%"
                />
              </template>
              <template v-else-if="column.key === 'fieldGroup'">
                <a-select
                  v-model:value="record.fieldGroup"
                  :options="fieldGroupOptions"
                  style="width: 100%"
                  allow-clear
                />
              </template>
              <template v-else-if="column.key === 'isRequired'">
                <a-switch
                  :checked="record.isRequired === 1"
                  checked-children="是"
                  un-checked-children="否"
                  @change="(val) => handleFieldRequiredChange(record, val)"
                />
              </template>
              <template v-else-if="column.key === 'isPreset'">
                <a-tag :color="record.isPreset === 1 ? 'blue' : 'default'">
                  {{ record.isPreset === 1 ? '预置' : '自定义' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a-button type="link" size="small" @click="handleEditFieldStyle(record)">
                    样式配置
                  </a-button>
                  <a-button
                    v-if="record.isPreset !== 1"
                    type="link"
                    size="small"
                    danger
                    @click="handleDeleteField(index)"
                  >
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>

        <div v-show="currentStep === 3" class="step-content">
          <a-result status="success" title="配置完成" sub-title="请确认以下信息无误后提交保存">
            <template #extra>
              <a-descriptions bordered :column="2" class="summary">
                <a-descriptions-item label="模板编码">{{ formData.templateCode }}</a-descriptions-item>
                <a-descriptions-item label="模板名称">{{ formData.templateName }}</a-descriptions-item>
                <a-descriptions-item label="模板类型">{{ formData.templateType }}</a-descriptions-item>
                <a-descriptions-item label="模板分类">{{ formData.templateCategory || '-' }}</a-descriptions-item>
                <a-descriptions-item label="版本号">{{ formData.version }}</a-descriptions-item>
                <a-descriptions-item label="适用单位">{{ formData.unitName || '-' }}</a-descriptions-item>
                <a-descriptions-item label="保密等级">{{ formData.securityLevel || '-' }}</a-descriptions-item>
                <a-descriptions-item label="紧急程度">{{ formData.urgencyLevel || '-' }}</a-descriptions-item>
                <a-descriptions-item label="红头单位">{{ formData.header.unitName }}</a-descriptions-item>
                <a-descriptions-item label="字段数量">{{ formData.fields?.length || 0 }}</a-descriptions-item>
                <a-descriptions-item label="模板说明" :span="2">{{ formData.description || '-' }}</a-descriptions-item>
              </a-descriptions>

              <div style="margin-top: 24px">
                <a-space>
                  <a-button type="primary" size="large" :disabled="!isEdit" @click="handlePreviewHeader">
                    <template #icon><EyeOutlined /></template>
                    预览红头样式
                  </a-button>
                  <a-button type="primary" size="large" :loading="saving" @click="handleSave">
                    <template #icon><SaveOutlined /></template>
                    {{ isEdit ? '更新模板' : '保存模板' }}
                  </a-button>
                </a-space>
              </div>
            </template>
          </a-result>
        </div>

        <div class="form-actions">
          <a-button v-if="currentStep > 0" @click="handlePrev">
            上一步
          </a-button>
          <a-button
            v-if="currentStep < 3"
            type="primary"
            @click="handleNext"
          >
            下一步
          </a-button>
        </div>
      </a-form>
    </a-card>

    <a-modal
      v-model:open="fieldStyleModalVisible"
      title="字段样式配置"
      :width="800"
      @ok="handleFieldStyleOk"
      @cancel="fieldStyleModalVisible = false"
    >
      <a-form layout="vertical" :model="currentEditField">
        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="默认值">
              <a-input v-model:value="currentEditField.defaultValue" placeholder="默认值" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="占位提示">
              <a-input v-model:value="currentEditField.placeholder" placeholder="占位提示" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序号">
              <a-input-number v-model:value="currentEditField.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="选项值(JSON格式，适用于select/radio/checkbox)">
              <a-textarea
                v-model:value="currentEditField.fieldOptions"
                placeholder='例如：[{"label":"选项1","value":"1"},{"label":"选项2","value":"2"}]'
                :rows="3"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-divider>样式配置</a-divider>
        <a-row :gutter="24">
          <a-col :span="6">
            <a-form-item label="字体">
              <a-select
                v-model:value="currentEditField.fontFamily"
                :options="fontOptions"
                style="width: 100%"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="字号(px)">
              <a-input-number
                v-model:value="currentEditField.fontSize"
                :min="12"
                :max="72"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="字体颜色">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%">
                <a-input
                  v-model:value="currentEditField.fontColor"
                  placeholder="#000000"
                  style="flex: 1"
                />
                <input
                  type="color"
                  v-model="currentEditField.fontColor"
                  style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
                />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="是否加粗">
              <a-switch
                :checked="currentEditField.fontBold === 1"
                checked-children="是"
                un-checked-children="否"
                @change="handleFieldBoldChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="对齐方式">
              <a-select
                v-model:value="currentEditField.textAlign"
                :options="textAlignOptions"
                style="width: 100%"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="行高">
              <a-input-number
                v-model:value="currentEditField.lineHeight"
                :min="1"
                :max="5"
                :step="0.1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="首行缩进(字符)">
              <a-input-number
                v-model:value="currentEditField.textIndent"
                :min="0"
                :max="10"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-divider>边距配置</a-divider>
        <a-row :gutter="24">
          <a-col :span="6">
            <a-form-item label="上边距(px)">
              <a-input-number
                v-model:value="currentEditField.marginTop"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="下边距(px)">
              <a-input-number
                v-model:value="currentEditField.marginBottom"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="左边距(px)">
              <a-input-number
                v-model:value="currentEditField.marginLeft"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="右边距(px)">
              <a-input-number
                v-model:value="currentEditField.marginRight"
                :min="0"
                :max="200"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, TableColumnsType } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  PlusOutlined,
  EyeOutlined,
  SaveOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons-vue'
import {
  getTemplateDetail,
  saveTemplate,
  updateTemplate,
  getTemplatePreviewUrl
} from '@/api/template'
import type {
  DocTemplateSaveDTO,
  DocTemplateHeaderDTO,
  DocTemplateFieldDTO,
  DocTemplateVO
} from '@/types/template'
import {
  templateTypeOptions,
  templateCategoryOptions,
  fieldTypeOptions,
  fieldGroupOptions,
  textAlignOptions,
  fontOptions,
  securityLevelOptions,
  urgencyLevelOptions
} from '@/types/template'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const saving = ref(false)
const currentStep = ref(0)
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)
const templateId = computed(() => Number(route.params.id) || 0)

const pageTitle = computed(() => (isEdit.value ? '编辑模板' : '新建模板'))

const headerActiveKeys = ref<string[]>(['unit'])

const headerBoldChecked = ref(true)
const showDocNumberChecked = ref(true)
const showRedLineChecked = ref(true)
const showStarChecked = ref(true)

const fieldStyleModalVisible = ref(false)
const currentEditField = ref<DocTemplateFieldDTO>({} as DocTemplateFieldDTO)
let currentEditFieldIndex = -1

const presetFields: DocTemplateFieldDTO[] = [
  { fieldKey: 'docNumber', fieldName: '文号', fieldType: 'input', fieldGroup: 'header', isPreset: 1, isRequired: 1, sortOrder: 1 },
  { fieldKey: 'signer', fieldName: '签发人', fieldType: 'input', fieldGroup: 'header', isPreset: 1, isRequired: 0, sortOrder: 2 },
  { fieldKey: 'docTitle', fieldName: '标题', fieldType: 'input', fieldGroup: 'main', isPreset: 1, isRequired: 1, sortOrder: 3 },
  { fieldKey: 'mainSendDept', fieldName: '主送机关', fieldType: 'input', fieldGroup: 'main', isPreset: 1, isRequired: 0, sortOrder: 4 },
  { fieldKey: 'docContent', fieldName: '正文', fieldType: 'editor', fieldGroup: 'main', isPreset: 1, isRequired: 1, sortOrder: 5 },
  { fieldKey: 'attachmentInfo', fieldName: '附件', fieldType: 'textarea', fieldGroup: 'attachment', isPreset: 1, isRequired: 0, sortOrder: 6 },
  { fieldKey: 'writtenDate', fieldName: '成文日期', fieldType: 'date', fieldGroup: 'footer', isPreset: 1, isRequired: 1, sortOrder: 7 },
  { fieldKey: 'copySendDept', fieldName: '抄送机关', fieldType: 'input', fieldGroup: 'footer', isPreset: 1, isRequired: 0, sortOrder: 8 }
]

const defaultHeader: DocTemplateHeaderDTO = {
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

const formData = reactive<DocTemplateSaveDTO>({
  templateCode: '',
  templateName: '',
  templateType: '',
  templateCategory: '',
  version: 1,
  description: '',
  unitName: '',
  securityLevel: '',
  urgencyLevel: '',
  permissionRoles: '',
  permissionUsers: '',
  permissionDepts: '',
  header: { ...defaultHeader },
  fields: JSON.parse(JSON.stringify(presetFields))
})

const fieldColumns: TableColumnsType = [
  { title: '排序', key: 'sort', width: 100 },
  { title: '字段键名', key: 'fieldKey', width: 150 },
  { title: '字段名称', key: 'fieldName', width: 150 },
  { title: '字段类型', key: 'fieldType', width: 120 },
  { title: '字段分组', key: 'fieldGroup', width: 100 },
  { title: '是否必填', key: 'isRequired', width: 100 },
  { title: '类型', key: 'isPreset', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const rules = {
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  'header.unitName': [{ required: true, message: '请输入发文单位名称', trigger: 'blur' }]
}

const handleHeaderBoldChange = (checked: boolean) => {
  formData.header.unitNameFontBold = checked ? 1 : 0
}

const handleShowDocNumberChange = (checked: boolean) => {
  formData.header.showDocumentNumber = checked ? 1 : 0
}

const handleShowRedLineChange = (checked: boolean) => {
  formData.header.showRedLine = checked ? 1 : 0
}

const handleShowStarChange = (checked: boolean) => {
  formData.header.showStar = checked ? 1 : 0
}

const handleFieldRequiredChange = (record: DocTemplateFieldDTO, checked: boolean) => {
  record.isRequired = checked ? 1 : 0
}

const handleFieldBoldChange = (checked: boolean) => {
  currentEditField.value.fontBold = checked ? 1 : 0
}

const handleMoveField = (index: number, direction: number) => {
  if (!formData.fields) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= formData.fields.length) return
  const temp = formData.fields[index]
  formData.fields[index] = formData.fields[newIndex]
  formData.fields[newIndex] = temp
  formData.fields.forEach((f, i) => {
    f.sortOrder = i + 1
  })
}

const handleNext = async () => {
  try {
    await formRef.value?.validate()
    if (currentStep.value < 3) {
      currentStep.value++
    }
  } catch (error) {
    message.warning('请完善必填项')
  }
}

const handlePrev = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleBack = () => {
  router.push('/template')
}

const handleAddField = () => {
  const newField: DocTemplateFieldDTO = {
    fieldKey: `field_${Date.now()}`,
    fieldName: '新字段',
    fieldType: 'input',
    fieldGroup: 'main',
    isPreset: 0,
    isRequired: 0,
    sortOrder: (formData.fields?.length || 0) + 1
  }
  if (!formData.fields) {
    formData.fields = []
  }
  formData.fields.push(newField)
}

const handleDeleteField = (index: number) => {
  if (formData.fields && formData.fields[index]?.isPreset !== 1) {
    formData.fields.splice(index, 1)
    formData.fields.forEach((f, i) => {
      f.sortOrder = i + 1
    })
  }
}

const handleEditFieldStyle = (record: DocTemplateFieldDTO) => {
  currentEditFieldIndex = formData.fields?.findIndex(f => f.fieldKey === record.fieldKey) ?? -1
  currentEditField.value = { ...record }
  fieldStyleModalVisible.value = true
}

const handleFieldStyleOk = () => {
  if (currentEditFieldIndex >= 0 && formData.fields) {
    formData.fields[currentEditFieldIndex] = { ...currentEditField.value }
  }
  fieldStyleModalVisible.value = false
}

const handlePreviewHeader = () => {
  if (templateId.value) {
    window.open(getTemplatePreviewUrl(templateId.value), '_blank')
  } else {
    message.warning('请先保存模板后再预览')
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true

    const saveData: DocTemplateSaveDTO = {
      ...formData,
      header: {
        ...formData.header,
        unitNameFontBold: headerBoldChecked.value ? 1 : 0,
        showDocumentNumber: showDocNumberChecked.value ? 1 : 0,
        showRedLine: showRedLineChecked.value ? 1 : 0,
        showStar: showStarChecked.value ? 1 : 0
      }
    }

    if (isEdit.value) {
      saveData.id = templateId.value
      const res = await updateTemplate(saveData)
      if (res.code === 0) {
        message.success('更新成功')
        router.push('/template')
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await saveTemplate(saveData)
      if (res.code === 0) {
        message.success('保存成功')
        router.push('/template')
      } else {
        message.error(res.message || '保存失败')
      }
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    saving.value = false
  }
}

const loadTemplateDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await getTemplateDetail(id)
    if (res.code === 0 && res.data) {
      const data = res.data as DocTemplateVO
      Object.assign(formData, {
        id: data.id,
        templateCode: data.templateCode,
        templateName: data.templateName,
        templateType: data.templateType,
        templateCategory: data.templateCategory,
        version: data.version,
        description: data.description,
        unitName: data.unitName,
        securityLevel: data.securityLevel,
        urgencyLevel: data.urgencyLevel,
        permissionRoles: data.permissionRoles,
        permissionUsers: data.permissionUsers,
        permissionDepts: data.permissionDepts,
        header: data.header ? { ...defaultHeader, ...data.header } : { ...defaultHeader },
        fields: data.fields && data.fields.length > 0
          ? data.fields.map(f => ({ ...f }))
          : JSON.parse(JSON.stringify(presetFields))
      })

      headerBoldChecked.value = formData.header.unitNameFontBold === 1
      showDocNumberChecked.value = formData.header.showDocumentNumber === 1
      showRedLineChecked.value = formData.header.showRedLine === 1
      showStarChecked.value = formData.header.showStar === 1
    } else {
      message.error(res.message || '获取模板详情失败')
    }
  } catch (error) {
    message.error('获取模板详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit.value && templateId.value) {
    loadTemplateDetail(templateId.value)
  } else {
    headerBoldChecked.value = true
    showDocNumberChecked.value = true
    showRedLineChecked.value = true
    showStarChecked.value = true
  }
})
</script>

<style scoped lang="less">
.template-form {
  padding: 16px;

  .form-steps {
    margin: 24px 0 32px 0;
  }

  .form-content {
    padding: 0 24px;
  }

  .step-content {
    min-height: 400px;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
  }

  .summary {
    margin-top: 24px;
    text-align: left;
  }
}
</style>
