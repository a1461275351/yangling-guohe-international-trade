<template>
  <div class="contract-list">
    <el-card shadow="never">
      <!-- Status filter tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="初始化" name="INIT" />
        <el-tab-pane label="签署中" name="SIGNING" />
        <el-tab-pane label="已生效" name="EFFECTIVE" />
        <el-tab-pane label="履行中" name="EXECUTING" />
        <el-tab-pane label="临期" name="EXPIRING" />
        <el-tab-pane label="已过期" name="EXPIRED" />
      </el-tabs>

      <!-- Advanced filter -->
      <el-form :model="queryParams" inline class="filter-form">
        <el-form-item label="合同编号">
          <el-input
            v-model="queryParams.contractNo"
            placeholder="请输入合同编号"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="对方名称">
          <el-input
            v-model="queryParams.partnerName"
            placeholder="请输入对方名称"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="签署日期">
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Action button -->
      <div class="table-toolbar">
        <el-button type="primary" @click="router.push('/contracts/create')">
          <el-icon><Plus /></el-icon>创建合同
        </el-button>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="contractList" border stripe>
        <el-table-column prop="contractNo" label="合同编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contractType" label="合同类型" width="100" align="center">
          <template #default="{ row }">
            {{ contractTypeMap[row.contractType] || row.contractType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="partnerName" label="对方名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="amount" label="合同金额" min-width="120" align="right">
          <template #default="{ row }">
            {{ row.amount != null ? Number(row.amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="signDate" label="签署日期" width="120" align="center" />
        <el-table-column prop="expireDate" label="到期日期" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusColorMap[row.status]" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/contracts/detail/${row.id}`)">
              查看详情
            </el-button>
            <el-button
              v-if="['INIT', 'SIGNING'].includes(row.status)"
              link
              type="primary"
              @click="router.push(`/contracts/edit/${row.id}`)"
            >
              编辑
            </el-button>
            <el-dropdown
              v-if="getNextStatuses(row.status).length > 0"
              @command="(cmd) => handleStatusChange(row.id, cmd)"
              trigger="click"
            >
              <el-button link type="primary">
                状态变更<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in getNextStatuses(row.status)"
                    :key="item.value"
                    :command="item.value"
                  >
                    {{ item.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              v-if="row.status === 'INIT'"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, ArrowDown } from '@element-plus/icons-vue'
import { getContractList, updateContractStatus, deleteContract } from '@/api/contract'

const router = useRouter()

const statusMap = {
  INIT: '初始化',
  SIGNING: '签署中',
  EFFECTIVE: '已生效',
  EXECUTING: '履行中',
  COMPLETED: '已完成',
  EXPIRED: '已过期',
  DESTROYED: '已销毁'
}

const contractTypeMap = {
  PURCHASE: '采购合同',
  SALES: '销售合同',
  SERVICE: '服务合同',
  AGENCY: '代理合同',
  OTHER: '其他'
}

const statusColorMap = {
  INIT: 'info',
  SIGNING: 'warning',
  EFFECTIVE: 'success',
  EXECUTING: '',
  COMPLETED: 'success',
  EXPIRED: 'danger',
  DESTROYED: 'info'
}

const statusTransitions = {
  INIT: [{ value: 'SIGNING', label: '签署中' }],
  SIGNING: [
    { value: 'EFFECTIVE', label: '已生效' },
    { value: 'DESTROYED', label: '已销毁' }
  ],
  EFFECTIVE: [{ value: 'EXECUTING', label: '履行中' }],
  EXECUTING: [
    { value: 'COMPLETED', label: '已完成' },
    { value: 'EXPIRED', label: '已过期' }
  ],
  COMPLETED: [],
  EXPIRED: [],
  DESTROYED: []
}

const loading = ref(false)
const contractList = ref([])
const total = ref(0)
const activeTab = ref('all')
const dateRange = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  contractNo: '',
  partnerName: '',
  startDate: '',
  endDate: '',
  status: ''
})

function getNextStatuses(status) {
  return statusTransitions[status] || []
}

function handleTabChange(tab) {
  if (tab === 'all') {
    queryParams.status = ''
  } else if (tab === 'EXPIRING') {
    queryParams.status = 'EXPIRING'
  } else {
    queryParams.status = tab
  }
  queryParams.pageNum = 1
  fetchList()
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
  fetchList()
}

function handleReset() {
  queryParams.contractNo = ''
  queryParams.partnerName = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = null
  activeTab.value = 'all'
  queryParams.status = ''
  queryParams.pageNum = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getContractList(queryParams)
    contractList.value = res.data.records || res.data.list || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取合同列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(id, newStatus) {
  try {
    await ElMessageBox.confirm(
      `确认将合同状态变更为"${statusMap[newStatus]}"吗？`,
      '状态变更确认',
      { type: 'warning' }
    )
    await updateContractStatus(id, newStatus)
    ElMessage.success('状态变更成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态变更失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除合同"${row.title}"吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    await deleteContract(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.contract-list {
  padding: 20px;
}

.filter-form {
  margin-bottom: 16px;
}

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
