<template>
  <div class="contract-detail">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>合同详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="合同编号">
          {{ detail.contractNo }}
        </el-descriptions-item>
        <el-descriptions-item label="合同标题">
          {{ detail.title }}
        </el-descriptions-item>
        <el-descriptions-item label="合同类型">
          {{ contractTypeMap[detail.contractType] || detail.contractType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务企业">
          {{ detail.enterpriseName || (detail.enterpriseId ? `企业#${detail.enterpriseId}` : '-') }}
        </el-descriptions-item>
        <el-descriptions-item label="本方企业">
          {{ detail.ourCompany }}
        </el-descriptions-item>
        <el-descriptions-item label="对方类型">
          {{ partnerTypeMap[detail.partnerType] || detail.partnerType }}
        </el-descriptions-item>
        <el-descriptions-item label="对方名称">
          {{ detail.partnerName }}
        </el-descriptions-item>
        <el-descriptions-item label="合同状态">
          <el-tag :type="statusColorMap[detail.status]" size="default">
            {{ statusMap[detail.status] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签署日期">
          {{ detail.signDate }}
        </el-descriptions-item>
        <el-descriptions-item label="到期日期">
          {{ detail.expireDate }}
        </el-descriptions-item>
        <el-descriptions-item label="合同金额">
          {{ detail.amount != null ? Number(detail.amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="币种">
          {{ currencyMap[detail.currency] || detail.currency }}
        </el-descriptions-item>
        <el-descriptions-item label="合同条款" :span="2">
          <div class="terms-content">{{ detail.terms || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ detail.remark || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detail.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ detail.updateTime }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- Status change actions -->
      <div class="status-actions" v-if="nextStatuses.length > 0">
        <el-divider />
        <span class="action-label">状态变更：</span>
        <el-button
          v-for="item in nextStatuses"
          :key="item.value"
          :type="getButtonType(item.value)"
          @click="handleStatusChange(item.value)"
        >
          变更为「{{ item.label }}」
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContractById, updateContractStatus } from '@/api/contract'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref({})

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

const contractTypeMap = {
  PURCHASE: '采购合同',
  SALES: '销售合同',
  SERVICE: '服务合同',
  AGENCY: '代理合同',
  OTHER: '其他'
}

const partnerTypeMap = {
  SUPPLIER: '供应商',
  CUSTOMER: '客户'
}

const currencyMap = {
  CNY: '人民币 (CNY)',
  USD: '美元 (USD)',
  EUR: '欧元 (EUR)'
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

const nextStatuses = computed(() => {
  return statusTransitions[detail.value.status] || []
})

function getButtonType(status) {
  const typeMap = {
    SIGNING: 'warning',
    EFFECTIVE: 'success',
    EXECUTING: 'primary',
    COMPLETED: 'success',
    EXPIRED: 'danger',
    DESTROYED: 'info'
  }
  return typeMap[status] || 'default'
}

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getContractById(route.params.id)
    detail.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取合同详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(newStatus) {
  try {
    await ElMessageBox.confirm(
      `确认将合同状态变更为"${statusMap[newStatus]}"吗？`,
      '状态变更确认',
      { type: 'warning' }
    )
    await updateContractStatus(route.params.id, newStatus)
    ElMessage.success('状态变更成功')
    fetchDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态变更失败')
    }
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.contract-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.terms-content {
  white-space: pre-wrap;
  word-break: break-all;
}

.status-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-label {
  font-weight: bold;
  color: #606266;
}
</style>
