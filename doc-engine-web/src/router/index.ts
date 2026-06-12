import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/template'
  },
  {
    path: '/template',
    name: 'TemplateList',
    component: () => import('@/views/template/List.vue'),
    meta: { title: '公文模板管理' }
  },
  {
    path: '/template/create',
    name: 'TemplateCreate',
    component: () => import('@/views/template/Form.vue'),
    meta: { title: '创建公文模板' }
  },
  {
    path: '/template/edit/:id',
    name: 'TemplateEdit',
    component: () => import('@/views/template/Form.vue'),
    meta: { title: '编辑公文模板' }
  },
  {
    path: '/template/preview/:id',
    name: 'TemplatePreview',
    component: () => import('@/views/template/Preview.vue'),
    meta: { title: '模板预览' }
  },
  {
    path: '/template-header',
    name: 'TemplateHeaderList',
    component: () => import('@/views/template/HeaderList.vue'),
    meta: { title: '红头配置管理' }
  },
  {
    path: '/document',
    name: 'DocumentList',
    component: () => import('@/views/document/List.vue'),
    meta: { title: '公文管理' }
  },
  {
    path: '/document/create/:templateId',
    name: 'DocumentCreate',
    component: () => import('@/views/document/Form.vue'),
    meta: { title: '创建公文' }
  },
  {
    path: '/document/edit/:id',
    name: 'DocumentEdit',
    component: () => import('@/views/document/Form.vue'),
    meta: { title: '编辑公文' }
  },
  {
    path: '/seal',
    name: 'SealList',
    component: () => import('@/views/seal/List.vue'),
    meta: { title: '印章管理' }
  },
  {
    path: '/signature',
    name: 'SignatureList',
    component: () => import('@/views/signature/List.vue'),
    meta: { title: '签章记录' }
  },
  {
    path: '/signature/sign/:documentId?',
    name: 'SignatureSign',
    component: () => import('@/views/signature/Sign.vue'),
    meta: { title: '签章操作' }
  },
  {
    path: '/signature/verify/:signatureId',
    name: 'SignatureVerify',
    component: () => import('@/views/signature/Verify.vue'),
    meta: { title: '验章结果' }
  },
  {
    path: '/signature/detail/:id',
    name: 'SignatureDetail',
    component: () => import('@/views/signature/Detail.vue'),
    meta: { title: '签章详情' }
  },
  {
    path: '/signature/log',
    name: 'SignatureLogList',
    component: () => import('@/views/signature/LogList.vue'),
    meta: { title: '签章日志' }
  },
  {
    path: '/workflow',
    name: 'WorkflowList',
    component: () => import('@/views/workflow/List.vue'),
    meta: { title: '流程定义管理' }
  },
  {
    path: '/workflow/design',
    name: 'WorkflowDesignCreate',
    component: () => import('@/views/workflow/Design.vue'),
    meta: { title: '新建流程' }
  },
  {
    path: '/workflow/design/:id',
    name: 'WorkflowDesignEdit',
    component: () => import('@/views/workflow/Design.vue'),
    meta: { title: '编辑流程' }
  },
  {
    path: '/workflow/todo',
    name: 'WorkflowTodo',
    component: () => import('@/views/workflow/TodoList.vue'),
    meta: { title: '我的待办' }
  },
  {
    path: '/workflow/done',
    name: 'WorkflowDone',
    component: () => import('@/views/workflow/DoneList.vue'),
    meta: { title: '我的已办' }
  },
  {
    path: '/workflow/task/:id',
    name: 'WorkflowTaskDetail',
    component: () => import('@/views/workflow/TaskDetail.vue'),
    meta: { title: '审批详情' }
  },
  {
    path: '/archive',
    name: 'ArchiveList',
    component: () => import('@/views/archive/List.vue'),
    meta: { title: '档案管理' }
  },
  {
    path: '/archive/:id',
    name: 'ArchiveDetail',
    component: () => import('@/views/archive/Detail.vue'),
    meta: { title: '归档详情' }
  },
  {
    path: '/borrow',
    name: 'BorrowList',
    component: () => import('@/views/borrow/List.vue'),
    meta: { title: '借阅管理' }
  },
  {
    path: '/borrow/detail/:id',
    name: 'BorrowDetail',
    component: () => import('@/views/borrow/Detail.vue'),
    meta: { title: '借阅详情' }
  },
  {
    path: '/borrow/apply',
    name: 'BorrowApply',
    component: () => import('@/views/borrow/Apply.vue'),
    meta: { title: '借阅申请' }
  },
  {
    path: '/incoming',
    name: 'IncomingList',
    component: () => import('@/views/incoming/List.vue'),
    meta: { title: '收文登记' }
  },
  {
    path: '/incoming/register',
    name: 'IncomingRegister',
    component: () => import('@/views/incoming/Register.vue'),
    meta: { title: '收文登记' }
  },
  {
    path: '/incoming/:id',
    name: 'IncomingDetail',
    component: () => import('@/views/incoming/Detail.vue'),
    meta: { title: '收文详情' }
  },
  {
    path: '/handling',
    name: 'HandlingList',
    component: () => import('@/views/handling/List.vue'),
    meta: { title: '承办办理' }
  },
  {
    path: '/handling/detail/:id',
    name: 'HandlingDetail',
    component: () => import('@/views/handling/Detail.vue'),
    meta: { title: '处理详情' }
  },
  {
    path: '/supervision',
    name: 'SupervisionList',
    component: () => import('@/views/supervision/List.vue'),
    meta: { title: '督办管理' }
  },
  {
    path: '/supervision/:id',
    name: 'SupervisionDetail',
    component: () => import('@/views/supervision/Detail.vue'),
    meta: { title: '督办详情' }
  },
  {
    path: '/supervision/my/:id',
    name: 'MySupervisionDetail',
    component: () => import('@/views/supervision/Detail.vue'),
    meta: { title: '督办详情' }
  },
  {
    path: '/my-supervisions',
    name: 'MySupervisions',
    component: () => import('@/views/supervision/MyList.vue'),
    meta: { title: '我的督办' }
  },
  {
    path: '/security/permission',
    name: 'PermissionList',
    component: () => import('@/views/security/PermissionList.vue'),
    meta: { title: '权限配置' }
  },
  {
    path: '/security/audit-log',
    name: 'AuditLogList',
    component: () => import('@/views/security/AuditLogList.vue'),
    meta: { title: '审计日志' }
  },
  {
    path: '/security/integrity',
    name: 'IntegrityList',
    component: () => import('@/views/security/IntegrityList.vue'),
    meta: { title: '完整性验证' }
  },
  {
    path: '/org/unit',
    name: 'OrgUnitList',
    component: () => import('@/views/org/UnitList.vue'),
    meta: { title: '单位管理' }
  },
  {
    path: '/org/dept',
    name: 'OrgDeptList',
    component: () => import('@/views/org/DeptList.vue'),
    meta: { title: '部门管理' }
  },
  {
    path: '/org/post',
    name: 'OrgPostList',
    component: () => import('@/views/org/PostList.vue'),
    meta: { title: '岗位管理' }
  },
  {
    path: '/org/user',
    name: 'OrgUserList',
    component: () => import('@/views/org/UserList.vue'),
    meta: { title: '人员管理' }
  },
  {
    path: '/stat/dashboard',
    name: 'StatDashboard',
    component: () => import('@/views/stat/Dashboard.vue'),
    meta: { title: '数据统计' }
  },
  {
    path: '/stat/timeliness',
    name: 'StatTimeliness',
    component: () => import('@/views/stat/Timeliness.vue'),
    meta: { title: '办理时效统计' }
  },
  {
    path: '/stat/rejection',
    name: 'StatRejection',
    component: () => import('@/views/stat/Rejection.vue'),
    meta: { title: '退回热点分析' }
  },
  {
    path: '/stat/efficiency',
    name: 'StatEfficiency',
    component: () => import('@/views/stat/EfficiencyRanking.vue'),
    meta: { title: '效能排行与报表' }
  },
  {
    path: '/stat/supervision-suggestion',
    name: 'StatSupervisionSuggestion',
    component: () => import('@/views/stat/SupervisionSuggestion.vue'),
    meta: { title: '督办建议' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '政务公文引擎管理系统'
  next()
})

export default router
