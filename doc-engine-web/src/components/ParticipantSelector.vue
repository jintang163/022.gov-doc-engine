<template>
  <div class="participant-selector">
    <div class="selector-header">
      <a-radio-group
        v-model:value="currentType"
        :disabled="types.length === 1"
        @change="handleTypeChange"
      >
        <a-radio-button
          v-for="type in availableTypes"
          :key="type.value"
          :value="type.value"
        >
          {{ type.label }}
        </a-radio-button>
      </a-radio-group>
    </div>

    <div class="selector-body">
      <template v-if="currentType !== 'expression'">
        <a-input
          v-model:value="searchKeyword"
          placeholder="请输入搜索关键词"
          allow-clear
          style="margin-bottom: 12px"
        >
          <template #prefix>
            <search-outlined />
          </template>
        </a-input>

        <div class="selectable-list">
          <a-checkbox-group v-model:value="selectedValues">
            <a-space direction="vertical" :size="8" fill>
              <a-checkbox
                v-for="item in filteredOptions"
                :key="item.value"
                :value="item.value"
                :disabled="!multiple && selectedValues.length > 0 && selectedValues[0] !== item.value"
                class="selectable-item"
              >
                <span class="item-label">{{ item.label }}</span>
              </a-checkbox>
            </a-space>
          </a-checkbox-group>
          <a-empty
            v-if="filteredOptions.length === 0"
            description="暂无数据"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            :image-style="{ height: 80 }"
          />
        </div>
      </template>

      <template v-else>
        <a-textarea
          v-model:value="expressionValue"
          placeholder="请输入表达式，例如：${creator} 或 ${leader}"
          :rows="4"
          @change="handleExpressionChange"
        />
        <p class="expression-tip">
          <info-circle-outlined /> 支持变量表达式，如 ${creator} 表示创建人，${leader} 表示直属领导
        </p>
      </template>
    </div>

    <div class="selected-tags" v-if="selectedParticipants.length > 0">
      <div class="selected-title">
        已选择 {{ selectedParticipants.length }} 人：
      </div>
      <a-tag
        v-for="(item, idx) in selectedParticipants"
        :key="idx"
        :color="getTypeColor(item.participantType!)"
        closable
        @close="handleRemove(item)"
      >
        <component :is="getTypeIcon(item.participantType!)" /> {{ item.participantName }}
      </a-tag>
    </div>

    <a-modal
      v-model:open="pickerVisible"
      title="选择参与者"
      width="600px"
      @ok="handlePickerOk"
      @cancel="pickerVisible = false"
    >
      <div class="modal-content">
        <a-tabs v-model:activeKey="currentType" @change="handleTypeChange">
          <a-tab-pane
            v-for="type in availableTypes"
            :key="type.value"
            :tab="type.label"
          >
            <a-input
              v-model:value="searchKeyword"
              placeholder="请输入搜索关键词"
              allow-clear
              style="margin-bottom: 12px"
            >
              <template #prefix>
                <search-outlined />
              </template>
            </a-input>

            <div class="modal-list">
              <a-checkbox-group v-model:value="tempSelectedValues">
                <a-space direction="vertical" :size="8" fill>
                  <a-checkbox
                    v-for="item in filteredOptions"
                    :key="item.value"
                    :value="item.value"
                    :disabled="!multiple && tempSelectedValues.length > 0 && tempSelectedValues[0] !== item.value"
                  >
                    {{ item.label }}
                  </a-checkbox>
                </a-space>
              </a-checkbox-group>
              <a-empty
                v-if="filteredOptions.length === 0"
                description="暂无数据"
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
                :image-style="{ height: 80 }"
              />
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>

    <div v-if="!inline" class="selector-trigger" @click="openPicker">
      <template v-if="selectedParticipants.length > 0">
        <a-tag
          v-for="(item, idx) in displayParticipants"
          :key="idx"
          :color="getTypeColor(item.participantType!)"
        >
          {{ item.participantName }}
        </a-tag>
        <span v-if="selectedParticipants.length > 3" class="more-count">
          +{{ selectedParticipants.length - 3 }}
        </span>
      </template>
      <span v-else class="placeholder">请选择参与者</span>
      <down-outlined class="trigger-icon" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Empty } from 'ant-design-vue'
import type { WfParticipantDTO } from '@/types/workflow'
import { participantTypeOptions } from '@/types/workflow'
import {
  SearchOutlined,
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  SolutionOutlined,
  CodeOutlined,
  InfoCircleOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

interface Props {
  modelValue?: WfParticipantDTO[]
  multiple?: boolean
  types?: WfParticipantDTO['type'][]
  inline?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: WfParticipantDTO[]): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  multiple: true,
  types: () => ['user', 'post', 'dept', 'role', 'expression'],
  inline: false
})

const emit = defineEmits<Emits>()

const currentType = ref<WfParticipantDTO['type']>('user')
const searchKeyword = ref('')
const selectedValues = ref<string[]>([])
const tempSelectedValues = ref<string[]>([])
const expressionValue = ref('')
const pickerVisible = ref(false)

const mockOptions = ref<Record<WfParticipantDTO['type'], { value: string; label: string }[]>>({
  user: [
    { value: 'user1', label: '张三' },
    { value: 'user2', label: '李四' },
    { value: 'user3', label: '王五' },
    { value: 'user4', label: '赵六' },
    { value: 'user5', label: '钱七' }
  ],
  post: [
    { value: 'post1', label: '局长' },
    { value: 'post2', label: '副局长' },
    { value: 'post3', label: '处长' },
    { value: 'post4', label: '副处长' },
    { value: 'post5', label: '科长' }
  ],
  dept: [
    { value: 'dept1', label: '办公室' },
    { value: 'dept2', label: '人事处' },
    { value: 'dept3', label: '财务处' },
    { value: 'dept4', label: '业务一处' },
    { value: 'dept5', label: '业务二处' }
  ],
  role: [
    { value: 'role1', label: '管理员' },
    { value: 'role2', label: '审批员' },
    { value: 'role3', label: '普通用户' },
    { value: 'role4', label: '访客' }
  ],
  expression: []
})

const availableTypes = computed(() => {
  return participantTypeOptions.filter(t => props.types.includes(t.value))
})

const filteredOptions = computed(() => {
  const options = mockOptions.value[currentType.value] || []
  if (!searchKeyword.value) return options
  return options.filter(opt =>
    opt.label.includes(searchKeyword.value) ||
    opt.value.includes(searchKeyword.value)
  )
})

const selectedParticipants = computed(() => {
  return props.modelValue || []
})

const displayParticipants = computed(() => {
  return selectedParticipants.value.slice(0, 3)
})

const getTypeColor = (type: string): string => {
  const colors: Record<string, string> = {
    user: 'blue',
    post: 'cyan',
    dept: 'geekblue',
    role: 'purple',
    expression: 'default'
  }
  return colors[type] || 'default'
}

const getTypeIcon = (type: string) => {
  const icons: Record<string, any> = {
    user: UserOutlined,
    post: SolutionOutlined,
    dept: ApartmentOutlined,
    role: TeamOutlined,
    expression: CodeOutlined
  }
  return icons[type] || UserOutlined
}

const handleTypeChange = (type: string) => {
  currentType.value = type as any
  searchKeyword.value = ''
  if (!props.inline) {
    tempSelectedValues.value = selectedParticipants.value
      .filter(p => p.participantType === currentType.value)
      .map(p => p.participantValue!)
  } else {
    selectedValues.value = selectedParticipants.value
      .filter(p => p.participantType === currentType.value)
      .map(p => p.participantValue!)
  }
}

const updateValue = () => {
  const result: WfParticipantDTO[] = []

  if (currentType.value === 'expression' && expressionValue.value) {
    result.push({
      participantType: 'expression',
      participantValue: expressionValue.value,
      participantName: `表达式：${expressionValue.value}`
    })
  } else {
    const values = props.inline ? selectedValues.value : tempSelectedValues.value
    const options = mockOptions.value[currentType.value] || []
    let order = 1
    values.forEach(val => {
      const opt = options.find(o => o.value === val)
      if (opt) {
        result.push({
          participantType: currentType.value,
          participantValue: opt.value,
          participantName: opt.label,
          sortOrder: order++
        })
      }
    })
  }

  if (!props.multiple && result.length > 1) {
    result.splice(1)
  }

  emit('update:modelValue', result)
}

watch(selectedValues, () => {
  if (props.inline) {
    updateValue()
  }
}, { deep: true })

watch(expressionValue, () => {
  if (currentType.value === 'expression') {
    updateValue()
  }
})

const handleExpressionChange = () => {
  updateValue()
}

const handleRemove = (item: WfParticipantDTO) => {
  const newValue = selectedParticipants.value.filter(
    p => p.participantValue !== item.participantValue || p.participantType !== item.participantType
  )
  emit('update:modelValue', newValue)
}

const openPicker = () => {
  currentType.value = (props.types as any)[0] || 'user'
  tempSelectedValues.value = selectedParticipants.value
    .filter(p => p.participantType === currentType.value)
    .map(p => p.participantValue!)
  pickerVisible.value = true
}

const handlePickerOk = () => {
  updateValue()
  pickerVisible.value = false
}

watch(() => props.types, (val) => {
  if (val && val.length > 0 && !(val as any).includes(currentType.value)) {
    currentType.value = (val as any)[0]
  }
}, { immediate: true })

watch(() => props.modelValue, (val) => {
  if (val && val.length > 0) {
    const firstItem = val[0]
    if ((props.types as any).includes(firstItem.participantType)) {
      currentType.value = firstItem.participantType as any
    }
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="less">
.participant-selector {
  .selector-header {
    margin-bottom: 12px;
  }

  .selector-body {
    .selectable-list {
      max-height: 200px;
      overflow-y: auto;
      padding: 8px;
      border: 1px solid #e8e8e8;
      border-radius: 4px;

      .selectable-item {
        width: 100%;
        padding: 8px;
        border-radius: 4px;
        transition: background 0.2s;

        &:hover {
          background: #f5f5f5;
        }

        .item-label {
          margin-left: 8px;
        }
      }
    }

    .expression-tip {
      margin-top: 8px;
      font-size: 12px;
      color: #8c8c8c;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .selected-tags {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #e8e8e8;

    .selected-title {
      font-size: 12px;
      color: #666;
      margin-bottom: 8px;
    }

    :deep(.ant-tag) {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }

  .selector-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    min-height: 40px;

    &:hover {
      border-color: #1890ff;
    }

    .placeholder {
      color: #bfbfbf;
      flex: 1;
    }

    .trigger-icon {
      color: #bfbfbf;
      margin-left: auto;
    }

    .more-count {
      color: #8c8c8c;
      font-size: 12px;
    }

    :deep(.ant-tag) {
      margin-right: 4px;
    }
  }

  .modal-content {
    .modal-list {
      max-height: 300px;
      overflow-y: auto;
      padding: 8px;
      border: 1px solid #e8e8e8;
      border-radius: 4px;
    }
  }
}
</style>
