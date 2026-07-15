<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const certifications = ref<any[]>([])
async function fetch() {
  try { const res = await axios.get('/api/v1/user/admin/certifications'); certifications.value = res.data.data || [] } catch {}
}
async function audit(id: number, approved: boolean) {
  await axios.put(`/api/v1/user/admin/certifications/${id}/audit`, { approved })
  ElMessage.success(approved ? '审核通过' : '已驳回')
  fetch()
}
onMounted(fetch)
</script>
<template>
  <div><div class="ph"><h2>用户管理</h2></div>
    <el-card header="师傅入驻审核">
      <el-table :data="certifications">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userId" label="师傅ID" width="100"/>
        <el-table-column prop="realName" label="姓名"/>
        <el-table-column prop="idCard" label="身份证" width="180"/>
        <el-table-column prop="skills" label="技能"/>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'danger':'warning'" size="small">{{row.status===1?'通过':row.status===2?'驳回':'待审'}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button v-if="row.status===0" size="small" type="success" @click="audit(row.id,true)">通过</el-button>
            <el-button v-if="row.status===0" size="small" type="danger" @click="audit(row.id,false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>.ph{margin-bottom:20px}.ph h2{margin:0;font-size:20px}</style>
