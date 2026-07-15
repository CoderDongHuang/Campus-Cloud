<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const templates = ref<any[]>([])
const dialogVisible = ref(false)
const form = ref({ name: '', discountValue: 10, useThreshold: 50, totalStock: 100, validDays: 7, status: 1 })

async function fetchTemplates() {
  try { const res = await axios.get('/api/v1/coupon/admin/templates'); templates.value = res.data.data || [] } catch { /* 降级 */ }
}
async function createTemplate() {
  if (!form.value.name) { ElMessage.warning('请输入券名称'); return }
  await axios.post('/api/v1/coupon/admin/template', form.value)
  ElMessage.success('券模板创建成功')
  dialogVisible.value = false
  fetchTemplates()
}
onMounted(fetchTemplates)
</script>

<template>
  <div>
    <div class="page-header"><h2>优惠券管理</h2><el-button type="primary" @click="dialogVisible = true">+ 创建券模板</el-button></div>
    <el-card>
      <el-table :data="templates">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="discountValue" label="面值(¥)" width="80" />
        <el-table-column prop="useThreshold" label="门槛(¥)" width="80" />
        <el-table-column prop="totalStock" label="库存" width="60" />
        <el-table-column prop="validDays" label="有效天数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'停用' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="创建券模板" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="面值"><el-input-number v-model="form.discountValue" :min="1" /></el-form-item>
        <el-form-item label="门槛"><el-input-number v-model="form.useThreshold" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.totalStock" :min="1" /></el-form-item>
        <el-form-item label="有效期(天)"><el-input-number v-model="form.validDays" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="createTemplate">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
</style>
