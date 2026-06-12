<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">收文详情</h2>
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <a-col :span="14">
        <a-card title="收文信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered size="small" v-if="detail">
            <a-descriptions-item label="收文编号">{{ detail.incomingNo }}</a-descriptions-item>
            <a-descriptions-item label="来源名称">{{ detail.sourceName }}</a-descriptions-item>
            <a-descriptions-item label="来源单位">{{ detail.sourceUnit }}</a-descriptions-item>
            <a-descriptions-item label="来文字号">{{ detail.sourceDocNumber }}</a-descriptions-item>
            <a-descriptions-item label="公文标题" :span="2">{{ detail.docTitle }}</a-descriptions-item>
            <a-descriptions-item label="文种分类">{{ detail.docType }}</a-descriptions-item>
            <a-descriptions-item label="密级">{{ detail.securityLevel }}</a-descriptions-item>
            <a-descriptions-item label="缓急程度">{{ detail.urgencyLevel }}</a-descriptions-item>
            <a-descriptions-item label="收到日期">{{ detail.receivedDate }}</a-descriptions-item>
            <a-descriptions-item label="收文方式">{{ detail.receivedMethodName }}</a-descriptions-item>
            <a-descriptions-item label="份数">{{ detail.copies }}</a-descriptions-item>
            <a-descriptions-item label="页数">{{ detail.pages }}</a-descriptions-item>
            <a-descriptions-item label="关键词" :span="2">{{ detail.keyword }}</a-descriptions-item>
            <a-descriptions-item label="登记人">{{ detail.registrantName }}</a-descriptions-item>
            <a-descriptions-item label="登记部门">{{ detail.registrantDeptName }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(detail.status)">{{ detail.statusName }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
          <a-skeleton v-else active :paragraph="{ rows: 8 }" />
        </a-card>

        <a-card title="公文正文" :bordered="false">
          <div v-if="detail?.docContent" class="doc-content" v-html="detail.docContent" />
          <a-empty v-else description="暂无正文内容" />
        </a-card>
      </a-col>

      <a-col :span="10">
        <a-card title="拟办意见" :bordered="false" style="margin-bottom: 16px;">
          <a-form layout="vertical" v-if="detail?.status === 'registered'">
            <a-form-item label="拟办意见">
              <a-textarea
                v-model:value="opinionForm.opinion"
                :rows="4"
                placeholder="请输入拟办意见"
              />
            </a-form-item>
            <a-form-item label="承办部门">
              <a-input v-model:value="opinionForm.targetDeptName" placeholder="请输入承办部门">
                <template #addonAfter>
                  <a-button type="link" @click="loadRecommendForOpinion" :loading="recommendLoading">
                    <template #icon><BulbOutlined /></template>
                    智能推荐
                  </a-button>
                </template>
              </a-input>
            </a-form-item>
            <div v-if="opinionRecommendList.length > 0" class="recommend-panel">
              <div class="recommend-title">
                <BulbOutlined />
                <span>智能推荐部门（点击一键采纳）</span>
              </div>
              <div class="recommend-list">
                <div
                  v-for="item in opinionRecommendList"
                  :key="item.deptId"
                  class="recommend-item"
                  @click="adoptRecommendForOpinion(item)"
                >
                  <div class="recommend-item-header">
                    <span class="recommend-dept-name">{{ item.deptName }}</span>
                    <a-tag :color="getScoreColor(item.matchScore)" size="small">
                      {{ item.matchScoreText }} {{ (item.matchScore * 100).toFixed(0) }}%
                    </a-tag>
                  </div>
                  <div class="recommend-item-info">
                    <span>历史办理 {{ item.matchCount }} 次</span>
                    <span v-if="item.lastHandleTime"> · 最近：{{ item.lastHandleTime }}</span>
                  </div>
                  <div class="recommend-item-reason">{{ item.matchReason }}</div>
                </div>
              </div>
            </div>
            <a-form-item label="承办人">
              <a-input v-model:value="opinionForm.targetUserName" placeholder="请输入承办人" />
            </a-form-item>
            <a-form-item label="办理期限">
              <a-date-picker
                v-model:value="opinionForm.deadline"
                style="width: 100%"
                placeholder="请选择办理期限"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleDraftOpinion" :loading="submittingOpinion">
                提交拟办
              </a-button>
            </a-form-item>
          </a-form>
          <a-timeline v-else-if="handlingRecords.length > 0">
            <a-timeline-item
              v-for="record in handlingRecords"
              :key="record.id"
              :color="getTimelineColor(record.status)"
            >
              <div class="timeline-item">
                <div class="timeline-header">
                  <span class="timeline-type">{{ record.handlingTypeName }}</span>
                  <a-tag :color="getHandlingStatusColor(record.status)" size="small">
                    {{ record.statusName }}
                  </a-tag>
                </div>
                <div class="timeline-opinion" v-if="record.opinion">{{ record.opinion }}</div>
                <div class="timeline-meta">
                  <span v-if="record.handlerName">{{ record.handlerName }}</span>
                  <span v-if="record.handlerDeptName">{{ record.handlerDeptName }}</span>
                  <span v-if="record.targetDeptName">→ {{ record.targetDeptName }}</span>
                  <span v-if="record.targetUserName">{{ record.targetUserName }}</span>
                  <span v-if="record.deadline">期限: {{ record.deadline }}</span>
                </div>
                <div class="timeline-time" v-if="record.handlingTime">{{ record.handlingTime }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无处理记录" />
        </a-card>

        <a-card title="转承办" :bordered="false" v-if="detail?.status === 'proposed'">
          <a-form layout="vertical">
            <a-form-item label="承办部门">
              <a-input v-model:value="assignForm.targetDeptName" placeholder="请输入承办部门">
                <template #addonAfter>
                  <a-button type="link" @click="loadRecommendForAssign" :loading="recommendLoading">
                    <template #icon><BulbOutlined /></template>
                    智能推荐
                  </a-button>
                </template>
              </a-input>
            </a-form-item>
            <div v-if="assignRecommendList.length > 0" class="recommend-panel">
              <div class="recommend-title">
                <BulbOutlined />
                <span>智能推荐部门（点击一键采纳）</span>
              </div>
              <div class="recommend-list">
                <div
                  v-for="item in assignRecommendList"
                  :key="item.deptId"
                  class="recommend-item"
                  @click="adoptRecommendForAssign(item)"
                >
                  <div class="recommend-item-header">
                    <span class="recommend-dept-name">{{ item.deptName }}</span>
                    <a-tag :color="getScoreColor(item.matchScore)" size="small">
                      {{ item.matchScoreText }} {{ (item.matchScore * 100).toFixed(0) }}%
                    </a-tag>
                  </div>
                  <div class="recommend-item-info">
                    <span>历史办理 {{ item.matchCount }} 次</span>
                    <span v-if="item.lastHandleTime"> · 最近：{{ item.lastHandleTime }}</span>
                  </div>
                  <div class="recommend-item-reason">{{ item.matchReason }}</div>
                </div>
              </div>
            </div>
            <a-form-item label="承办人">
              <a-input v-model:value="assignForm.targetUserName" placeholder="请输入承办人" />
            </a-form-item>
            <a-form-item label="办理期限">
              <a-date-picker
                v-model:value="assignForm.deadline"
                style="width: 100%"
                placeholder="请选择办理期限"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleAssignHandling" :loading="submittingAssign">
                提交转承办
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, BulbOutlined } from '@ant-design/icons-vue'
import { getIncomingDetail, draftOpinion, assignHandling, getHandlingPage, recommendDepts } from '@/api/incoming'
import type { DocIncomingVO, DocHandlingDTO, DocHandlingVO, DeptRecommendVO } from '@/types/incoming'
import { incomingStatusOptions } from '@/types/incoming'

const route = useRoute()
const router = useRouter()

const detail = ref<DocIncomingVO | null>(null)
const handlingRecords = ref<DocHandlingVO[]>([])
const submittingOpinion = ref(false)
const submittingAssign = ref(false)
const recommendLoading = ref(false)
const opinionRecommendList = ref<DeptRecommendVO[]>([])
const assignRecommendList = ref<DeptRecommendVO[]>([])

const opinionForm = reactive<DocHandlingDTO>({
  incomingId: 0,
  handlingType: 'draft_opinion',
  opinion: undefined,
  targetDeptName: undefined,
  targetUserName: undefined,
  deadline: undefined
})

const assignForm = reactive<DocHandlingDTO>({
  incomingId: 0,
  handlingType: 'assign',
  targetDeptName: undefined,
  targetUserName: undefined,
  deadline: undefined
})

const getStatusColor = (status: string) => {
  const option = incomingStatusOptions.find(o => o.value === status)
  return option?.color || 'default'
}

const getTimelineColor = (status: string) => {
  if (status === 'completed') return 'green'
  if (status === 'processing') return 'blue'
  return 'gray'
}

const getHandlingStatusColor = (status: string) => {
  if (status === 'completed') return 'success'
  if (status === 'processing') return 'processing'
  return 'warning'
}

const getScoreColor = (score: number) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'blue'
  if (score >= 0.4) return 'processing'
  return 'default'
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    return
  }

  try {
    detail.value = await getIncomingDetail(id)
    opinionForm.incomingId = id
    assignForm.incomingId = id
  } catch (error) {
    message.error('加载收文详情失败')
  }
}

const loadHandlingRecords = async () => {
  const id = Number(route.params.id)
  if (!id) return

  try {
    const result = await getHandlingPage({ incomingId: id, pageNum: 1, pageSize: 100 })
    handlingRecords.value = result.list || []
  } catch (error) {
    message.error('加载处理记录失败')
  }
}

const loadRecommendForOpinion = async () => {
  if (!detail.value) return
  recommendLoading.value = true
  opinionRecommendList.value = []
  try {
    const list = await recommendDepts({
      docTitle: detail.value.docTitle,
      docType: detail.value.docType,
      keyword: detail.value.keyword
    })
    opinionRecommendList.value = list
    if (list.length === 0) {
      message.info('暂无匹配的历史推荐部门')
    }
  } catch (error) {
    message.error('加载推荐部门失败')
  } finally {
    recommendLoading.value = false
  }
}

const loadRecommendForAssign = async () => {
  if (!detail.value) return
  recommendLoading.value = true
  assignRecommendList.value = []
  try {
    const list = await recommendDepts({
      docTitle: detail.value.docTitle,
      docType: detail.value.docType,
      keyword: detail.value.keyword
    })
    assignRecommendList.value = list
    if (list.length === 0) {
      message.info('暂无匹配的历史推荐部门')
    }
  } catch (error) {
    message.error('加载推荐部门失败')
  } finally {
    recommendLoading.value = false
  }
}

const adoptRecommendForOpinion = (item: DeptRecommendVO) => {
  opinionForm.targetDeptName = item.deptName
  opinionForm.targetDeptId = item.deptId
  message.success(`已采纳：${item.deptName}`)
}

const adoptRecommendForAssign = (item: DeptRecommendVO) => {
  assignForm.targetDeptName = item.deptName
  assignForm.targetDeptId = item.deptId
  message.success(`已采纳：${item.deptName}`)
}

const handleDraftOpinion = async () => {
  if (!opinionForm.opinion) {
    message.warning('请输入拟办意见')
    return
  }
  submittingOpinion.value = true
  try {
    await draftOpinion(opinionForm)
    message.success('拟办意见提交成功')
    await loadDetail()
    await loadHandlingRecords()
  } catch (error) {
    message.error('提交拟办意见失败')
  } finally {
    submittingOpinion.value = false
  }
}

const handleAssignHandling = async () => {
  if (!assignForm.targetDeptName) {
    message.warning('请输入承办部门')
    return
  }
  submittingAssign.value = true
  try {
    await assignHandling(assignForm)
    message.success('转承办提交成功')
    await loadDetail()
    await loadHandlingRecords()
  } catch (error) {
    message.error('提交转承办失败')
  } finally {
    submittingAssign.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
  loadHandlingRecords()
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

.doc-content {
  line-height: 1.8;
  color: #333;
}

.timeline-item {
  .timeline-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .timeline-type {
      font-weight: 500;
    }
  }

  .timeline-opinion {
    color: #595959;
    margin-bottom: 4px;
  }

  .timeline-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    color: #8c8c8c;
    font-size: 13px;
  }

  .timeline-time {
    color: #8c8c8c;
    font-size: 12px;
    margin-top: 4px;
  }
}

.recommend-panel {
  margin-bottom: 16px;
  padding: 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;

  .recommend-title {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 10px;
    color: #389e0d;
    font-weight: 500;
    font-size: 13px;
  }

  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .recommend-item {
    padding: 10px 12px;
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: #52c41a;
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.15);
    }

    .recommend-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .recommend-dept-name {
        font-weight: 600;
        color: #262626;
        font-size: 14px;
      }
    }

    .recommend-item-info {
      color: #8c8c8c;
      font-size: 12px;
      margin-bottom: 4px;
    }

    .recommend-item-reason {
      color: #595959;
      font-size: 12px;
      line-height: 1.5;
    }
  }
}
</style>
