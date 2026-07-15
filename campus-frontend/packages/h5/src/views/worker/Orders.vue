<script setup lang="ts">
import { ref, onMounted } from 'vue'; import axios from 'axios'
const orders = ref<any[]>([])
onMounted(async () => { try { const res = await axios.get('/api/v1/order/orders', { params: { page: 1, size: 50 } }); orders.value = res.data.data || [] } catch {} })
</script>
<template>
  <div class="page"><van-nav-bar title="全部工单"/>
    <van-tabs><van-tab v-for="s in ['PENDING_ACCEPT','IN_PROGRESS','COMPLETED']" :key="s" :title="s==='PENDING_ACCEPT'?'待接':s==='IN_PROGRESS'?'进行中':'已完成'">
      <div v-for="o in orders.filter((x:any)=>x.status===s)" :key="o.orderNo" class="card">
        <div class="c-no">{{o.orderNo}}</div><div class="c-price">¥{{o.actualAmount}}</div><div class="c-time">{{o.createTime}}</div>
      </div>
      <van-empty v-if="!orders.filter((x:any)=>x.status===s).length" description="暂无"/>
    </van-tab></van-tabs>
  </div>
</template>
<style scoped>.page{padding-bottom:50px}.card{margin:8px 16px;padding:16px;background:#fff;border-radius:12px}.c-no{font-weight:600}.c-price{color:#FF6B6B;font-weight:700}.c-time{font-size:12px;color:#ccc;margin-top:4px}</style>
