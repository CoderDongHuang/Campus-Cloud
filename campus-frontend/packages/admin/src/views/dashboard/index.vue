<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const stats = ref({ todayGMV: 0, todayOrders: 0 })
const topServices = ref<Array<{name:string, salesCount:number}>>([])
const workerRanking = ref<Array<{worker_id:number, order_count:number}>>([])

onMounted(async () => {
  try {
    const [overview, top, ranking] = await Promise.all([
      axios.get('/api/v1/data/dashboard/overview'),
      axios.get('/api/v1/product/spu/top?n=5'),
      axios.get('/api/v1/order/stats/worker-ranking'),
    ])
    stats.value = overview.data.data
    topServices.value = top.data.data
    workerRanking.value = ranking.data.data
  } catch { /* 后端未启动时静默降级 */ }
})
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日 GMV</div>
          <div class="stat-value">¥{{ (stats.todayGMV || 0).toLocaleString() }}</div>
          <div class="stat-trend up">↑ 12.5%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">订单量</div>
          <div class="stat-value">{{ (stats.todayOrders || 0).toLocaleString() }} 单</div>
          <div class="stat-trend up">↑ 8.3%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">支付转化率</div>
          <div class="stat-value">86%</div>
          <div class="stat-trend down">↓ 2.1%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">在线师傅</div>
          <div class="stat-value">58 人</div>
          <div class="stat-trend flat">持平</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 + 排行 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card header="📈 热门服务 TOP5">
          <el-table :data="topServices" size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="服务名" />
            <el-table-column prop="salesCount" label="销量" width="100" sortable />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="👨‍🔧 师傅接单排行">
          <el-table :data="workerRanking" size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="worker_id" label="师傅ID" />
            <el-table-column prop="order_count" label="接单量" width="100" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  backdrop-filter: blur(10px);
}
.stat-label { font-size: 13px; color: #94A3B8; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #1E293B; margin-bottom: 4px; }
.stat-trend { font-size: 12px; }
.stat-trend.up { color: #22C55E; }
.stat-trend.down { color: #FF6B6B; }
.stat-trend.flat { color: #94A3B8; }
</style>
