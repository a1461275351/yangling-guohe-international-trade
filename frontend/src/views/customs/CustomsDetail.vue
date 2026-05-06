<template>
  <div class="customs-detail">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="header-row">
          <span>报关单详情</span>
          <el-button @click="$router.push('/customs')">返回列表</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="报关单号">{{ detail.declarationNo }}</el-descriptions-item>
        <el-descriptions-item label="进出口类型">
          <el-tag :type="detail.ieType === 'I' ? 'success' : 'warning'">
            {{ detail.ieType === 'I' ? '进口' : '出口' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进出境日期">{{ detail.ieDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申报日期">{{ detail.declareDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运输方式">{{ detail.transportMode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="贸易方式">{{ detail.tradeMode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="海关编码">{{ detail.customsCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收发货人">{{ detail.consigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收发货人编码">{{ detail.consigneeCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经营单位">{{ detail.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经营单位编码">{{ detail.operatorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="起运国/抵达国">{{ detail.originCountry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="装货/指运港">{{ detail.loadingPort || '-' }}</el-descriptions-item>
        <el-descriptions-item label="境内目的地">{{ detail.domesticDest || '-' }}</el-descriptions-item>
        <el-descriptions-item label="许可证号">{{ detail.licenseNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同协议号">{{ detail.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成交方式">{{ detail.transactionMode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运费">{{ detail.freight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保费">{{ detail.insurance || '-' }}</el-descriptions-item>
        <el-descriptions-item label="杂费">{{ detail.sundry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          {{ detail.totalAmount != null ? Number(detail.totalAmount).toFixed(2) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="件数">{{ detail.packageCount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="毛重(kg)">{{ detail.grossWeight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="净重(kg)">{{ detail.netWeight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- Customs Goods -->
      <div v-if="goodsList.length > 0" style="margin-top: 24px">
        <el-divider content-position="left">商品明细</el-divider>
        <el-table :data="goodsList" border stripe>
          <el-table-column type="index" width="60" label="序号" align="center" />
          <el-table-column prop="goodsNo" label="商品序号" width="90" />
          <el-table-column prop="hsCode" label="商品编号" min-width="120" />
          <el-table-column prop="goodsName" label="商品名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="spec" label="规格型号" min-width="120" show-overflow-tooltip />
          <el-table-column prop="quantity" label="数量" width="90" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="unitPrice" label="单价" width="100" align="right">
            <template #default="{ row }">
              {{ row.unitPrice != null ? Number(row.unitPrice).toFixed(2) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="总价" width="120" align="right">
            <template #default="{ row }">
              {{ row.totalPrice != null ? Number(row.totalPrice).toFixed(2) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="originCountry" label="原产国" width="90" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCustomsById } from '@/api/customs'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const goodsList = ref([])

async function loadDetail() {
  loading.value = true
  try {
    const res = await getCustomsById(route.params.id)
    detail.value = res.data
    goodsList.value = res.data.goodsList || res.data.customsGoods || []
  } catch (e) {
    ElMessage.error('加载报关单详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.customs-detail {
  padding: 16px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
