<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="layout-header">
      <div class="header-content">
        <span class="logo">📄 政务公文引擎管理系统</span>
        <a-menu
          theme="dark"
          mode="horizontal"
          :selected-keys="selectedKeys"
          @click="handleMenuClick"
          class="header-menu"
        >
          <a-menu-item key="/template">公文模板管理</a-menu-item>
          <a-menu-item key="/document">公文管理</a-menu-item>
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

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
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
