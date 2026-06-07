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
