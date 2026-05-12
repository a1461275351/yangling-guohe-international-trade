<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="welcome-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <div class="welcome-section">
            <h2 class="welcome-title">
              欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}
            </h2>
            <p class="welcome-desc">
              {{ greetingText }}，祝您工作顺利！
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Admin / Guohe Stats -->
    <el-row v-if="userStore.isAdmin || userStore.isGuohe" :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/tenants')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ecf5ff; color: #409eff;">
              <el-icon :size="32"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.tenantCount }}</div>
              <div class="stat-label">租户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/users')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f0f9eb; color: #67c23a;">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/user-applies')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #fdf6ec; color: #e6a23c;">
              <el-icon :size="32"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApprovals }}</div>
              <div class="stat-label">待审批申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Certification Indicators (Admin/Guohe) -->
    <el-row v-if="(userStore.isAdmin || userStore.isGuohe) && certData.loaded" :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" :class="['stat-card', 'cert-card']" @click="$router.push('/stats')">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: certData.enterpriseMet ? '#f0f9eb' : '#fef0f0', color: certData.enterpriseMet ? '#67c23a' : '#f56c6c' }">
              <el-icon :size="32"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ certData.enterpriseCount }} <span class="stat-unit">/ {{ certData.enterpriseThreshold }}</span></div>
              <div class="stat-label">服务企业数
                <el-tag :type="certData.enterpriseMet ? 'success' : 'danger'" size="small" style="margin-left: 4px">
                  {{ certData.enterpriseMet ? '达标' : '未达标' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" :class="['stat-card', 'cert-card']" @click="$router.push('/stats')">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: certData.tradeMet ? '#f0f9eb' : '#fef0f0', color: certData.tradeMet ? '#67c23a' : '#f56c6c' }">
              <el-icon :size="32"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatAmount(certData.annualTradeTotal) }} <span class="stat-unit">万元</span></div>
              <div class="stat-label">年度进出口额
                <el-tag :type="certData.tradeMet ? 'success' : 'danger'" size="small" style="margin-left: 4px">
                  {{ certData.tradeMet ? '达标' : '未达标' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" :class="['stat-card', 'cert-card']" @click="$router.push('/stats')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ecf5ff; color: #409eff;">
              <el-icon :size="32"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ certData.serviceCoverage }}</div>
              <div class="stat-label">服务类型覆盖</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Enterprise Stats -->
    <el-row v-else-if="!userStore.isAdmin && !userStore.isGuohe" :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/contracts')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ecf5ff; color: #409eff;">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.contractCount }}</div>
              <div class="stat-label">合同数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/orders')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f0f9eb; color: #67c23a;">
              <el-icon :size="32"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orderCount }}</div>
              <div class="stat-label">订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/goods')">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #fdf6ec; color: #e6a23c;">
              <el-icon :size="32"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.goodsCount }}</div>
              <div class="stat-label">货物数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button v-if="userStore.isAdmin || userStore.isGuohe" @click="$router.push('/users')">用户管理</el-button>
            <el-button v-if="userStore.isAdmin || userStore.isGuohe" @click="$router.push('/tenants')">租户管理</el-button>
            <el-button @click="$router.push('/contracts')">合同管理</el-button>
            <el-button @click="$router.push('/orders')">订单管理</el-button>
            <el-button @click="$router.push('/profile')">个人中心</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header-title">系统信息</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="当前角色">
              <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="所属企业">
              {{ userStore.userInfo?.tenantName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="企业号">
              {{ userStore.userInfo?.tenantCode || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTenantList } from '@/api/tenant'
import { getUserList, getApplyList } from '@/api/user'
import { getContractList } from '@/api/contract'
import { getOrderList } from '@/api/order'
import { getGoodsList } from '@/api/goods'
import { getCertificationDashboard } from '@/api/stats'

const userStore = useUserStore()

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const roleLabel = computed(() => {
  const map = { ADMIN: '系统管理员', GUOHE: '国合员工', ENTERPRISE: '企业员工' }
  return map[userStore.role] || userStore.role
})

const roleTagType = computed(() => {
  const map = { ADMIN: 'danger', GUOHE: '', ENTERPRISE: 'success' }
  return map[userStore.role] || 'info'
})

const stats = reactive({
  tenantCount: 0,
  userCount: 0,
  pendingApprovals: 0,
  contractCount: 0,
  orderCount: 0,
  goodsCount: 0
})

const certData = reactive({
  loaded: false,
  enterpriseCount: 0,
  enterpriseThreshold: 10,
  enterpriseMet: false,
  annualTradeTotal: 0,
  tradeThreshold: 3500,
  tradeMet: false,
  serviceCoverage: '0/6'
})

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  if (userStore.isAdmin || userStore.isGuohe) {
    try {
      const [tenantRes, userRes, applyRes] = await Promise.all([
        getTenantList({ current: 1, size: 1 }),
        getUserList({ current: 1, size: 1 }),
        getApplyList({ status: 'PENDING', current: 1, size: 1 })
      ])
      stats.tenantCount = tenantRes.data?.total || 0
      stats.userCount = userRes.data?.total || 0
      stats.pendingApprovals = applyRes.data?.total || 0
    } catch (e) {
      console.warn('加载管理统计数据失败', e)
    }
    try {
      const certRes = await getCertificationDashboard()
      Object.assign(certData, certRes.data)
      certData.loaded = true
    } catch (e) {
      console.warn('加载认定指标失败', e)
    }
  } else {
    try {
      const [contractRes, orderRes, goodsRes] = await Promise.all([
        getContractList({ current: 1, size: 1 }),
        getOrderList({ current: 1, size: 1 }),
        getGoodsList({ current: 1, size: 1 })
      ])
      stats.contractCount = contractRes.data?.total || 0
      stats.orderCount = orderRes.data?.total || 0
      stats.goodsCount = goodsRes.data?.total || 0
    } catch (e) {
      console.warn('加载业务统计数据失败', e)
    }
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-row {
  margin-bottom: 20px;
}

.welcome-section {
  padding: 10px 0;
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-unit {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
}

.cert-card {
  border-top: 3px solid #409eff;
}

.info-row {
  margin-bottom: 20px;
}

.card-header-title {
  font-weight: 600;
  font-size: 15px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 12px;
  }

  .info-row .el-col {
    margin-bottom: 12px;
  }
}
</style>
