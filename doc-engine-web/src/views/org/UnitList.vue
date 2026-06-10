<template>
  <div class="unit-list">
    <div class="left-panel">
      <a-card title="单位管理" :bordered="false">
        <a-space style="margin-bottom: 12px; width: 100%">
          <a-button type="primary" @click="handleAddRoot">
            <template #icon><PlusOutlined /></template>
            新增根单位
          </a-button>
        </a-space>
        <a-input-search
          v-model:value="searchValue"
          placeholder="搜索单位"
          style="margin-bottom: 12px"
          allow-clear
        />
        <a-tree
          :tree-data="filteredTreeData"
          :field-names="{ title: 'unitName', key: 'id', children: 'children' }"
          :selected-keys="selectedKeys"
          @select="handleSelect"
          default-expand-all
          show-icon
        />
      </a-card>
    </div>

    <div class="right-panel">
      <a-card v-if="!currentUnit" :bordered="false">
        <a-empty description="请选择单位" />
      </a-card>
      <a-card v-else :bordered="false" :title="currentUnit.unitName">
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleEdit(currentUnit)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button @click="handleAddChild(currentUnit)">
              <template #icon><PlusOutlined /></template>
              新增子单位
            </a-button>
            <a-popconfirm
              title="确定要删除该单位吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(currentUnit.id)"
            >
              <a-button danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>

        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="单位编码">{{ currentUnit.unitCode }}</a-descriptions-item>
          <a-descriptions-item label="单位名称">{{ currentUnit.unitName }}</a-descriptions-item>
          <a-descriptions-item label="单位类型">
            {{ getUnitTypeLabel(currentUnit.unitType) }}
          </a-descriptions-item>
          <a-descriptions-item label="简称">{{ currentUnit.shortName }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ currentUnit.leader }}</a-descriptions-item>
          <a-descriptions-item label="电话">{{ currentUnit.phone }}</a-descriptions-item>
          <a-descriptions-item label="地址">{{ currentUnit.address }}</a-descriptions-item>
          <a-descriptions-item label="排序">{{ currentUnit.sortOrder }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentUnit.status)">
              {{ getStatusLabel(currentUnit.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="备注">{{ currentUnit.remark }}</a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="saving"
      @ok="handleSave"
      @cancel="handleModalCancel"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="单位编码" name="unitCode">
              <a-input v-model:value="formData.unitCode" placeholder="请输入单位编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="单位名称" name="unitName">
              <a-input v-model:value="formData.unitName" placeholder="请输入单位名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="单位类型" name="unitType">
              <a-select v-model:value="formData.unitType" placeholder="请选择单位类型" :options="unitTypeOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="简称" name="shortName">
              <a-input v-model:value="formData.shortName" placeholder="请输入简称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="负责人" name="leader">
              <a-input v-model:value="formData.leader" placeholder="请输入负责人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电话" name="phone">
              <a-input v-model:value="formData.phone" placeholder="请输入电话" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="地址" name="address">
              <a-input v-model:value="formData.address" placeholder="请输入地址" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序" name="sortOrder">
              <a-input-number v-model:value="formData.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="备注" name="remark">
              <a-textarea v-model:value="formData.remark" placeholder="请输入备注" :rows="3" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getUnitTree, getUnitDetail, saveUnit, updateUnit, deleteUnit } from '@/api/org'
import type { SysUnitVO, SysUnitSaveDTO } from '@/types/org'
import { unitTypeOptions, orgStatusOptions } from '@/types/org'

const loading = ref(false)
const saving = ref(false)
const treeData = ref<SysUnitVO[]>([])
const searchValue = ref('')
const selectedKeys = ref<number[]>([])
const currentUnit = ref<SysUnitVO | null>(null)
const modalVisible = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<SysUnitSaveDTO>({
  unitCode: '',
  unitName: '',
  unitType: undefined,
  parentId: null,
  shortName: undefined,
  leader: undefined,
  phone: undefined,
  address: undefined,
  sortOrder: 0,
  remark: undefined
})

const rules = {
  unitCode: [{ required: true, message: '请输入单位编码', trigger: 'blur' }],
  unitName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }]
}

const isEdit = computed(() => !!formData.id)
const modalTitle = computed(() => isEdit.value ? '编辑单位' : '新增单位')

const filteredTreeData = computed(() => {
  if (!searchValue.value) return treeData.value
  const filter = (nodes: SysUnitVO[]): SysUnitVO[] => {
    return nodes
      .map(node => {
        if (node.unitName.includes(searchValue.value) || node.unitCode.includes(searchValue.value)) {
          return { ...node }
        }
        const filteredChildren = filter(node.children || [])
        if (filteredChildren.length > 0) {
          return { ...node, children: filteredChildren }
        }
        return null
      })
      .filter((item): item is SysUnitVO => item !== null)
  }
  return filter(treeData.value)
})

const getUnitTypeLabel = (type: string) => {
  return unitTypeOptions.find(opt => opt.value === type)?.label || type
}

const getStatusLabel = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.label || '未知'
}

const getStatusColor = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.color || 'default'
}

const fetchTreeData = async () => {
  loading.value = true
  try {
    const res = await getUnitTree()
    if (res.code === 200) {
      treeData.value = res.data
    } else {
      message.error(res.message || '获取单位树失败')
    }
  } catch (error) {
    message.error('获取单位树失败')
  } finally {
    loading.value = false
  }
}

const handleSelect = async (keys: number[]) => {
  if (keys.length === 0) return
  selectedKeys.value = keys
  try {
    const res = await getUnitDetail(keys[0])
    if (res.code === 200) {
      currentUnit.value = res.data
    } else {
      message.error(res.message || '获取单位详情失败')
    }
  } catch (error) {
    message.error('获取单位详情失败')
  }
}

const resetFormData = () => {
  formData.id = undefined
  formData.unitCode = ''
  formData.unitName = ''
  formData.unitType = undefined
  formData.parentId = null
  formData.shortName = undefined
  formData.leader = undefined
  formData.phone = undefined
  formData.address = undefined
  formData.sortOrder = 0
  formData.remark = undefined
}

const handleAddRoot = () => {
  resetFormData()
  formData.parentId = null
  modalVisible.value = true
}

const handleAddChild = (unit: SysUnitVO) => {
  resetFormData()
  formData.parentId = unit.id
  modalVisible.value = true
}

const handleEdit = (unit: SysUnitVO) => {
  formData.id = unit.id
  formData.unitCode = unit.unitCode
  formData.unitName = unit.unitName
  formData.unitType = unit.unitType
  formData.parentId = unit.parentId
  formData.shortName = unit.shortName
  formData.leader = unit.leader
  formData.phone = unit.phone
  formData.address = unit.address
  formData.sortOrder = unit.sortOrder
  formData.remark = unit.remark
  modalVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    if (isEdit.value) {
      const res = await updateUnit(formData)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        fetchTreeData()
        if (selectedKeys.value.length > 0) {
          const detailRes = await getUnitDetail(selectedKeys.value[0])
          if (detailRes.code === 200) {
            currentUnit.value = detailRes.data
          }
        }
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await saveUnit(formData)
      if (res.code === 200) {
        message.success('新增成功')
        modalVisible.value = false
        fetchTreeData()
      } else {
        message.error(res.message || '新增失败')
      }
    }
  } catch (error) {
    message.warning('请完善必填项')
  } finally {
    saving.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleDelete = async (id: number) => {
  try {
    const res = await deleteUnit(id)
    if (res.code === 200) {
      message.success('删除成功')
      currentUnit.value = null
      selectedKeys.value = []
      fetchTreeData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchTreeData()
})
</script>

<style scoped lang="less">
.unit-list {
  display: flex;
  gap: 16px;
  padding: 16px;
  height: calc(100vh - 120px);

  .left-panel {
    width: 300px;
    flex-shrink: 0;
    overflow: auto;
  }

  .right-panel {
    flex: 1;
    overflow: auto;
  }
}
</style>
