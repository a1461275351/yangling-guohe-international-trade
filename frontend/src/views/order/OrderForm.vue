<template>
  <div class="order-form">
    <el-card shadow="never">
      <template #header>
        <span>{{ isEdit ? '编辑订单' : '创建订单' }}</span>
      </template>

      <!-- Steps -->
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step title="选择合同" />
        <el-step title="订单信息" />
        <el-step title="货物清单" />
      </el-steps>

      <!-- Step 1: Select Contract -->
      <div v-show="currentStep === 0">
        <el-alert v-if="!selectedContract" title="请从下方列表中选择一份生效合同" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
        <el-alert v-else title="已选择合同" type="success" :closable="false" show-icon style="margin-bottom: 16px">
          合同编号: {{ selectedContract.contractNo }}
        </el-alert>

        <el-table
          v-loading="contractLoading"
          :data="contractList"
          border
          stripe
          highlight-current-row
          @current-change="handleContractSelect"
        >
          <el-table-column prop="contractNo" label="合同编号" min-width="150" />
          <el-table-column prop="contractTitle" label="合同名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="partnerName" label="合作方" min-width="150" />
          <el-table-column prop="signDate" label="签订日期" width="120" />
          <el-table-column prop="totalAmount" label="金额" width="120" align="right">
            <template #default="{ row }">
              {{ row.totalAmount != null ? Number(row.totalAmount).toFixed(2) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="currency" label="币种" width="80" align="center" />
        </el-table>
      </div>

      <!-- Step 2: Order Info -->
      <div v-show="currentStep === 1">
        <el-form ref="orderInfoRef" :model="formData" :rules="orderRules" label-width="100px" style="max-width: 600px">
          <el-form-item label="贸易条款" prop="tradeTerms">
            <el-select v-model="formData.tradeTerms" placeholder="请选择贸易条款" style="width: 100%">
              <el-option label="FOB" value="FOB" />
              <el-option label="CIF" value="CIF" />
              <el-option label="CFR" value="CFR" />
              <el-option label="EXW" value="EXW" />
            </el-select>
          </el-form-item>
          <el-form-item label="付款方式" prop="paymentMethod">
            <el-select v-model="formData.paymentMethod" placeholder="请选择付款方式" style="width: 100%">
              <el-option label="T/T (电汇)" value="T/T" />
              <el-option label="L/C (信用证)" value="L/C" />
              <el-option label="D/P (付款交单)" value="D/P" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 3: Goods List -->
      <div v-show="currentStep === 2">
        <div class="toolbar">
          <el-button type="primary" @click="goodsDialogVisible = true">从货物库添加</el-button>
          <span class="total-amount">总金额: <strong>{{ totalAmount }}</strong></span>
        </div>

        <el-table :data="formData.goodsList" border stripe>
          <el-table-column prop="goodsName" label="品名" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.goodsName || row.name }}</template>
          </el-table-column>
          <el-table-column prop="goodsNo" label="货物编号" min-width="120" />
          <el-table-column prop="hsCode" label="HS编码" min-width="120" />
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :controls="false" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="单价" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              {{ ((row.quantity || 0) * (row.price || 0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="formData.goodsList.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Bottom buttons -->
      <div class="bottom-actions">
        <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
        <el-button v-if="currentStep < 2" type="primary" @click="handleNext">下一步</el-button>
        <el-button v-if="currentStep === 2" type="primary" :loading="submitting" @click="handleSubmit">提交订单</el-button>
        <el-button @click="$router.push('/orders')">取消</el-button>
      </div>
    </el-card>

    <!-- Goods Selection Dialog -->
    <el-dialog v-model="goodsDialogVisible" title="选择货物" width="800px" destroy-on-close>
      <el-table
        :data="goodsLibrary"
        v-loading="goodsLibLoading"
        border
        stripe
        @selection-change="handleGoodsSelection"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="goodsNo" label="货物编号" min-width="120" />
        <el-table-column prop="name" label="品名" min-width="140" />
        <el-table-column prop="hsCode" label="HS编码" min-width="120" />
        <el-table-column prop="spec" label="规格" min-width="100" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="{ row }">{{ row.price != null ? Number(row.price).toFixed(2) : '-' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmGoodsSelection">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderById, createOrder, updateOrder } from '@/api/order'
import { getContractList } from '@/api/contract'
import { getGoodsSelection } from '@/api/goods'

const route = useRoute()
const router = useRouter()
const orderInfoRef = ref(null)
const currentStep = ref(0)
const submitting = ref(false)
const contractLoading = ref(false)
const goodsLibLoading = ref(false)
const goodsDialogVisible = ref(false)

const isEdit = computed(() => !!route.params.id)

const selectedContract = ref(null)
const contractList = ref([])
const goodsLibrary = ref([])
const selectedGoods = ref([])

const formData = reactive({
  contractId: null,
  tradeTerms: '',
  paymentMethod: '',
  remark: '',
  goodsList: []
})

const orderRules = {
  tradeTerms: [{ required: true, message: '请选择贸易条款', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}

const totalAmount = computed(() => {
  return formData.goodsList
    .reduce((sum, item) => sum + (item.quantity || 0) * (item.price || 0), 0)
    .toFixed(2)
})

async function loadContracts() {
  contractLoading.value = true
  try {
    const res = await getContractList({ status: 'EFFECTIVE', pageNum: 1, pageSize: 200 })
    contractList.value = res.data.records || res.data.list || []
  } catch (e) {
    console.error(e)
  } finally {
    contractLoading.value = false
  }
}

async function loadGoodsLibrary() {
  goodsLibLoading.value = true
  try {
    const res = await getGoodsSelection()
    goodsLibrary.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    goodsLibLoading.value = false
  }
}

async function loadOrder() {
  if (!route.params.id) return
  try {
    const res = await getOrderById(route.params.id)
    const order = res.data
    formData.contractId = order.contractId
    formData.tradeTerms = order.tradeTerms
    formData.paymentMethod = order.paymentMethod
    formData.remark = order.remark
    formData.goodsList = order.goodsList || order.orderGoods || []
    if (order.contractId) {
      selectedContract.value = contractList.value.find(c => c.id === order.contractId) || { contractNo: order.contractNo }
    }
  } catch (e) {
    ElMessage.error('加载订单信息失败')
    console.error(e)
  }
}

function handleContractSelect(row) {
  selectedContract.value = row
  formData.contractId = row?.id || null
}

async function handleNext() {
  if (currentStep.value === 0) {
    if (!selectedContract.value) {
      ElMessage.warning('请先选择一份合同')
      return
    }
  }
  if (currentStep.value === 1) {
    try {
      await orderInfoRef.value.validate()
    } catch {
      return
    }
  }
  currentStep.value++
  if (currentStep.value === 2 && goodsLibrary.value.length === 0) {
    loadGoodsLibrary()
  }
}

function handleGoodsSelection(rows) {
  selectedGoods.value = rows
}

function confirmGoodsSelection() {
  const existingIds = new Set(formData.goodsList.map(g => g.goodsId || g.id))
  const newItems = selectedGoods.value
    .filter(g => !existingIds.has(g.id))
    .map(g => ({
      goodsId: g.id,
      goodsName: g.name,
      goodsNo: g.goodsNo,
      hsCode: g.hsCode,
      quantity: 1,
      unit: g.unit,
      price: g.price || 0
    }))
  formData.goodsList.push(...newItems)
  goodsDialogVisible.value = false
  if (newItems.length > 0) {
    ElMessage.success(`已添加 ${newItems.length} 条货物`)
  } else {
    ElMessage.info('所选货物已在清单中')
  }
}

async function handleSubmit() {
  if (formData.goodsList.length === 0) {
    ElMessage.warning('请至少添加一条货物')
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...formData,
      totalAmount: totalAmount.value,
      currency: selectedContract.value?.currency || 'CNY'
    }
    if (isEdit.value) {
      await updateOrder(route.params.id, payload)
    } else {
      await createOrder(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    router.push('/orders')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadContracts()
  if (isEdit.value) {
    await loadOrder()
  }
})
</script>

<style scoped>
.order-form {
  padding: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.total-amount {
  font-size: 16px;
  color: #303133;
}
.total-amount strong {
  color: #e6a23c;
  font-size: 18px;
}
.bottom-actions {
  margin-top: 24px;
  display: flex;
  gap: 8px;
  justify-content: center;
}
</style>
