<template>
  <div class="order-detail">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="header-row">
          <span>订单详情</span>
          <el-button @click="$router.push('/orders')">返回列表</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border v-if="order">
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(order.status)">{{ statusLabel(order.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="合同编号">{{ order.contractNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同名称">{{ order.contractTitle || '-' }}</el-descriptions-item>
            <el-descriptions-item label="贸易条款">{{ order.tradeTerms || '-' }}</el-descriptions-item>
            <el-descriptions-item label="付款方式">{{ order.paymentMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="总金额">
              {{ order.totalAmount != null ? Number(order.totalAmount).toFixed(2) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="币种">{{ order.currency || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ order.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ order.updateTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 货物清单 -->
        <el-tab-pane label="货物清单" name="goods">
          <el-table :data="goodsList" border stripe>
            <el-table-column type="index" width="60" label="序号" align="center" />
            <el-table-column prop="goodsName" label="品名" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.goodsName || row.name }}</template>
            </el-table-column>
            <el-table-column prop="goodsNo" label="货物编号" min-width="120" />
            <el-table-column prop="hsCode" label="HS编码" min-width="120" />
            <el-table-column prop="quantity" label="数量" width="100" align="center" />
            <el-table-column prop="unit" label="单位" width="80" align="center" />
            <el-table-column prop="price" label="单价" width="100" align="right">
              <template #default="{ row }">
                {{ row.price != null ? Number(row.price).toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                {{ ((row.quantity || 0) * (row.price || 0)).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="goods-total" v-if="goodsList.length > 0">
            合计金额: <strong>{{ goodsTotal }}</strong>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderById } from '@/api/order'

const route = useRoute()
const loading = ref(false)
const activeTab = ref('info')
const order = ref(null)
const goodsList = ref([])

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

const goodsTotal = computed(() => {
  return goodsList.value
    .reduce((sum, item) => sum + (item.quantity || 0) * (item.price || 0), 0)
    .toFixed(2)
})

async function loadOrder() {
  loading.value = true
  try {
    const res = await getOrderById(route.params.id)
    order.value = res.data
    goodsList.value = res.data.goodsList || res.data.orderGoods || []
  } catch (e) {
    ElMessage.error('加载订单详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  padding: 16px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.goods-total {
  margin-top: 16px;
  text-align: right;
  font-size: 16px;
}
.goods-total strong {
  color: #e6a23c;
  font-size: 18px;
}
</style>
