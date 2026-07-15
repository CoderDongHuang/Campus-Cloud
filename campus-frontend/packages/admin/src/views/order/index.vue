<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const orders = ref<any[]>([])
const refunds = ref<any[]>([])
const loading = ref(false)

async function fetchOrders() {
  loading.value = true
  try {
    const [oRes, rRes] = await Promise.all([
      axios.get('/api/v1/order/orders', { params: { page: 1, size: 50 } }),
      axios.get('/api/v1/order/admin/refunds'),
    ])
    orders.value = oRes.data.data || []
    refunds.value = rRes.data.data || []
  } catch { /* backend may not be running */ }
  loading.value = false
}

async function approveRefund(refundId: number) {
  await axios.put(`/api/v1/order/admin/refunds/${refundId}/approve`)
  ElMessage.success('退款已通过')
  fetchOrders()
}

const statusMap: Record<string, string> = {
  PENDING_PAY: '待支付', PENDING_ACCEPT: '待接单', IN_PROGRESS: '服务中',
  COMPLETED: '已完成', CANCELED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款',
}

onMounted(fetchOrders)
</script>

<template>
  <div>
    <div class="page-header"><h2>订单管理</h2></div>

    <!-- 退款审核区 -->
    <el-card v-if="refunds.length" header="⏳ 待审核退款" style="margin-bottom:20px">
      <el-table :data="refunds" size="small">
        <el-table-column prop="refundId" label="退款ID" width="100" />
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="refundAmount" label="退款金额" width="100" />
        <el-table-column prop="reason" label="原因" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveRefund(row.refundId)">通过</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订单列表 -->
    <el-card>
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="220" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="金额(¥)" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" text>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
</style>
