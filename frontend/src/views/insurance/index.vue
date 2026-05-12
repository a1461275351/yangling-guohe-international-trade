<template>
  <div class="insurance-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.policyNo" placeholder="保单号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="queryParams.buyerCountry" placeholder="买方国家" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.status" placeholder="状态" clearable>
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已申请" value="APPLIED" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="生效中" value="ACTIVE" />
              <el-option label="理赔中" value="CLAIMED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增信保</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="policyNo" label="保单号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="buyerName" label="买方名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="buyerCountry" label="买方国家" width="110" />
        <el-table-column prop="creditLimit" label="信用额度" width="120" align="right" />
        <el-table-column prop="insuredAmount" label="投保金额" width="120" align="right" />
        <el-table-column prop="premium" label="保费" width="100" align="right" />
        <el-table-column prop="coverageStart" label="保险起期" width="120" />
        <el-table-column prop="coverageEnd" label="保险止期" width="120" />
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
    <el-dialog v-model="detailVisible" title="信保详情" width="650px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="保单号">{{ currentRow.policyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买方名称">{{ currentRow.buyerName }}</el-descriptions-item>
        <el-descriptions-item label="买方国家">{{ currentRow.buyerCountry }}</el-descriptions-item>
        <el-descriptions-item label="信用额度">{{ currentRow.creditLimit }}</el-descriptions-item>
        <el-descriptions-item label="投保金额">{{ currentRow.insuredAmount }}</el-descriptions-item>
        <el-descriptions-item label="保费">{{ currentRow.premium }}</el-descriptions-item>
        <el-descriptions-item label="保险起期">{{ currentRow.coverageStart || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保险止期">{{ currentRow.coverageEnd || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑信保' : '新增信保'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="保单号">
          <el-input v-model="formData.policyNo" placeholder="请输入保单号" />
        </el-form-item>
        <el-form-item label="买方名称" prop="buyerName">
          <el-input v-model="formData.buyerName" placeholder="请输入买方名称" />
        </el-form-item>
        <el-form-item label="买方国家" prop="buyerCountry">
          <el-input v-model="formData.buyerCountry" placeholder="请输入买方国家" />
        </el-form-item>
        <el-form-item label="信用额度">
          <el-input-number v-model="formData.creditLimit" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="投保金额">
          <el-input-number v-model="formData.insuredAmount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保费">
          <el-input-number v-model="formData.premium" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保险起期">
          <el-date-picker v-model="formData.coverageStart" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保险止期">
          <el-date-picker v-model="formData.coverageEnd" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
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
import { getInsuranceList, createInsurance, updateInsurance, deleteInsurance } from '@/api/insurance'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ policyNo: '', buyerCountry: '', status: '', pageNum: 1, pageSize: 20 })

const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formSubmitting = ref(false)

const formData = reactive({
  id: null, policyNo: '', buyerName: '', buyerCountry: '', creditLimit: null,
  insuredAmount: null, premium: null, coverageStart: '', coverageEnd: '', remark: ''
})

const formRules = {
  buyerName: [{ required: true, message: '请输入买方名称', trigger: 'blur' }],
  buyerCountry: [{ required: true, message: '请输入买方国家', trigger: 'blur' }]
}

const statusLabel = (s) => ({ DRAFT: '草稿', APPLIED: '已申请', APPROVED: '已批准', ACTIVE: '生效中', CLAIMED: '理赔中', CLOSED: '已关闭' }[s] || s)
const statusTagType = (s) => ({ DRAFT: 'info', APPLIED: '', APPROVED: 'success', ACTIVE: 'success', CLAIMED: 'warning', CLOSED: 'info' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await getInsuranceList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载信保列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { policyNo: '', buyerCountry: '', status: '', pageNum: 1 })
  loadData()
}

function handleDetail(row) { currentRow.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false
  Object.keys(formData).forEach(k => { formData[k] = '' })
  formData.id = null
  formData.creditLimit = null
  formData.insuredAmount = null
  formData.premium = null
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
      await updateInsurance({ ...formData })
      ElMessage.success('更新成功')
    } else {
      await createInsurance({ ...formData })
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
  await ElMessageBox.confirm('确认删除该信保记录？', '提示', { type: 'warning' })
  await deleteInsurance(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(() => loadData())
</script>

<style scoped>
.insurance-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
