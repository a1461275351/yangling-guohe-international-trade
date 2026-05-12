<template>
  <div class="stats-dashboard">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="page-title">省级外综服认定驾驶舱</span>
          <div>
            <el-button type="primary" @click="handleExport('enterprises')">导出企业清单</el-button>
            <el-button type="primary" @click="handleExport('trade')">导出进出口统计</el-button>
            <el-button type="primary" @click="handleExport('service')">导出服务概览</el-button>
          </div>
        </div>
      </template>

      <!-- 核心指标 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" :class="['indicator-card', data.enterpriseMet ? 'met' : 'unmet']">
            <div class="indicator">
              <div class="indicator-value">{{ data.enterpriseCount }}</div>
              <div class="indicator-label">服务企业数</div>
              <div class="indicator-target">
                目标: {{ data.enterpriseThreshold }} 家
                <el-tag :type="data.enterpriseMet ? 'success' : 'danger'" size="small" style="margin-left: 8px">
                  {{ data.enterpriseMet ? '已达标' : '未达标' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" :class="['indicator-card', data.tradeMet ? 'met' : 'unmet']">
            <div class="indicator">
              <div class="indicator-value">{{ formatAmount(data.annualTradeTotal) }}</div>
              <div class="indicator-label">年度进出口额(万元)</div>
              <div class="indicator-target">
                目标: {{ data.tradeThreshold }} 万元
                <el-tag :type="data.tradeMet ? 'success' : 'danger'" size="small" style="margin-left: 8px">
                  {{ data.tradeMet ? '已达标' : '未达标' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="indicator-card">
            <div class="indicator">
              <div class="indicator-value">{{ data.serviceCoverage }}</div>
              <div class="indicator-label">服务类型覆盖</div>
              <div class="indicator-target">通关/物流/退税/结算/信保/融资</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 进出口明细 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover">
            <el-statistic title="年度出口额(万元)" :value="data.annualExportTotal || 0" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover">
            <el-statistic title="年度进口额(万元)" :value="data.annualImportTotal || 0" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover">
            <el-statistic title="已签署协议企业" :value="data.signedAgreementCount || 0" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 各服务模块数据量 -->
      <el-card shadow="hover" style="margin-top: 20px">
        <template #header><span class="section-title">综合服务数据概览</span></template>
        <el-row :gutter="20">
          <el-col :xs="12" :sm="4">
            <el-statistic title="通关(报关单)" :value="'已有'" />
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-statistic title="物流服务" :value="data.logisticsCount || 0" />
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-statistic title="退税业务" :value="data.taxRefundCount || 0" />
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-statistic title="结算收汇" :value="data.settlementCount || 0" />
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-statistic title="信保服务" :value="data.insuranceCount || 0" />
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-statistic title="融资协助" :value="data.financingCount || 0" />
          </el-col>
        </el-row>
      </el-card>

      <!-- 认定材料清单 -->
      <el-card shadow="hover" style="margin-top: 20px">
        <template #header><span class="section-title">省级认定申报材料清单</span></template>
        <el-table :data="materialList" border stripe>
          <el-table-column prop="name" label="材料名称" min-width="250" />
          <el-table-column label="平台支撑" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.ready ? 'success' : 'warning'" size="small">
                {{ row.ready ? '可导出' : '待完善' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="source" label="数据来源" min-width="200" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getCertificationDashboard, exportEnterprises, exportTradeStats, exportServiceSummary } from '@/api/stats'

const data = reactive({
  enterpriseCount: 0, signedAgreementCount: 0, annualExportTotal: 0, annualImportTotal: 0,
  annualTradeTotal: 0, serviceCoverage: '0/6', logisticsCount: 0, taxRefundCount: 0,
  settlementCount: 0, insuranceCount: 0, financingCount: 0,
  enterpriseThreshold: 10, enterpriseMet: false, tradeThreshold: 3500, tradeMet: false
})

const materialList = computed(() => [
  { name: '陕西省外贸综合服务企业申请报告', ready: false, source: '手动撰写，平台提供数据支撑' },
  { name: '陕西省外贸综合服务企业申报表', ready: true, source: '驾驶舱统计数据' },
  { name: '单位法人营业执照复印件', ready: false, source: '文件管理-企业资质' },
  { name: '服务企业清单及合同协议', ready: data.enterpriseCount > 0, source: '服务企业档案 + 合同管理' },
  { name: '通关服务证明材料', ready: true, source: '报关单管理' },
  { name: '物流服务证明材料', ready: data.logisticsCount > 0, source: '物流服务模块' },
  { name: '退税服务证明材料', ready: data.taxRefundCount > 0, source: '退税业务模块' },
  { name: '结算服务证明材料', ready: data.settlementCount > 0, source: '结算收汇模块' },
  { name: '信保服务证明材料', ready: data.insuranceCount > 0, source: '信保服务模块' },
  { name: '融资服务证明材料', ready: data.financingCount > 0, source: '融资协助模块' },
  { name: '线上平台运行情况材料', ready: true, source: '操作日志 + 平台截图' },
])

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function handleExport(type) {
  try {
    let res, filename
    if (type === 'enterprises') {
      res = await exportEnterprises()
      filename = '服务企业清单.xlsx'
    } else if (type === 'trade') {
      res = await exportTradeStats()
      filename = '进出口统计.xlsx'
    } else {
      res = await exportServiceSummary()
      filename = '综合服务概览.xlsx'
    }
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
    console.error(e)
  }
}

onMounted(async () => {
  try {
    const res = await getCertificationDashboard()
    Object.assign(data, res.data)
  } catch (e) {
    console.warn('加载驾驶舱数据失败', e)
  }
})
</script>

<style scoped>
.stats-dashboard { padding: 16px; }
.page-title { font-size: 18px; font-weight: 600; }
.section-title { font-size: 15px; font-weight: 600; }
.stats-row { margin-bottom: 20px; }
.indicator-card { text-align: center; }
.indicator-card.met { border-left: 4px solid #67c23a; }
.indicator-card.unmet { border-left: 4px solid #f56c6c; }
.indicator-value { font-size: 36px; font-weight: 700; color: #303133; }
.indicator-label { font-size: 14px; color: #909399; margin: 8px 0 4px; }
.indicator-target { font-size: 13px; color: #606266; }

@media (max-width: 768px) {
  .stats-row .el-col { margin-bottom: 12px; }
}
</style>
