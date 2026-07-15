<script setup lang="ts">
import { ref } from 'vue'; import { useRouter, useRoute } from 'vue-router'; import axios from 'axios'; import { showSuccessToast } from 'vant'
const router = useRouter(); const route = useRoute()
const spuId = ref(route.query.spuId || 1)
async function submit() {
  try { await axios.post('/api/v1/order/orders', { spuId: Number(spuId.value), skuId: 1, addressId: 1, originalAmount: 99, actualAmount: 79, appointmentTime: new Date().toISOString() }); showSuccessToast('下单成功'); router.push('/student/orders') } catch {}
}
</script>
<template>
  <div class="page"><van-nav-bar title="确认订单" left-arrow @click-left="() => $router.back()"/>
    <van-cell-group inset><van-cell title="服务地址" value="东区12-501"/><van-cell title="服务时间" value="今天 14:00-16:00"/></van-cell-group>
    <div class="price-row"><span>合计</span><b>¥79.00</b></div>
    <div style="padding:16px"><van-button round block type="danger" @click="submit">提交订单</van-button></div>
  </div>
</template>
<style scoped>.price-row{display:flex;justify-content:space-between;padding:16px;font-size:18px}.price-row b{color:#FF6B6B;font-size:24px}</style>
