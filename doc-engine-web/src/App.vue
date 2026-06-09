<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="layout-header">
      <div class="header-content">
        <span class="logo">📄 政务公文引擎管理系统</span>
        <a-menu
          theme="dark"
          mode="horizontal"
          :selected-keys="selectedKeys"
          :open-keys="openKeys"
          @update:open-keys="openKeys = $event"
          @click="handleMenuClick"
          class="header-menu"
        >
          <a-menu-item key="/template">公文模板管理</a-menu-item>
          <a-menu-item key="/template-header">红头配置管理</a-menu-item>
          <a-menu-item key="/document">公文管理</a-menu-item>
          <a-sub-menu key="signature">
            <template #title>🔏 电子签章</template>
            <a-menu-item key="/seal">印章管理</a-menu-item>
            <a-menu-item key="/signature">签章记录</a-menu-item>
            <a-menu-item key="/signature/sign">签章操作</a-menu-item>
            <a-menu-item key="/signature/log">签章日志</a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="archive">
            <template #title>🗄️ 归档与查询</template>
            <a-menu-item key="/archive">档案管理</a-menu-item>
            <a-menu-item key="/borrow">借阅管理</a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="incoming">
            <template #title>📥 收文办理</template>
            <a-menu-item key="/incoming">收文登记</a-menu-item>
            <a-menu-item key="/handling">承办办理</a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="security">
            <template #title>🔒 权限与安全</template>
            <a-menu-item key="/security/permission">权限配置</a-menu-item>
            <a-menu-item key="/security/audit-log">审计日志</a-menu-item>
            <a-menu-item key="/security/integrity">完整性验证</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </div>
    </a-layout-header>
    <a-layout-content class="layout-content">
      <router-view />
    </a-layout-content>
    <a-layout-footer class="layout-footer">
      政务协同办公公文引擎 ©2024 Created by Gov Doc Engine
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const selectedKeys = ref<string[]>(['/template'])
const openKeys = ref<string[]>([])

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
    if (path.startsWith('/seal') || path.startsWith('/signature')) {
      openKeys.value = ['signature']
    } else if (path.startsWith('/archive') || path.startsWith('/borrow')) {
      openKeys.value = ['archive']
    } else if (path.startsWith('/incoming') || path.startsWith('/handling')) {
      openKeys.value = ['incoming']
    } else if (path.startsWith('/security')) {
      openKeys.value = ['security']
    }
  },
  { immediate: true }
)

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<style lang="less" scoped>
.layout-header {
  padding: 0;
  height: 64px;
  line-height: 64px;
  background: #001529;

  .header-content {
    display: flex;
    align-items: center;
    padding: 0 24px;

    .logo {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      margin-right: 40px;
    }

    .header-menu {
      flex: 1;
      min-width: 0;
      border-bottom: none;
    }
  }
}

.layout-content {
  padding: 24px;
  background: #f0f2f5;
}

.layout-footer {
  text-align: center;
  color: #666;
}
</style>
