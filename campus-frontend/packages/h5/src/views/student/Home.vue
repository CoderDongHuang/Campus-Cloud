<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const activeTab = ref(0)
const keyword = ref('')
const services = ref<any[]>([])
const categories = ref<any[]>([])
const hotKeywords = ref<string[]>([])

onMounted(async () => {
  try {
    const [s, c, h] = await Promise.all([
      axios.get('/api/v1/product/spu/list'),
      axios.get('/api/v1/product/categories/tree'),
      axios.get('/api/v1/search/hot-keywords'),
    ])
    services.value = s.data.data || []
    categories.value = c.data.data || []
    hotKeywords.value = h.data.data || []
  } catch {}
})

function goDetail(spu: any) { router.push(`/student/service/${spu.id}`) }
function searchTag(word: string) { keyword.value = word }
</script>

<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-box" @click="router.push('/student/service/0')">
        <van-icon name="search" size="18" color="#999" />
        <span class="placeholder">搜点什么服务...</span>
      </div>
    </div>

    <!-- 热搜词 -->
    <div class="hot-tags" v-if="hotKeywords.length">
      <span v-for="w in hotKeywords.slice(0,6)" :key="w" class="tag" @click="searchTag(w)">{{ w }}</span>
    </div>

    <!-- 分类图标 -->
    <div class="cat-row">
      <div v-for="cat in categories.slice(0,8)" :key="cat.id" class="cat-item" @click="$router.push(`/student/service/0?cat=${cat.id}`)">
        <div class="cat-icon">{{ cat.icon || '📂' }}</div>
        <div class="cat-label">{{ cat.name }}</div>
      </div>
    </div>

    <!-- 限时抢券 Banner -->
    <div class="coupon-banner" @click="$router.push('/student/coupon')">
      <div class="banner-left">
        <div class="banner-tag">🔥 限时抢</div>
        <div class="banner-title">满50减10 优惠券</div>
        <div class="banner-sub">仅剩 32 张 · 速抢</div>
      </div>
      <div class="banner-right"><van-button round type="danger" size="small">立即抢</van-button></div>
    </div>

    <!-- 推荐服务 -->
    <div class="section-title">推荐服务</div>
    <div class="service-list">
      <div v-for="spu in services" :key="spu.id" class="service-card" @click="goDetail(spu)">
        <div class="card-icon">{{ spu.categoryId === 1 ? '🧹' : spu.categoryId === 2 ? '🔧' : '🧼' }}</div>
        <div class="card-info">
          <div class="card-name">{{ spu.name }}</div>
          <div class="card-desc">{{ spu.description || '品质服务，值得信赖' }}</div>
          <div class="card-bottom">
            <span class="card-price">¥{{ spu.actualAmount || 59 }}</span>
            <span class="card-sales">已售 {{ spu.salesCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 TabBar -->
    <van-tabbar v-model="activeTab" :fixed="true" :border="true" active-color="#FF9800">
      <van-tabbar-item icon="home-o" to="/student">首页</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/student/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/student/im/1">消息</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/student/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.home { padding: 0 16px 60px; background: #f7f8fa; min-height: 100vh; }
.search-bar { padding: 12px 0; }
.search-box { display: flex; align-items: center; gap: 8px; background: #fff; border-radius: 20px; padding: 10px 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.placeholder { color: #ccc; font-size: 14px; }
.hot-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.tag { padding: 4px 12px; background: #fff; border-radius: 12px; font-size: 12px; color: #FF9800; cursor: pointer; }
.cat-row { display: flex; gap: 0; justify-content: space-between; margin-bottom: 16px; }
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.cat-icon { font-size: 28px; width: 52px; height: 52px; display: flex; align-items: center; justify-content: center; background: #fff; border-radius: 16px; }
.cat-label { font-size: 11px; color: #666; }
.coupon-banner { display: flex; align-items: center; justify-content: space-between; padding: 16px; background: linear-gradient(135deg, #FFF3E0, #FFE0B2); border-radius: 12px; margin-bottom: 20px; }
.banner-tag { font-size: 12px; color: #E65100; font-weight: 600; }
.banner-title { font-size: 16px; font-weight: 700; color: #333; margin: 4px 0; }
.banner-sub { font-size: 12px; color: #999; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.service-list { display: flex; flex-direction: column; gap: 12px; }
.service-card { display: flex; gap: 12px; background: #fff; border-radius: 12px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); cursor: pointer; }
.card-icon { font-size: 36px; }
.card-info { flex: 1; }
.card-name { font-size: 15px; font-weight: 600; color: #333; }
.card-desc { font-size: 12px; color: #999; margin: 4px 0; }
.card-bottom { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 18px; font-weight: 700; color: #FF6B6B; }
.card-sales { font-size: 12px; color: #ccc; }
</style>
