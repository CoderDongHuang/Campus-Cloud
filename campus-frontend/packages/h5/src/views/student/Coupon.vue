<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { showSuccessToast } from 'vant'
const templates = ref<any[]>([])
const myCoupons = ref<any[]>([])
onMounted(async () => {
  try { const [t,m] = await Promise.all([axios.get('/api/v1/coupon/admin/templates'), axios.get('/api/v1/coupon/my')]); templates.value = t.data.data || []; myCoupons.value = m.data.data || [] } catch {}
})
async function grab(id: number) {
  try { await axios.post(`/api/v1/coupon/grab/${id}`); showSuccessToast('抢券成功!') } catch { showSuccessToast('已抢过或抢光了') }
}
</script>
<template>
  <div class="page"><van-nav-bar title="优惠券中心" />
    <van-tabs><van-tab title="抢券">
      <div v-for="t in templates" :key="t.id" class="coupon-card">
        <div class="c-left"><div class="c-value">¥{{t.discountValue}}</div><div class="c-condition">满{{t.useThreshold}}可用</div></div>
        <div class="c-right"><div class="c-name">{{t.name}}</div><div class="c-stock">剩余 {{t.totalStock}} 张</div><van-button size="small" round type="danger" @click="grab(t.id)">立即抢</van-button></div>
      </div>
    </van-tab><van-tab title="我的券">
      <div v-for="c in myCoupons" :key="c.id" class="coupon-card">
        <div class="c-left"><div class="c-value">¥{{c.discountValue}}</div><div class="c-condition">{{c.status==='UNUSED'?'可用':'已用'}}</div></div>
        <div class="c-right"><div class="c-name">优惠券</div><div class="c-stock">有效期至 {{c.expireTime?.slice(0,10)}}</div></div>
      </div>
      <van-empty v-if="!myCoupons.length" description="还没有优惠券" />
    </van-tab></van-tabs>
  </div>
</template>
<style scoped>.page{padding-bottom:50px}.coupon-card{display:flex;margin:12px 16px;padding:16px;background:linear-gradient(135deg,#FFF3E0,#FFE0B2);border-radius:12px}.c-left{text-align:center;width:80px}.c-value{font-size:28px;font-weight:700;color:#E65100}.c-condition{font-size:11px;color:#BF360C}.c-right{flex:1;display:flex;flex-direction:column;align-items:flex-end;justify-content:center;gap:6px}.c-name{font-weight:600}</style>
