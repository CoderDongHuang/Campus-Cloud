<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
const route = useRoute(); const router = useRouter()
const spu = ref<any>({}); const skus = ref<any[]>([])
onMounted(async () => {
  try { const res = await axios.get(`/api/v1/product/spu/${route.params.id}`); spu.value = res.data.data?.spu || res.data.data; skus.value = res.data.data?.skus || [] } catch {}
})
</script>
<template>
  <div class="page"><van-nav-bar title="服务详情" left-arrow @click-left="() => $router.back()" />
    <div class="hero"><div class="hero-icon">🧹</div><div class="hero-name">{{spu.name}}</div><div class="hero-price">¥{{spu.actualAmount || 59}}</div></div>
    <div class="section"><div class="s-title">服务说明</div><p class="desc">{{spu.description || '专业服务，品质保障'}}</p></div>
    <div class="bottom-bar"><div class="total"><span>合计</span><b>¥{{spu.actualAmount || 59}}</b></div><van-button round type="danger" block @click="() => router.push('/student/order/confirm?spuId='+spu.id)">立即下单</van-button></div>
  </div>
</template>
<style scoped>.page{padding-bottom:80px}.hero{text-align:center;padding:32px 0;background:#fff;margin-bottom:12px}.hero-icon{font-size:64px}.hero-name{font-size:20px;font-weight:700;margin:8px 0}.hero-price{font-size:28px;font-weight:700;color:#FF6B6B}.section{background:#fff;padding:16px;margin-bottom:12px}.s-title{font-size:15px;font-weight:600;margin-bottom:8px}.desc{font-size:14px;color:#666;line-height:1.8}.bottom-bar{position:fixed;bottom:0;left:0;right:0;background:#fff;padding:12px 16px;display:flex;align-items:center;gap:12px;box-shadow:0 -2px 8px rgba(0,0,0,0.06)}.total b{font-size:20px;color:#FF6B6B;margin-left:8px}</style>
