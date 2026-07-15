<script setup lang="ts">
import { ref, onMounted } from 'vue'; import axios from 'axios'
const wallet = ref<any>({ pendingAmount: 0, availableAmount: 0, totalEarned: 0 })
const orders = ref<any[]>([])
onMounted(async () => {
  try { const [w, o] = await Promise.all([axios.get('/api/v1/settlement/wallet'), axios.get('/api/v1/order/orders', { params: { page: 1, size: 20 } })]); wallet.value = w.data.data || {}; orders.value = (o.data.data || []).filter((x:any) => x.status === 'PENDING_ACCEPT' || x.status === 'IN_PROGRESS') } catch {}
})
async function accept(orderNo: string) { await axios.put(`/api/v1/order/worker/orders/${orderNo}/accept`); location.reload() }
async function complete(orderNo: string) { await axios.put(`/api/v1/order/worker/orders/${orderNo}/complete`); location.reload() }
</script>
<template>
  <div class="page">
    <div class="wallet-card">
      <div class="w-today">今日收入</div><div class="w-amount">¥{{wallet.totalEarned || 0}}</div>
      <div class="w-row"><span>待结算 ¥{{wallet.pendingAmount || 0}}</span><span>可提现 ¥{{wallet.availableAmount || 0}}</span></div>
    </div>
    <van-tabs><van-tab title="待接单">
      <div v-for="o in orders.filter((x:any)=>x.status==='PENDING_ACCEPT')" :key="o.orderNo" class="order-card">
        <div class="o-name">{{o.orderNo}}</div><div class="o-price">¥{{o.actualAmount}}</div>
        <van-button size="small" round type="primary" @click="accept(o.orderNo)">接单</van-button>
      </div>
      <van-empty v-if="!orders.filter((x:any)=>x.status==='PENDING_ACCEPT').length" description="暂无新工单"/>
    </van-tab><van-tab title="进行中">
      <div v-for="o in orders.filter((x:any)=>x.status==='IN_PROGRESS')" :key="o.orderNo" class="order-card">
        <div class="o-name">{{o.orderNo}}</div><van-button size="small" round type="success" @click="complete(o.orderNo)">完成服务</van-button>
      </div>
    </van-tab></van-tabs>
  </div>
</template>
<style scoped>.page{padding-bottom:50px}.wallet-card{margin:16px;padding:24px;background:linear-gradient(135deg,#1565C0,#1976D2);border-radius:16px;color:#fff}.w-today{font-size:13px;opacity:0.8}.w-amount{font-size:36px;font-weight:700;margin:4px 0}.w-row{display:flex;gap:24px;font-size:12px;opacity:0.7}.order-card{display:flex;align-items:center;justify-content:space-between;margin:8px 16px;padding:16px;background:#fff;border-radius:12px}.o-name{font-weight:600}.o-price{color:#FF6B6B;font-weight:600}</style>
