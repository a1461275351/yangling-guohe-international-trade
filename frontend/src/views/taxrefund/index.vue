<template>
  <div class="taxrefund-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.refundNo" placeholder="退税编号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.status" placeholder="状态" clearable>
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="审核中" value="REVIEWING" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已退税" value="REFUNDED" />
              <el-option label="暂缓" value="SUSPENDED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.riskFlag" placeholder="风险标识" clearable>
              <el-option label="正常" value="NORMAL" />
              <el-option label="疑点" value="SUSPICIOUS" />
              <el-option label="不予退税" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增退税</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="refundNo" label="退税编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="invoiceNo" label="发票号码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="invoiceAmount" label="发票金额" width="120" align="right" />
        <el-table-column prop="refundRate" label="退税率(%)" width="100" align="right" />
        <el-table-column prop="refundAmount" label="退税金额" width="120" align="right" />
        <el-table-column prop="actualRefund" label="实退金额" width="120" align="right" />
        <el-table-column prop="applyDate" label="申请日期" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险标识" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskFlag)" size="small">
              {{ riskLabel(row.riskFlag) }}
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
    <el-dialog v-model="detailVisible" title="退税详情" width="650px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="退税编号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ currentRow.invoiceNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票金额">{{ currentRow.invoiceAmount }}</el-descriptions-item>
        <el-descriptions-item label="退税率(%)">{{ currentRow.refundRate }}</el-descriptions-item>
        <el-descriptions-item label="退税金额">{{ currentRow.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="实退金额">{{ currentRow.actualRefund }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ currentRow.applyDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险标识">
          <el-tag :type="riskTagType(currentRow.riskFlag)" size="small">{{ riskLabel(currentRow.riskFlag) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑退税' : '新增退税'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="退税编号" prop="refundNo">
          <el-input v-model="formData.refundNo" placeholder="请输入退税编号" />
        </el-form-item>
        <el-form-item label="发票号码">
          <el-input v-model="formData.invoiceNo" placeholder="请输入发票号码" />
        </el-form-item>
        <el-form-item label="发票金额">
          <el-input-number v-model="formData.invoiceAmount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退税率">
          <el-input-number v-model="formData.refundRate" :precision="2" :min="0" :max="100" style="width: 100%">
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="退税金额">
          <el-input-number v-model="formData.refundAmount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker v-model="formData.applyDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="风险标识">
          <el-select v-model="formData.riskFlag" placeholder="请选择" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="疑点" value="SUSPICIOUS" />
            <el-option label="不予退税" value="REJECTED" />
          </el-select>
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
import { getTaxRefundList, createTaxRefund, updateTaxRefund, deleteTaxRefund } from '@/api/taxRefund'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ refundNo: '', status: '', riskFlag: '', pageNum: 1, pageSize: 20 })

const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formSubmitting = ref(false)

const formData = reactive({
  id: null, refundNo: '', invoiceNo: '', invoiceAmount: null, refundRate: null,
  refundAmount: null, applyDate: '', riskFlag: '', remark: ''
})

const formRules = {
  refundNo: [{ required: true, message: '请输入退税编号', trigger: 'blur' }]
}

const statusLabel = (s) => ({ DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', APPROVED: '已批准', REJECTED: '已驳回', REFUNDED: '已退税', SUSPENDED: '暂缓' }[s] || s)
const statusTagType = (s) => ({ DRAFT: 'info', SUBMITTED: '', REVIEWING: 'warning', APPROVED: 'success', REJECTED: 'danger', REFUNDED: 'success', SUSPENDED: 'info' }[s] || '')
const riskLabel = (s) => ({ NORMAL: '正常', SUSPICIOUS: '疑点', REJECTED: '不予退税' }[s] || s)
const riskTagType = (s) => ({ NORMAL: 'success', SUSPICIOUS: 'warning', REJECTED: 'danger' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await getTaxRefundList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载退税列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { refundNo: '', status: '', riskFlag: '', pageNum: 1 })
  loadData()
}

function handleDetail(row) { currentRow.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false
  Object.keys(formData).forEach(k => { formData[k] = '' })
  formData.id = null
  formData.invoiceAmount = null
  formData.refundRate = null
  formData.refundAmount = null
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
      await updateTaxRefund({ ...formData })
      ElMessage.success('更新成功')
    } else {
      await createTaxRefund({ ...formData })
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
  await ElMessageBox.confirm('确认删除该退税记录？', '提示', { type: 'warning' })
  await deleteTaxRefund(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(() => loadData())
</script>

<style scoped>
.taxrefund-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
