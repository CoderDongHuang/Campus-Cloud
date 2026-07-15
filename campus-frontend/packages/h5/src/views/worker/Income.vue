<script setup lang="ts">
import { ref, onMounted } from 'vue'; import axios from 'axios'; import { showSuccessToast } from 'vant'
const wallet = ref<any>({})
const incomeList = ref<any[]>([])
onMounted(async () => {
  try { const [w, i] = await Promise.all([axios.get('/api/v1/settlement/wallet'), axios.get('/api/v1/settlement/incomeList')]); wallet.value = w.data.data || {}; incomeList.value = i.data.data || [] } catch {}
})
async function withdraw() { try { await axios.post('/api/v1/settlement/withdraw', { amount: wallet.value.availableAmount }); showSuccessToast('提现申请已提交') } catch {} }
</script>
<template>
  <div class="page"><van-nav-bar title="我的收入"/>
    <div class="wallet-card"><div class="w-label">可提现余额</div><div class="w-amount">¥{{wallet.availableAmount || 0}}</div><div class="w-row"><span>待结算 ¥{{wallet.pendingAmount || 0}}</span><span>累计 ¥{{wallet.totalEarned || 0}}</span></div>
      <van-button round block type="primary" style="margin-top:12px" @click="withdraw">申请提现</van-button>
    </div>
    <van-cell-group inset title="收入明细"><van-cell v-for="item in incomeList" :key="item.id" :title="item.orderNo" :value="'¥'+item.workerAmount" :label="item.createTime"/></van-cell-group>
  </div>
</template>
<style scoped>.page{padding-bottom:50px}.wallet-card{margin:16px;padding:24px;background:linear-gradient(135deg,#2E7D32,#43A047);border-radius:16px;color:#fff}.w-label{font-size:13px;opacity:0.8}.w-amount{font-size:36px;font-weight:700;margin:4px 0}.w-row{display:flex;gap:24px;font-size:12px;opacity:0.7}</style>
