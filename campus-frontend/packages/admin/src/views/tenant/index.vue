<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@shared/utils/request'
const tenants = ref<any[]>([])
const packages = ref<any[]>([])
onMounted(async () => {
  try { const [t,p] = await Promise.all([http.get('/api/v1/tenant/admin/list'), http.get('/api/v1/tenant/packages')]); tenants.value = t.data.data || []; packages.value = p.data.data || [] } catch {}
})
</script>
<template>
  <div><div class="ph"><h2>租户管理</h2></div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card header="学校列表">
          <el-table :data="tenants">
            <el-table-column prop="id" label="ID" width="80"/>
            <el-table-column prop="name" label="学校名称"/>
            <el-table-column prop="contactName" label="联系人"/>
            <el-table-column prop="contactPhone" label="联系电话" width="120"/>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{row}">
                <el-tag :type="row.status===1?'success':row.status===0?'warning':'danger'" size="small">
                  {{row.status===1?'正式':row.status===0?'待审':'关闭'}}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="套餐方案">
          <div v-for="pkg in packages" :key="pkg.id" class="pkg-item">
            <b>{{pkg.name}}</b> <span style="color:#999">¥{{pkg.price}}/月</span>
            <div class="pkg-detail">订单上限:{{pkg.maxOrders}} | 师傅上限:{{pkg.maxWorkers}} | 存储:{{pkg.maxStorageMb}}MB</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<style scoped>.ph{margin-bottom:20px}.ph h2{margin:0;font-size:20px}.pkg-item{margin-bottom:12px;padding-bottom:12px;border-bottom:1px solid #f0f0f0}.pkg-detail{font-size:12px;color:#bbb;margin-top:4px}</style>
