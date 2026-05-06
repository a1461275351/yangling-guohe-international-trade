<template>
  <div class="order-list">
    <el-card shadow="never">
      <!-- Status tabs -->
      <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="草稿" name="DRAFT" />
        <el-tab-pane label="已提交" name="SUBMITTED" />
        <el-tab-pane label="处理中" name="PROCESSING" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <!-- Advanced filter -->
      <div class="search-bar">
        <el-form :model="queryParams" inline>
          <el-form-item label="订单号">
            <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
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
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <el-button type="primary" @click="$router.push('/orders/create')">创建订单</el-button>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contractNo" label="合同编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="tradeTerms" label="贸易条款" width="100" align="center" />
        <el-table-column prop="paymentMethod" label="付款方式" width="100" align="center" />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">
            {{ row.totalAmount != null ? Number(row.totalAmount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/orders/detail/${row.id}`)">查看详情</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="$router.push(`/orders/edit/${row.id}`)">编辑</el-button>
            <el-dropdown v-if="row.status !== 'DRAFT' && row.status !== 'CANCELLED'" trigger="click" @command="(cmd) => handleGenerate(cmd, row)">
              <el-button link type="primary">生成票据</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="invoice">发票</el-dropdown-item>
                  <el-dropdown-item command="packingList">装箱单</el-dropdown-item>
                  <el-dropdown-item command="bill">提单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="row.status === 'DRAFT'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, deleteOrder } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeStatus = ref('')
const dateRange = ref(null)

const queryParams = reactive({
  orderNo: '',
  status: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 20
})

const statusMap = {
  DRAFT: { label: '草稿', type: 'info' },
  SUBMITTED: { label: '已提交', type: 'warning' },
  PROCESSING: { label: '处理中', type: '' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

function statusTagType(status) {
  return statusMap[status]?.type || 'info'
}

function statusLabel(status) {
  return statusMap[status]?.label || status
}

function handleTabChange(val) {
  queryParams.status = val
  queryParams.pageNum = 1
  loadData()
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
  queryParams.orderNo = ''
  queryParams.status = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  activeStatus.value = ''
  dateRange.value = null
  queryParams.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getOrderList(queryParams)
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleGenerate(command, row) {
  ElMessage.info(`生成${command === 'invoice' ? '发票' : command === 'packingList' ? '装箱单' : '提单'}功能开发中`)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', { type: 'warning' })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-list {
  padding: 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
