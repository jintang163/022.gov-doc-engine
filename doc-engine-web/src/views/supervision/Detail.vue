<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">督办详情</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <a-button
          type="primary"
          @click="handlePush"
          v-if="detail && (detail.status === 'generated' || detail.status === 'notified')"
        >
          <template #icon><SendOutlined /></template>
          推送分管领导
        </a-button>
        <a-button
          type="primary"
          @click="handleComplete"
          v-if="detail && detail.status !== 'completed' && detail.status !== 'closed'"
        >
          <template #icon><CheckCircleOutlined /></template>
          办结
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="16">
        <a-card title="公文信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered size="small" v-if="detail?.incoming">
            <a-descriptions-item label="公文标题" :span="2">
              {{ detail.incoming.docTitle }}
            </a-descriptions-item>
            <a-descriptions-item label="来文字号">
              {{ detail.incoming.sourceDocNumber }}
            </a-descriptions-item>
            <a-descriptions-item label="来源单位">
              {{ detail.incoming.sourceUnit }}
            </a-descriptions-item>
            <a-descriptions-item label="密级">
              {{ detail.incoming.securityLevel }}
            </a-descriptions-item>
            <a-descriptions-item label="缓急程度">
              {{ detail.incoming.urgencyLevel }}
            </a-descriptions-item>
            <a-descriptions-item label="收文日期">
              {{ detail.incoming.receivedDate }}
            </a-descriptions-item>
            <a-descriptions-item label="收文状态">
              <a-tag>{{ detail.incoming.statusName }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 5 }" />
        </a-card>

        <a-card title="催办记录" :bordered="false">
          <a-timeline v-if="urgeLogs.length > 0">
            <a-timeline-item
              v-for="log in urgeLogs"
              :key="log.id"
              :color="log.status === 'acknowledged' ? 'green' : 'blue'"
            >
              <div class="urge-item">
                <div class="urge-header">
                  <span class="urge-type">
                    {{ log.urgeType === 'system' ? '系统催办' : '人工催办' }}
                  </span>
                  <a-tag :color="log.status === 'acknowledged' ? 'success' : 'processing'">
                    {{ log.statusName }}
                  </a-tag>
                  <span class="urge-time">{{ log.createTime }}</span>
                </div>
                <div class="urge-content">{{ log.urgeContent }}</div>
                <div class="urge-footer">
                  被催办人：{{ log.urgedUserName }}（{{ log.urgedDeptName }}）
                  <template v-if="log.acknowledgeTime">
                    ，确认时间：{{ log.acknowledgeTime }}
                  </template>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无催办记录" />
        </a-card>
      </a-col>

      <a-col :span="8">
        <a-card title="督办信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered size="small" v-if="detail">
            <a-descriptions-item label="督办单编号">
              {{ detail.supervisionNo }}
            </a-descriptions-item>
            <a-descriptions-item label="督办类型">
              <a-tag :color="getTypeColor(detail.supervisionType)">
                {{ detail.supervisionTypeName }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="责任人">
              {{ detail.responsibleUserName }}
            </a-descriptions-item>
            <a-descriptions-item label="责任部门">
              {{ detail.responsibleDeptName }}
            </a-descriptions-item>
            <a-descriptions-item label="分管领导">
              {{ detail.leaderName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="办理期限">
              {{ detail.deadline || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="超时天数">
              <span :style="{ color: detail.overdueDays > 0 ? '#ff4d4f' : '#52c41a' }">
                {{ detail.overdueDays }} 天
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="催办次数">
              {{ detail.urgeCount }} 次
            </a-descriptions-item>
            <a-descriptions-item label="督办状态">
              <a-tag :color="getStatusColor(detail.status)">
                {{ detail.statusName }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="推送状态">
              <a-tag :color="getPushStatusColor(detail.pushStatus)">
                {{ detail.pushStatusName }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="推送时间">
              {{ detail.pushTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="完成时间">
              {{ detail.completeTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="督办内容" v-if="detail.supervisionContent">
              {{ detail.supervisionContent }}
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 8 }" />
        </a-card>

        <a-card
          title="操作区"
          :bordered="false"
          v-if="detail && detail.status !== 'completed' && detail.status !== 'closed'"
        >
          <a-form layout="vertical">
            <a-form-item label="选择状态">
              <a-select v-model:value="selectedStatus" style="width: 100%">
                <a-select-option value="processing">处理中</a-select-option>
                <a-select-option value="completed">已完成</a-select-option>
                <a-select-option value="closed">已关闭</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleUpdateStatus" :loading="updating">
                更新状态
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  SendOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import {
  getSupervisionDetail,
  pushToLeader,
  updateSupervisionStatus,
  completeSupervision
} from '@/api/supervision'
import type { DocSupervisionVO, DocUrgeLogVO } from '@/types/supervision'
import {
  supervisionTypeOptions,
  supervisionStatusOptions,
  pushStatusOptions
} from '@/types/supervision'

const route = useRoute()
const router = useRouter()

const detail = ref<DocSupervisionVO | null>(null)
const urgeLogs = ref<DocUrgeLogVO[]>([])
const updating = ref(false)
const selectedStatus = ref('processing')

const getTypeColor = (type: string) => {
  const option = supervisionTypeOptions.find(o => o.value === type)
  return option?.color || 'default'
}

const getStatusColor = (status: string) => {
  const option = supervisionStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const getPushStatusColor = (status: string) => {
  const option = pushStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }

  try {
    const res = await getSupervisionDetail(id)
    detail.value = res
    urgeLogs.value = res.urgeLogs || []
  } catch (error) {
    message.error('加载督办详情失败')
  }
}

const handlePush = () => {
  Modal.confirm({
    title: '确认推送',
    content: '确定将该督办单推送至分管领导移动端吗？',
    okText: '确认推送',
    cancelText: '取消',
    onOk: async () => {
      try {
        const id = Number(route.params.id)
        await pushToLeader(id)
        message.success('推送分管领导成功')
        loadDetail()
      } catch (error) {
        message.error('推送失败')
      }
    }
  })
}

const handleComplete = () => {
  Modal.confirm({
    title: '确认办结',
    content: '确定将该督办单标记为已完成吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const id = Number(route.params.id)
        await completeSupervision(id)
        message.success('督办已办结')
        loadDetail()
      } catch (error) {
        message.error('操作失败')
      }
    }
  })
}

const handleUpdateStatus = async () => {
  if (!selectedStatus.value) {
    message.warning('请选择状态')
    return
  }
  updating.value = true
  try {
    const id = Number(route.params.id)
    await updateSupervisionStatus(id, selectedStatus.value)
    message.success('状态更新成功')
    loadDetail()
  } catch (error) {
    message.error('状态更新失败')
  } finally {
    updating.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="less" scoped>
.page-container {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
  }
}

.urge-item {
  padding: 8px 0;

  .urge-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .urge-type {
      font-weight: 500;
      color: #1890ff;
    }

    .urge-time {
      margin-left: auto;
      color: #8c8c8c;
      font-size: 12px;
    }
  }

  .urge-content {
    color: #595959;
    margin-bottom: 8px;
  }

  .urge-footer {
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
