<template>
  <div class="customs-list">
    <el-card shadow="never">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-button type="primary" @click="$router.push('/customs/import')">导入报关单</el-button>
      </div>

      <!-- Filter -->
      <div class="search-bar">
        <el-form :model="queryParams" inline>
          <el-form-item label="报关单号">
            <el-input v-model="queryParams.declarationNo" placeholder="请输入报关单号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="进出口类型">
            <el-radio-group v-model="queryParams.ieType" @change="handleSearch">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="I">进口</el-radio-button>
              <el-radio-button label="E">出口</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
          </el-form-item>
          <el-form-item label="运输方式">
            <el-input v-model="queryParams.transportMode" placeholder="运输方式" clearable />
          </el-form-item>
          <el-form-item label="服务企业">
            <el-select v-model="queryParams.enterpriseId" placeholder="全部" clearable filterable @change="handleSearch">
              <el-option v-for="e in enterpriseList" :key="e.id" :label="e.enterpriseName" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable>
              <el-option label="待审核" value="PENDING" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="已放行" value="RELEASED" />
              <el-option label="已退回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="declarationNo" label="报关单号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="ieType" label="进出口类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.ieType === 'I' ? 'success' : 'warning'">
              {{ row.ieType === 'I' ? '进口' : '出口' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ieDate" label="进出境日期" width="120" />
        <el-table-column prop="transportMode" label="运输方式" width="100" align="center" />
        <el-table-column prop="tradeMode" label="贸易方式" width="100" align="center" />
        <el-table-column prop="customsCode" label="海关编码" width="110" />
        <el-table-column prop="consigneeName" label="收货人" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">
            {{ row.totalAmount != null ? Number(row.totalAmount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="customsStatusType(row.status)">{{ customsStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/customs/detail/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
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
import { getCustomsList } from '@/api/customs'
import { getActiveEnterprises } from '@/api/enterprise'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)
const enterpriseList = ref([])

const queryParams = reactive({
  declarationNo: '',
  ieType: '',
  startDate: '',
  endDate: '',
  transportMode: '',
  status: '',
  enterpriseId: null,
  pageNum: 1,
  pageSize: 20
})

const statusConfig = {
  PENDING: { label: '待审核', type: 'info' },
  APPROVED: { label: '已审核', type: '' },
  RELEASED: { label: '已放行', type: 'success' },
  REJECTED: { label: '已退回', type: 'danger' }
}

function customsStatusType(status) {
  return statusConfig[status]?.type || 'info'
}

function customsStatusLabel(status) {
  return statusConfig[status]?.label || status || '-'
}

function handleDateChange(val) {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.declarationNo = ''
  queryParams.ieType = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.transportMode = ''
  queryParams.status = ''
  queryParams.enterpriseId = null
  dateRange.value = null
  queryParams.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCustomsList(queryParams)
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadEnterprises() {
  try {
    const res = await getActiveEnterprises()
    enterpriseList.value = res.data || []
  } catch (e) {
    console.warn('加载企业列表失败', e)
  }
}

onMounted(() => {
  loadEnterprises()
  loadData()
})
</script>

<style scoped>
.customs-list {
  padding: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
