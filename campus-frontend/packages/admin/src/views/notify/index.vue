<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
const messages = ref<any[]>([])
onMounted(async () => { try { const res = await axios.get('/api/v1/notify/inbox', { headers: { 'X-User-Id': '2073005678401105922' }, params: { page: 1, size: 20 } }); messages.value = res.data.data || [] } catch {} })
</script>
<template>
  <div><div class="ph"><h2>通知管理</h2></div>
    <el-card header="站内信记录">
      <el-table :data="messages">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userId" label="用户ID" width="100"/>
        <el-table-column prop="title" label="标题"/>
        <el-table-column prop="content" label="内容"/>
        <el-table-column prop="createTime" label="发送时间" width="180"/>
        <el-table-column prop="isRead" label="已读" width="60">
          <template #default="{row}"><el-tag :type="row.isRead?'success':'info'" size="small">{{row.isRead?'已读':'未读'}}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>.ph{margin-bottom:20px}.ph h2{margin:0;font-size:20px}</style>
