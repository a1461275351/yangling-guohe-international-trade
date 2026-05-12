<template>
  <div class="logistics-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.logisticsNo" placeholder="物流单号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.transportMode" placeholder="运输方式" clearable>
              <el-option label="海运" value="SEA" />
              <el-option label="空运" value="AIR" />
              <el-option label="铁路" value="RAIL" />
              <el-option label="公路" value="ROAD" />
              <el-option label="多式联运" value="MULTIMODAL" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.status" placeholder="状态" clearable>
              <el-option label="待发运" value="PENDING" />
              <el-option label="运输中" value="IN_TRANSIT" />
              <el-option label="已到港" value="ARRIVED" />
              <el-option label="已签收" value="DELIVERED" />
              <el-option label="异常" value="ABNORMAL" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增物流单</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="logisticsNo" label="物流单号" min-width="150" show-overflow-tooltip />
        <el-table-column label="运输方式" width="100" align="center">
          <template #default="{ row }">{{ modeLabel(row.transportMode) }}</template>
        </el-table-column>
        <el-table-column prop="departurePort" label="起运港" width="120" show-overflow-tooltip />
        <el-table-column prop="destinationPort" label="目的港" width="120" show-overflow-tooltip />
        <el-table-column prop="vesselVoyage" label="船名航次" min-width="150" show-overflow-tooltip />
        <el-table-column prop="blNo" label="提单号" width="140" show-overflow-tooltip />
        <el-table-column prop="logisticsProvider" label="物流商" width="150" show-overflow-tooltip />
        <el-table-column label="冷链" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isColdChain" type="primary" size="small">冷链</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="etd" label="预计离港" width="110" />
        <el-table-column prop="eta" label="预计到港" width="110" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
        :page-sizes="[20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="detailVisible" title="物流详情" width="700px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="物流单号">{{ currentRow.logisticsNo }}</el-descriptions-item>
        <el-descriptions-item label="运输方式">{{ modeLabel(currentRow.transportMode) }}</el-descriptions-item>
        <el-descriptions-item label="起运港">{{ currentRow.departurePort }}</el-descriptions-item>
        <el-descriptions-item label="目的港">{{ currentRow.destinationPort }}</el-descriptions-item>
        <el-descriptions-item label="船名航次">{{ currentRow.vesselVoyage }}</el-descriptions-item>
        <el-descriptions-item label="提单号">{{ currentRow.blNo }}</el-descriptions-item>
        <el-descriptions-item label="运单号">{{ currentRow.waybillNo }}</el-descriptions-item>
        <el-descriptions-item label="物流商">{{ currentRow.logisticsProvider }}</el-descriptions-item>
        <el-descriptions-item label="运费">{{ currentRow.freightAmount }} {{ currentRow.freightCurrency }}</el-descriptions-item>
        <el-descriptions-item label="保险金额">{{ currentRow.insuranceAmount }}</el-descriptions-item>
        <el-descriptions-item label="预计离港">{{ currentRow.etd }}</el-descriptions-item>
        <el-descriptions-item label="预计到港">{{ currentRow.eta }}</el-descriptions-item>
        <el-descriptions-item label="实际离港">{{ currentRow.actualDeparture }}</el-descriptions-item>
        <el-descriptions-item label="实际到港">{{ currentRow.actualArrival }}</el-descriptions-item>
        <el-descriptions-item label="冷链">{{ currentRow.isColdChain ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="温控范围">{{ currentRow.temperatureRange }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(currentRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑物流单' : '新增物流单'" width="750px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物流单号" prop="logisticsNo"><el-input v-model="formData.logisticsNo" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运输方式" prop="transportMode">
              <el-select v-model="formData.transportMode" style="width:100%">
                <el-option label="海运" value="SEA" /><el-option label="空运" value="AIR" />
                <el-option label="铁路" value="RAIL" /><el-option label="公路" value="ROAD" />
                <el-option label="多式联运" value="MULTIMODAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="起运港"><el-input v-model="formData.departurePort" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目的港"><el-input v-model="formData.destinationPort" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="船名航次"><el-input v-model="formData.vesselVoyage" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提单号"><el-input v-model="formData.blNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="运单号"><el-input v-model="formData.waybillNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物流商"><el-input v-model="formData.logisticsProvider" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="运费"><el-input-number v-model="formData.freightAmount" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="运费币种"><el-input v-model="formData.freightCurrency" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="预计离港"><el-date-picker v-model="formData.etd" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预计到港"><el-date-picker v-model="formData.eta" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="冷链运输">
              <el-radio-group v-model="formData.isColdChain">
                <el-radio :value="1">是</el-radio><el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="温控范围"><el-input v-model="formData.temperatureRange" placeholder="如 2-8℃" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogisticsList, createLogistics, updateLogistics, deleteLogistics } from '@/api/logistics'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ logisticsNo: '', transportMode: '', status: '', current: 1, size: 20 })
const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formData = reactive({
  id: null, logisticsNo: '', transportMode: '', departurePort: '', destinationPort: '', vesselVoyage: '',
  blNo: '', waybillNo: '', logisticsProvider: '', freightAmount: null, freightCurrency: 'USD',
  insuranceAmount: null, etd: '', eta: '', isColdChain: 0, temperatureRange: '', remark: ''
})
const formRules = {
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  transportMode: [{ required: true, message: '请选择运输方式', trigger: 'change' }]
}

const modeLabel = (s) => ({ SEA: '海运', AIR: '空运', RAIL: '铁路', ROAD: '公路', MULTIMODAL: '多式联运' }[s] || s)
const statusLabel = (s) => ({ PENDING: '待发运', IN_TRANSIT: '运输中', ARRIVED: '已到港', DELIVERED: '已签收', ABNORMAL: '异常' }[s] || s)
const statusTagType = (s) => ({ PENDING: 'info', IN_TRANSIT: '', ARRIVED: 'success', DELIVERED: 'success', ABNORMAL: 'danger' }[s] || '')

async function loadData() {
  loading.value = true
  try { const res = await getLogisticsList(queryParams); tableData.value = res.data?.records || []; total.value = res.data?.total || 0 }
  catch (e) { console.warn('加载物流列表失败', e) }
  finally { loading.value = false }
}
function handleSearch() { queryParams.current = 1; loadData() }
function handleReset() { Object.assign(queryParams, { logisticsNo: '', transportMode: '', status: '', current: 1 }); loadData() }
function handleDetail(row) { currentRow.value = row; detailVisible.value = true }
function handleAdd() { isEdit.value = false; Object.keys(formData).forEach(k => formData[k] = k === 'isColdChain' ? 0 : k === 'freightCurrency' ? 'USD' : null); formData.id = null; formVisible.value = true }
function handleEdit(row) { isEdit.value = true; Object.keys(formData).forEach(k => { formData[k] = row[k] ?? '' }); formVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); if (isEdit.value) { await updateLogistics(formData); ElMessage.success('更新成功') } else { await createLogistics(formData); ElMessage.success('创建成功') }; formVisible.value = false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm(`确认删除 ${row.logisticsNo}？`, '提示', { type: 'warning' }); await deleteLogistics(row.id); ElMessage.success('删除成功'); loadData() }
function resetForm() { formRef.value?.resetFields() }
onMounted(() => loadData())
</script>

<style scoped>
.logistics-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
