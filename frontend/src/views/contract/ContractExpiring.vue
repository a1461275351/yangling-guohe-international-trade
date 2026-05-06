<template>
  <div class="contract-expiring">
    <!-- Statistics cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.total || 0 }}</div>
            <div class="stat-label">临期合同总数</div>
          </div>
          <el-icon class="stat-icon" :size="48" color="#409EFF"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value warning">{{ stats.thisWeek || 0 }}</div>
            <div class="stat-label">本周到期</div>
          </div>
          <el-icon class="stat-icon" :size="48" color="#E6A23C"><Timer /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value danger">{{ stats.thisMonth || 0 }}</div>
            <div class="stat-label">本月到期</div>
          </div>
          <el-icon class="stat-icon" :size="48" color="#F56C6C"><Warning /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- Filter and table -->
    <el-card shadow="never">
      <div class="filter-bar">
        <span class="filter-label">到期天数筛选：</span>
        <el-radio-group v-model="days" @change="handleDaysChange">
          <el-radio-button :value="15">15天</el-radio-button>
          <el-radio-button :value="30">30天</el-radio-button>
          <el-radio-button :value="60">60天</el-radio-button>
          <el-radio-button :value="90">90天</el-radio-button>
        </el-radio-group>
      </div>

      <el-table v-loading="loading" :data="expiringList" border stripe>
        <el-table-column prop="contractNo" label="合同编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="partnerName" label="对方名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="expireDate" label="到期日期" width="120" align="center" />
        <el-table-column label="剩余天数" width="120" align="center">
          <template #default="{ row }">
            <span :class="getRemainingDaysClass(row.remainingDays)">
              {{ row.remainingDays }}天
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusColorMap[row.status]" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/contracts/detail/${row.id}`)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Timer, Warning } from '@element-plus/icons-vue'
import { getExpiringContracts, getExpiringStats } from '@/api/contract'

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

const statusColorMap = {
  INIT: 'info',
  SIGNING: 'warning',
  EFFECTIVE: 'success',
  EXECUTING: '',
  COMPLETED: 'success',
  EXPIRED: 'danger',
  DESTROYED: 'info'
}

const loading = ref(false)
const days = ref(30)
const expiringList = ref([])
const stats = reactive({
  total: 0,
  thisWeek: 0,
  thisMonth: 0
})

function getRemainingDaysClass(remainingDays) {
  if (remainingDays < 15) return 'days-danger'
  if (remainingDays < 30) return 'days-warning'
  return ''
}

function calculateRemainingDays(expireDate) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const expire = new Date(expireDate)
  expire.setHours(0, 0, 0, 0)
  return Math.ceil((expire - today) / (1000 * 60 * 60 * 24))
}

async function fetchStats() {
  try {
    const res = await getExpiringStats()
    const data = res.data || {}
    stats.total = data.total || 0
    stats.thisWeek = data.thisWeek || 0
    stats.thisMonth = data.thisMonth || 0
  } catch (error) {
    console.error('获取临期统计失败:', error)
  }
}

async function fetchExpiringList() {
  loading.value = true
  try {
    const res = await getExpiringContracts({ days: days.value })
    const list = res.data || []
    expiringList.value = list
      .map((item) => ({
        ...item,
        remainingDays: item.remainingDays != null ? item.remainingDays : calculateRemainingDays(item.expireDate)
      }))
      .sort((a, b) => a.remainingDays - b.remainingDays)
  } catch (error) {
    console.error('获取临期合同列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleDaysChange() {
  fetchExpiringList()
}

onMounted(() => {
  fetchStats()
  fetchExpiringList()
})
</script>

<style scoped>
.contract-expiring {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: default;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stat-icon {
  opacity: 0.8;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-label {
  font-weight: bold;
  color: #606266;
  margin-right: 12px;
}

.days-danger {
  color: #f56c6c;
  font-weight: bold;
}

.days-warning {
  color: #e6a23c;
  font-weight: bold;
}
</style>
