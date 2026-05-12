<template>
  <div class="log-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.username" placeholder="操作用户" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="queryParams.module" placeholder="模块" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="queryParams.action" placeholder="操作" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-date-picker
              v-model="queryParams.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
              style="width: 160px"
            />
          </el-form-item>
          <el-form-item>
            <el-date-picker
              v-model="queryParams.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              style="width: 160px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="targetType" label="目标类型" width="120" />
        <el-table-column prop="targetId" label="目标ID" width="120" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center" />
        <el-table-column prop="requestUrl" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestIp" label="请求IP" width="140" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLogList } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ username: '', module: '', action: '', startDate: '', endDate: '', pageNum: 1, pageSize: 20 })

async function loadData() {
  loading.value = true
  try {
    const res = await getLogList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载操作日志失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { username: '', module: '', action: '', startDate: '', endDate: '', pageNum: 1 })
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.log-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
