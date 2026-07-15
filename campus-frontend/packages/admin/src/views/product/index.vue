<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const categories = ref<any[]>([])
const spuList = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({ name: '', description: '', categoryId: null as any, status: 1, salesCount: 0 })

async function fetchCategories() {
  try { const res = await axios.get('/api/v1/product/categories/tree'); categories.value = res.data.data } catch { /* 降级 */ }
}
async function fetchSpu() {
  loading.value = true
  try { const res = await axios.get('/api/v1/product/spu/list', { params: { keyword: keyword.value || undefined } }); spuList.value = res.data.data } catch { /* 降级 */ }
  loading.value = false
}
async function saveSpu() {
  if (!form.value.name) { ElMessage.warning('请输入商品名称'); return }
  await axios.post('/api/v1/product/admin/spu', form.value)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  form.value = { name: '', description: '', categoryId: null, status: 1, salesCount: 0 }
  fetchSpu()
}
async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await axios.put(`/api/v1/product/admin/spu/${row.id}/status`, { status: newStatus })
  ElMessage.success(newStatus ? '已上架' : '已下架')
  fetchSpu()
}
onMounted(() => { fetchCategories(); fetchSpu() })
</script>

<template>
  <div>
    <div class="page-header"><h2>商品管理</h2><el-button type="primary" @click="dialogVisible = true">+ 新建商品</el-button></div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card header="服务类目">
          <div v-for="cat in categories" :key="cat.id" class="cat-item">
            <div class="cat-parent">{{ cat.icon || '📂' }} {{ cat.name }}</div>
            <div v-if="cat.children"><div v-for="sub in cat.children" :key="sub.id" class="cat-child">{{ sub.name }}</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <div class="toolbar"><el-input v-model="keyword" placeholder="搜索商品..." style="width:260px" clearable @change="fetchSpu" /></div>
          <el-table :data="spuList" v-loading="loading" style="margin-top:12px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="salesCount" label="销量" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" text @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="新建商品" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="类目">
          <el-select v-model="form.categoryId" placeholder="选择类目">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="saveSpu">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.cat-item { margin-bottom: 12px; }
.cat-parent { font-weight: 600; color: #333; font-size: 14px; }
.cat-child { font-size: 13px; color: #666; padding: 2px 0 2px 20px; cursor: pointer; }
.cat-child:hover { color: #409EFF; }
.toolbar { display: flex; gap: 12px; }
</style>
