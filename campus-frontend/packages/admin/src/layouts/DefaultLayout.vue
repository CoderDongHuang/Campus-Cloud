<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '数据大盘', icon: 'DataAnalysis' },
  { path: '/product', title: '商品管理', icon: 'Goods' },
  { path: '/order', title: '订单管理', icon: 'Document' },
  { path: '/coupon', title: '优惠券管理', icon: 'Ticket' },
  { path: '/user', title: '用户管理', icon: 'User' },
  { path: '/tenant', title: '租户管理', icon: 'OfficeBuilding' },
  { path: '/settlement', title: '结算管理', icon: 'Money' },
  { path: '/review', title: '评价管理', icon: 'Star' },
  { path: '/notify', title: '通知管理', icon: 'Bell' },
]

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span class="logo-mark">
          <span class="lg-a">X</span>
          <span class="lg-b">C</span>
          <span class="lg-c">Y</span>
        </span>
        <span v-if="!isCollapse" class="logo-text">校享云</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        background-color="#0F172A"
        text-color="#94A3B8"
        active-text-color="#4F6EF7"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
          <Expand v-if="isCollapse" /><Fold v-else />
        </el-icon>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div style="flex:1" />
        <el-dropdown>
          <span class="user-info">👤 管理员</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.aside { background: #0F172A; overflow: hidden; }
.logo { height: 56px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; font-size: 20px; font-weight: 700; border-bottom: 1px solid #1E293B; }
.logo-mark { width:36px; height:36px; background:#fff; border-radius:8px; position:relative; overflow:hidden; flex-shrink:0; }
.lg-a { position:absolute; top:-4px; left:2px; font-size:20px; font-weight:800; color:#1a1a1a; font-family:'SF Mono','Menlo',monospace; line-height:1 }
.lg-b { position:absolute; top:-2px; right:-2px; font-size:14px; font-weight:700; color:rgba(0,0,0,.35); font-family:'SF Mono','Menlo',monospace; line-height:1 }
.lg-c { position:absolute; bottom:-2px; right:2px; font-size:16px; font-weight:800; color:rgba(0,0,0,.6); font-family:'SF Mono','Menlo',monospace; line-height:1 }
.logo-text { letter-spacing: 2px; white-space: nowrap; }
.header { display: flex; align-items: center; gap: 12px; background: #fff; border-bottom: 1px solid #E2E8F0; padding: 0 20px; }
.collapse-btn { cursor: pointer; }
.user-info { cursor: pointer; color: #4F6EF7; }
.el-menu { border-right: none; }
</style>
