<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@shared/utils/request'
import { ElMessage } from 'element-plus'

const withdraws = ref<any[]>([])
async function fetch() {
  try { const res = await http.get('/api/v1/settlement/admin/withdraws'); withdraws.value = res.data.data || [] } catch {}
}
async function approve(id: number) {
  await http.put(`/api/v1/settlement/admin/withdraws/${id}/approve`)
  ElMessage.success('提现审核通过')
  fetch()
}
onMounted(fetch)
</script>
<template>
  <div><div class="ph"><h2>结算管理</h2></div>
    <el-card header="提现审核">
      <el-table :data="withdraws">
        <el-table-column prop="withdrawNo" label="提现单号" width="200"/>
        <el-table-column prop="userId" label="师傅ID" width="100"/>
        <el-table-column prop="amount" label="金额(¥)"/>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status==='APPROVED'?'success':'warning'" size="small">{{row.status}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180"/>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="approve(row.id)">通过</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>.ph{margin-bottom:20px}.ph h2{margin:0;font-size:20px}</style>
