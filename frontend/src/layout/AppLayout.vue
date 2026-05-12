<template>
  <el-container class="app-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
      <div class="logo" @click="$router.push('/')">
        <span v-if="!isCollapse">外综服平台</span>
        <span v-else>外</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <!-- 用户管理 (ADMIN/GUOHE) -->
        <el-sub-menu index="user-mgmt" v-if="isAdmin || isGuohe">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/users">用户列表</el-menu-item>
          <el-menu-item index="/user-applies">用户审批</el-menu-item>
        </el-sub-menu>

        <!-- 租户管理 (ADMIN/GUOHE) -->
        <el-menu-item index="/tenants" v-if="isAdmin || isGuohe">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>租户管理</template>
        </el-menu-item>

        <!-- 配置管理 (ADMIN only) -->
        <el-menu-item index="/config" v-if="isAdmin">
          <el-icon><Setting /></el-icon>
          <template #title>配置管理</template>
        </el-menu-item>

        <!-- 模板管理 (ADMIN only) -->
        <el-menu-item index="/templates" v-if="isAdmin">
          <el-icon><Document /></el-icon>
          <template #title>模板管理</template>
        </el-menu-item>

        <!-- 合同管理 -->
        <el-sub-menu index="contract-mgmt">
          <template #title>
            <el-icon><Notebook /></el-icon>
            <span>合同管理</span>
          </template>
          <el-menu-item index="/contracts">合同列表</el-menu-item>
          <el-menu-item index="/contracts/expiring">临期预警</el-menu-item>
        </el-sub-menu>

        <!-- 货物管理 -->
        <el-menu-item index="/goods">
          <el-icon><Box /></el-icon>
          <template #title>货物管理</template>
        </el-menu-item>

        <!-- 订单管理 -->
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>

        <!-- 进口台账 -->
        <el-menu-item index="/ledger">
          <el-icon><Notebook /></el-icon>
          <template #title>进口台账</template>
        </el-menu-item>

        <!-- 报关单管理 -->
        <el-menu-item index="/customs">
          <el-icon><Tickets /></el-icon>
          <template #title>报关单管理</template>
        </el-menu-item>

        <!-- 文件管理 -->
        <el-menu-item index="/files">
          <el-icon><Folder /></el-icon>
          <template #title>文件管理</template>
        </el-menu-item>

        <!-- 供应商客户管理 -->
        <el-menu-item index="/partners">
          <el-icon><Connection /></el-icon>
          <template #title>供应商与客户</template>
        </el-menu-item>

        <!-- 服务企业档案 -->
        <el-menu-item index="/enterprises">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>服务企业档案</template>
        </el-menu-item>

        <!-- 综合服务 -->
        <el-sub-menu index="service-mgmt">
          <template #title>
            <el-icon><Operation /></el-icon>
            <span>综合服务</span>
          </template>
          <el-menu-item index="/logistics">物流服务</el-menu-item>
          <el-menu-item index="/tax-refunds">退税业务</el-menu-item>
          <el-menu-item index="/settlements">结算收汇</el-menu-item>
          <el-menu-item index="/insurances">信保服务</el-menu-item>
          <el-menu-item index="/financings">融资协助</el-menu-item>
          <el-menu-item index="/tax">退税政策</el-menu-item>
        </el-sub-menu>

        <!-- 认定驾驶舱 (ADMIN/GUOHE) -->
        <el-menu-item index="/stats" v-if="isAdmin || isGuohe">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>认定驾驶舱</template>
        </el-menu-item>

        <!-- 操作日志 (ADMIN/GUOHE) -->
        <el-menu-item index="/logs" v-if="isAdmin || isGuohe">
          <el-icon><Document /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="tenant-name" v-if="userStore.userInfo?.tenantName">
            {{ userStore.userInfo.tenantName }}
          </span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.isAdmin)
const isGuohe = computed(() => userStore.isGuohe)

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
}
.app-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  background-color: #263445;
}
.app-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.tenant-name {
  color: #909399;
  font-size: 14px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #333;
}
.app-main {
  background: #f0f2f5;
  min-height: 0;
}
.el-menu {
  border-right: none;
}
</style>
