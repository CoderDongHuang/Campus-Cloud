<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
const reviews = ref<any[]>([])
async function fetch() { try { const res = await axios.get('/api/v1/review/admin/pending'); reviews.value = res.data.data || [] } catch {} }
async function approve(id: number) { await axios.put(`/api/v1/review/admin/reviews/${id}/approve`); ElMessage.success('审核通过'); fetch() }
onMounted(fetch)
</script>
<template>
  <div><div class="ph"><h2>评价管理</h2></div>
    <el-card header="待审核评价">
      <el-table :data="reviews">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="orderId" label="订单ID"/>
        <el-table-column prop="userId" label="用户ID" width="100"/>
        <el-table-column prop="workerId" label="师傅ID" width="100"/>
        <el-table-column prop="rating" label="评分" width="60"/>
        <el-table-column prop="content" label="内容"/>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button size="small" type="success" @click="approve(row.id)">通过</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>.ph{margin-bottom:20px}.ph h2{margin:0;font-size:20px}</style>
