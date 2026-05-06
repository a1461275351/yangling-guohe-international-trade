<template>
  <div class="tax-management">
    <el-card shadow="never">
      <template #header>
        <span class="page-title">退税管理</span>
      </template>

      <div class="tax-content">
        <!-- 退税政策概述 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">退税政策概述</span>
          </template>
          <div class="section-body">
            <p>出口退税是指国家对出口货物已征收的国内税部分或全部退还给出口企业的一种税收优惠政策。其目的是使出口货物以不含税价格进入国际市场，增强出口商品的国际竞争力。</p>
            <p>根据现行税收政策，出口退税主要涉及增值税和消费税两个税种。企业在出口货物后，可凭相关凭证向税务机关申请退还已缴纳的增值税和消费税。</p>
            <p>适用范围包括：一般贸易出口、加工贸易出口、保税区出口等多种贸易方式下的货物出口。</p>
          </div>
        </el-card>

        <!-- 退税流程 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">退税流程</span>
          </template>
          <div class="section-body">
            <el-steps direction="vertical" :active="5" finish-status="success">
              <el-step title="出口报关" description="货物出口报关，取得报关单（出口退税联）。" />
              <el-step title="收汇核销" description="出口收汇后，到外汇管理局办理核销手续。" />
              <el-step title="准备材料" description="收集增值税专用发票、报关单退税联、外汇核销单、出口发票等退税申报所需材料。" />
              <el-step title="申报退税" description="在规定期限内，通过电子税务局或到主管税务机关提交退税申报。" />
              <el-step title="审核退税" description="税务机关审核申报材料，审核通过后将退税款项拨付至企业账户。" />
            </el-steps>
          </div>
        </el-card>

        <!-- 退税比例说明 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">退税比例说明</span>
          </template>
          <div class="section-body">
            <el-table :data="taxRateData" border stripe style="width: 100%">
              <el-table-column prop="category" label="商品类别" min-width="200" />
              <el-table-column prop="rate" label="退税率" width="120" align="center" />
              <el-table-column prop="remark" label="说明" min-width="250" />
            </el-table>
          </div>
        </el-card>

        <!-- 注意事项 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">注意事项</span>
          </template>
          <div class="section-body">
            <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
              <template #title>申报时限</template>
              出口企业应在货物报关出口之日次月起至次年4月30日前的各增值税纳税申报期内，申报办理出口退（免）税。逾期未申报的，将不予办理退税。
            </el-alert>
            <ul class="notice-list">
              <li>企业须具备进出口经营权并已办理出口退税登记。</li>
              <li>出口货物必须是增值税、消费税征收范围内的货物。</li>
              <li>出口货物必须是报关离境出口的货物。</li>
              <li>出口货物必须是在财务上做出口销售处理的货物。</li>
              <li>出口货物必须是已收汇并经核销的货物（部分地区已取消核销要求）。</li>
              <li>增值税专用发票须在认证期限内完成认证。</li>
              <li>退税申报数据须与海关报关数据、外汇核销数据一致。</li>
              <li>如发现骗取出口退税行为，将依法追缴退税款并处以罚款。</li>
            </ul>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const taxRateData = ref([
  { category: '机电产品、高新技术产品', rate: '13%', remark: '适用最高退税率，包括电子设备、机械设备等' },
  { category: '纺织品、服装', rate: '13%', remark: '大部分纺织品及成衣适用' },
  { category: '农产品及加工品', rate: '9% - 13%', remark: '根据加工程度不同，退税率有所差异' },
  { category: '化工产品', rate: '9% - 13%', remark: '精细化工产品退税率较高' },
  { category: '钢铁及制品', rate: '0% - 13%', remark: '部分高耗能产品退税率较低或取消退税' },
  { category: '有色金属', rate: '0% - 9%', remark: '初级产品退税率低，深加工产品退税率较高' },
  { category: '资源性产品', rate: '0%', remark: '原油、天然气等资源性产品不予退税' }
])
</script>

<style scoped>
.tax-management {
  padding: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
.tax-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.section-card {
  border-radius: 8px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.section-body p {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 8px;
  text-indent: 2em;
}
.notice-list {
  padding-left: 20px;
  color: #606266;
  line-height: 2;
}
.notice-list li {
  margin-bottom: 4px;
}
</style>
