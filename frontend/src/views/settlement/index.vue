<template>
  <div class="settlement-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.settlementNo" placeholder="结算编号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.settlementType" placeholder="结算类型" clearable>
              <el-option label="应收" value="RECEIVABLE" />
              <el-option label="应付" value="PAYABLE" />
              <el-option label="结汇" value="EXCHANGE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.status" placeholder="状态" clearable>
              <el-option label="待处理" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
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
        <el-button type="primary" @click="handleAdd">新增结算</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="settlementNo" label="结算编号" min-width="160" show-overflow-tooltip />
        <el-table-column label="结算类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.settlementType)" size="small">
              {{ typeLabel(row.settlementType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column prop="amount" label="金额" width="120" align="right" />
        <el-table-column prop="exchangeRate" label="汇率" width="100" align="right" />
        <el-table-column prop="rmbAmount" label="人民币金额" width="130" align="right" />
        <el-table-column prop="bankName" label="银行名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="paymentDate" label="收付款日期" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="结算详情" width="650px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="结算编号">{{ currentRow.settlementNo }}</el-descriptions-item>
        <el-descriptions-item label="结算类型">
          <el-tag :type="typeTagType(currentRow.settlementType)" size="small">{{ typeLabel(currentRow.settlementType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="币种">{{ currentRow.currency || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="汇率">{{ currentRow.exchangeRate }}</el-descriptions-item>
        <el-descriptions-item label="人民币金额">{{ currentRow.rmbAmount }}</el-descriptions-item>
        <el-descriptions-item label="银行名称">{{ currentRow.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ currentRow.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收付款日期">{{ currentRow.paymentDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑结算' : '新增结算'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="结算编号" prop="settlementNo">
          <el-input v-model="formData.settlementNo" placeholder="请输入结算编号" />
        </el-form-item>
        <el-form-item label="结算类型" prop="settlementType">
          <el-select v-model="formData.settlementType" placeholder="请选择" style="width: 100%">
            <el-option label="应收" value="RECEIVABLE" />
            <el-option label="应付" value="PAYABLE" />
            <el-option label="结汇" value="EXCHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="币种">
          <el-input v-model="formData.currency" placeholder="请输入币种" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="formData.amount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="汇率">
          <el-input-number v-model="formData.exchangeRate" :precision="4" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人民币金额">
          <el-input-number v-model="formData.rmbAmount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="银行名称">
          <el-input v-model="formData.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="formData.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="收付款日期">
          <el-date-picker v-model="formData.paymentDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSettlementList, createSettlement, updateSettlement, deleteSettlement } from '@/api/settlement'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ settlementNo: '', settlementType: '', status: '', pageNum: 1, pageSize: 20 })

const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formSubmitting = ref(false)

const formData = reactive({
  id: null, settlementNo: '', settlementType: '', currency: '', amount: null,
  exchangeRate: null, rmbAmount: null, bankName: '', bankAccount: '', paymentDate: '', remark: ''
})

const formRules = {
  settlementNo: [{ required: true, message: '请输入结算编号', trigger: 'blur' }],
  settlementType: [{ required: true, message: '请选择结算类型', trigger: 'change' }]
}

const typeLabel = (s) => ({ RECEIVABLE: '应收', PAYABLE: '应付', EXCHANGE: '结汇' }[s] || s)
const typeTagType = (s) => ({ RECEIVABLE: 'success', PAYABLE: 'warning', EXCHANGE: '' }[s] || '')
const statusLabel = (s) => ({ PENDING: '待处理', COMPLETED: '已完成', ABNORMAL: '异常' }[s] || s)
const statusTagType = (s) => ({ PENDING: 'warning', COMPLETED: 'success', ABNORMAL: 'danger' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await getSettlementList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载结算列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { settlementNo: '', settlementType: '', status: '', pageNum: 1 })
  loadData()
}

function handleDetail(row) { currentRow.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false
  Object.keys(formData).forEach(k => { formData[k] = '' })
  formData.id = null
  formData.amount = null
  formData.exchangeRate = null
  formData.rmbAmount = null
  formVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.keys(formData).forEach(k => { formData[k] = row[k] ?? '' })
  formVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  formSubmitting.value = true
  try {
    if (isEdit.value) {
      await updateSettlement({ ...formData })
      ElMessage.success('更新成功')
    } else {
      await createSettlement({ ...formData })
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    formSubmitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该结算记录？', '提示', { type: 'warning' })
  await deleteSettlement(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(() => loadData())
</script>

<style scoped>
.settlement-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
