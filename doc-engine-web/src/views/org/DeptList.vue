<template>
  <div class="dept-list">
    <div class="left-panel">
      <a-card title="部门管理" :bordered="false">
        <a-select
          v-model:value="selectedUnitId"
          placeholder="请选择单位"
          style="width: 100%; margin-bottom: 12px"
          :options="unitSelectOptions"
          allow-clear
          @change="handleUnitChange"
        />
        <a-space style="margin-bottom: 12px; width: 100%">
          <a-button type="primary" :disabled="!selectedUnitId" @click="handleAddRoot">
            <template #icon><PlusOutlined /></template>
            新增根部门
          </a-button>
        </a-space>
        <a-input-search
          v-model:value="searchValue"
          placeholder="搜索部门"
          style="margin-bottom: 12px"
          allow-clear
        />
        <a-tree
          :tree-data="filteredTreeData"
          :field-names="{ title: 'deptName', key: 'id', children: 'children' }"
          :selected-keys="selectedKeys"
          @select="handleSelect"
          default-expand-all
        />
      </a-card>
    </div>

    <div class="right-panel">
      <a-card v-if="!currentDept" :bordered="false">
        <a-empty description="请选择部门" />
      </a-card>
      <a-card v-else :bordered="false" :title="currentDept.deptName">
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleEdit(currentDept)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button @click="handleAddChild(currentDept)">
              <template #icon><PlusOutlined /></template>
              新增子部门
            </a-button>
            <a-popconfirm
              title="确定要删除该部门吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(currentDept.id)"
            >
              <a-button danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>

        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="部门编码">{{ currentDept.deptCode }}</a-descriptions-item>
          <a-descriptions-item label="部门名称">{{ currentDept.deptName }}</a-descriptions-item>
          <a-descriptions-item label="部门类型">{{ currentDept.deptType }}</a-descriptions-item>
          <a-descriptions-item label="所属单位">{{ currentDept.unitName }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ currentDept.leader }}</a-descriptions-item>
          <a-descriptions-item label="电话">{{ currentDept.phone }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ currentDept.email }}</a-descriptions-item>
          <a-descriptions-item label="排序">{{ currentDept.sortOrder }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentDept.status)">
              {{ getStatusLabel(currentDept.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="备注">{{ currentDept.remark }}</a-descriptions-item>
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
            <a-form-item label="所属单位" name="unitId">
              <a-select v-model:value="formData.unitId" placeholder="请选择所属单位" :options="unitSelectOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="部门编码" name="deptCode">
              <a-input v-model:value="formData.deptCode" placeholder="请输入部门编码" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="部门名称" name="deptName">
              <a-input v-model:value="formData.deptName" placeholder="请输入部门名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="部门类型" name="deptType">
              <a-input v-model:value="formData.deptType" placeholder="请输入部门类型" />
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
          <a-col :span="12">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
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
import { getUnitTree, getDeptTree, getDeptDetail, saveDept, updateDept, deleteDept } from '@/api/org'
import type { SysUnitVO, SysDeptVO, SysDeptSaveDTO } from '@/types/org'
import { orgStatusOptions } from '@/types/org'

const loading = ref(false)
const saving = ref(false)
const unitTreeData = ref<SysUnitVO[]>([])
const deptTreeData = ref<SysDeptVO[]>([])
const searchValue = ref('')
const selectedUnitId = ref<number | undefined>(undefined)
const selectedKeys = ref<number[]>([])
const currentDept = ref<SysDeptVO | null>(null)
const modalVisible = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<SysDeptSaveDTO>({
  unitId: 0,
  deptCode: '',
  deptName: '',
  parentId: null,
  deptType: undefined,
  leader: undefined,
  phone: undefined,
  email: undefined,
  sortOrder: 0,
  remark: undefined
})

const rules = {
  unitId: [{ required: true, message: '请选择所属单位', trigger: 'change' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const isEdit = computed(() => !!formData.id)
const modalTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

const flattenUnits = (nodes: SysUnitVO[]): { label: string; value: number }[] => {
  const result: { label: string; value: number }[] = []
  const walk = (items: SysUnitVO[]) => {
    for (const item of items) {
      result.push({ label: item.unitName, value: item.id })
      if (item.children?.length) walk(item.children)
    }
  }
  walk(nodes)
  return result
}

const unitSelectOptions = computed(() => flattenUnits(unitTreeData.value))

const filteredTreeData = computed(() => {
  if (!searchValue.value) return deptTreeData.value
  const filter = (nodes: SysDeptVO[]): SysDeptVO[] => {
    return nodes
      .map(node => {
        if (node.deptName.includes(searchValue.value) || node.deptCode.includes(searchValue.value)) {
          return { ...node }
        }
        const filteredChildren = filter(node.children || [])
        if (filteredChildren.length > 0) {
          return { ...node, children: filteredChildren }
        }
        return null
      })
      .filter((item): item is SysDeptVO => item !== null)
  }
  return filter(deptTreeData.value)
})

const getStatusLabel = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.label || '未知'
}

const getStatusColor = (status: number) => {
  return orgStatusOptions.find(opt => opt.value === status)?.color || 'default'
}

const fetchUnitTree = async () => {
  try {
    const res = await getUnitTree()
    if (res.code === 200) {
      unitTreeData.value = res.data
    }
  } catch (error) {
    message.error('获取单位树失败')
  }
}

const fetchDeptTree = async (unitId: number) => {
  loading.value = true
  try {
    const res = await getDeptTree(unitId)
    if (res.code === 200) {
      deptTreeData.value = res.data
    } else {
      message.error(res.message || '获取部门树失败')
    }
  } catch (error) {
    message.error('获取部门树失败')
  } finally {
    loading.value = false
  }
}

const handleUnitChange = (value: number | undefined) => {
  selectedUnitId.value = value
  selectedKeys.value = []
  currentDept.value = null
  deptTreeData.value = []
  if (value) {
    fetchDeptTree(value)
  }
}

const handleSelect = async (keys: number[]) => {
  if (keys.length === 0) return
  selectedKeys.value = keys
  try {
    const res = await getDeptDetail(keys[0])
    if (res.code === 200) {
      currentDept.value = res.data
    } else {
      message.error(res.message || '获取部门详情失败')
    }
  } catch (error) {
    message.error('获取部门详情失败')
  }
}

const resetFormData = () => {
  formData.id = undefined
  formData.unitId = selectedUnitId.value || 0
  formData.deptCode = ''
  formData.deptName = ''
  formData.parentId = null
  formData.deptType = undefined
  formData.leader = undefined
  formData.phone = undefined
  formData.email = undefined
  formData.sortOrder = 0
  formData.remark = undefined
}

const handleAddRoot = () => {
  resetFormData()
  formData.parentId = null
  formData.unitId = selectedUnitId.value!
  modalVisible.value = true
}

const handleAddChild = (dept: SysDeptVO) => {
  resetFormData()
  formData.parentId = dept.id
  formData.unitId = dept.unitId
  modalVisible.value = true
}

const handleEdit = (dept: SysDeptVO) => {
  formData.id = dept.id
  formData.unitId = dept.unitId
  formData.deptCode = dept.deptCode
  formData.deptName = dept.deptName
  formData.parentId = dept.parentId
  formData.deptType = dept.deptType
  formData.leader = dept.leader
  formData.phone = dept.phone
  formData.email = dept.email
  formData.sortOrder = dept.sortOrder
  formData.remark = dept.remark
  modalVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    if (isEdit.value) {
      const res = await updateDept(formData)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        if (selectedUnitId.value) fetchDeptTree(selectedUnitId.value)
        if (selectedKeys.value.length > 0) {
          const detailRes = await getDeptDetail(selectedKeys.value[0])
          if (detailRes.code === 200) {
            currentDept.value = detailRes.data
          }
        }
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await saveDept(formData)
      if (res.code === 200) {
        message.success('新增成功')
        modalVisible.value = false
        if (selectedUnitId.value) fetchDeptTree(selectedUnitId.value)
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
    const res = await deleteDept(id)
    if (res.code === 200) {
      message.success('删除成功')
      currentDept.value = null
      selectedKeys.value = []
      if (selectedUnitId.value) fetchDeptTree(selectedUnitId.value)
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchUnitTree()
})
</script>

<style scoped lang="less">
.dept-list {
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
