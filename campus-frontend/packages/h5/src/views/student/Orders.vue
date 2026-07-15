<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
const orders = ref<any[]>([])
onMounted(async () => { try { const res = await axios.get('/api/v1/order/orders', { params: { page: 1, size: 20 } }); orders.value = res.data.data || [] } catch {} })
const statusMap: Record<string,string> = { PENDING_PAY:'待支付',PENDING_ACCEPT:'待接单',IN_PROGRESS:'服务中',COMPLETED:'已完成',CANCELED:'已取消' }
</script>
<template>
  <div class="page"><van-nav-bar title="我的订单" />
    <div v-for="o in orders" :key="o.orderNo" class="order-card">
      <div class="o-header"><span class="o-no">{{o.orderNo}}</span><span class="o-status" :class="o.status">{{statusMap[o.status]}}</span></div>
      <div class="o-amount">¥{{o.actualAmount}}</div><div class="o-time">{{o.createTime}}</div>
    </div>
    <van-empty v-if="!orders.length" description="暂无订单" />
  </div>
</template>
<style scoped>.page{padding-bottom:50px}.order-card{margin:12px 16px;padding:16px;background:#fff;border-radius:12px}.o-header{display:flex;justify-content:space-between;margin-bottom:8px}.o-no{font-size:12px;color:#999}.o-status{font-size:12px;padding:2px 8px;border-radius:4px}.PENDING_PAY{color:#FF9800;background:#FFF3E0}.COMPLETED{color:#4CAF50;background:#E8F5E9}.o-amount{font-size:20px;font-weight:700;color:#FF6B6B}.o-time{font-size:12px;color:#ccc;margin-top:4px}</style>
