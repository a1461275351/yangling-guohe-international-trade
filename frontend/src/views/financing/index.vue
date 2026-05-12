<template>
  <div class="financing-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.financingNo" placeholder="融资编号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.financingType" placeholder="融资类型" clearable>
              <el-option label="订单贷" value="ORDER_LOAN" />
              <el-option label="信用贷" value="CREDIT_LOAN" />
              <el-option label="税银贷" value="TAX_LOAN" />
              <el-option label="保单融资" value="POLICY_LOAN" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.status" placeholder="状态" clearable>
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已申请" value="APPLIED" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已放款" value="DISBURSED" />
              <el-option label="已还款" value="REPAID" />
              <el-option label="逾期" value="OVERDUE" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增融资</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="financingNo" label="融资编号" min-width="160" show-overflow-tooltip />
        <el-table-column label="融资类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.financingType)" size="small">
              {{ typeLabel(row.financingType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bankName" label="银行名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyAmount" label="申请金额" width="120" align="right" />
        <el-table-column prop="approvedAmount" label="批准金额" width="120" align="right" />
        <el-table-column prop="interestRate" label="利率(%)" width="100" align="right" />
        <el-table-column prop="applyDate" label="申请日期" width="120" />
        <el-table-column prop="maturityDate" label="到期日期" width="120" />
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
    <el-dialog v-model="detailVisible" title="融资详情" width="650px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="融资编号">{{ currentRow.financingNo }}</el-descriptions-item>
        <el-descriptions-item label="融资类型">
          <el-tag :type="typeTagType(currentRow.financingType)" size="small">{{ typeLabel(currentRow.financingType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="银行名称">{{ currentRow.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ currentRow.applyAmount }}</el-descriptions-item>
        <el-descriptions-item label="批准金额">{{ currentRow.approvedAmount }}</el-descriptions-item>
        <el-descriptions-item label="利率(%)">{{ currentRow.interestRate }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ currentRow.applyDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ currentRow.maturityDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑融资' : '新增融资'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="融资编号" prop="financingNo">
          <el-input v-model="formData.financingNo" placeholder="请输入融资编号" />
        </el-form-item>
        <el-form-item label="融资类型" prop="financingType">
          <el-select v-model="formData.financingType" placeholder="请选择" style="width: 100%">
            <el-option label="订单贷" value="ORDER_LOAN" />
            <el-option label="信用贷" value="CREDIT_LOAN" />
            <el-option label="税银贷" value="TAX_LOAN" />
            <el-option label="保单融资" value="POLICY_LOAN" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="银行名称">
          <el-input v-model="formData.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="申请金额">
          <el-input-number v-model="formData.applyAmount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="利率(%)">
          <el-input-number v-model="formData.interestRate" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker v-model="formData.applyDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="formData.maturityDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
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
import { getFinancingList, createFinancing, updateFinancing, deleteFinancing } from '@/api/financing'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ financingNo: '', financingType: '', status: '', pageNum: 1, pageSize: 20 })

const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formSubmitting = ref(false)

const formData = reactive({
  id: null, financingNo: '', financingType: '', bankName: '', applyAmount: null,
  interestRate: null, applyDate: '', maturityDate: '', remark: ''
})

const formRules = {
  financingNo: [{ required: true, message: '请输入融资编号', trigger: 'blur' }],
  financingType: [{ required: true, message: '请选择融资类型', trigger: 'change' }]
}

const typeLabel = (s) => ({ ORDER_LOAN: '订单贷', CREDIT_LOAN: '信用贷', TAX_LOAN: '税银贷', POLICY_LOAN: '保单融资', OTHER: '其他' }[s] || s)
const typeTagType = (s) => ({ ORDER_LOAN: 'success', CREDIT_LOAN: '', TAX_LOAN: 'warning', POLICY_LOAN: '', OTHER: 'info' }[s] || '')
const statusLabel = (s) => ({ DRAFT: '草稿', APPLIED: '已申请', APPROVED: '已批准', DISBURSED: '已放款', REPAID: '已还款', OVERDUE: '逾期', REJECTED: '已拒绝' }[s] || s)
const statusTagType = (s) => ({ DRAFT: 'info', APPLIED: '', APPROVED: 'success', DISBURSED: 'success', REPAID: '', OVERDUE: 'danger', REJECTED: 'danger' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await getFinancingList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载融资列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { financingNo: '', financingType: '', status: '', pageNum: 1 })
  loadData()
}

function handleDetail(row) { currentRow.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false
  Object.keys(formData).forEach(k => { formData[k] = '' })
  formData.id = null
  formData.applyAmount = null
  formData.interestRate = null
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
      await updateFinancing({ ...formData })
      ElMessage.success('更新成功')
    } else {
      await createFinancing({ ...formData })
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
  await ElMessageBox.confirm('确认删除该融资记录？', '提示', { type: 'warning' })
  await deleteFinancing(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(() => loadData())
</script>

<style scoped>
.financing-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
