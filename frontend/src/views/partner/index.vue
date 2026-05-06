<template>
  <div class="partner-management">
    <el-card shadow="never">
      <div class="partner-layout">
        <!-- Left tabs -->
        <el-tabs v-model="partnerType" tab-position="left" class="partner-tabs" @tab-change="handleTypeChange">
          <el-tab-pane label="供应商" name="SUPPLIER" />
          <el-tab-pane label="客户" name="CUSTOMER" />
        </el-tabs>

        <!-- Right content -->
        <div class="partner-content">
          <!-- Filter -->
          <div class="search-bar">
            <el-form inline>
              <el-form-item>
                <el-input v-model="queryParams.name" placeholder="搜索名称" clearable @keyup.enter="handleSearch" />
              </el-form-item>
              <el-form-item>
                <el-select v-model="queryParams.status" placeholder="状态" clearable @change="handleSearch">
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- Toolbar -->
          <div class="toolbar">
            <el-button type="primary" @click="handleAdd">添加{{ partnerType === 'SUPPLIER' ? '供应商' : '客户' }}</el-button>
            <el-button :disabled="selectedIds.length === 0" @click="handleBatchDisable">
              批量禁用 ({{ selectedIds.length }})
            </el-button>
          </div>

          <!-- Table -->
          <el-table v-loading="loading" :data="tableData" border stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="creditCode" label="统一社会信用代码" min-width="200" show-overflow-tooltip />
            <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
            <el-table-column prop="contactName" label="联系人" width="100" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status === 1"
                  @change="(val) => handleStatusChange(row, val)"
                  inline-prompt
                  active-text="启"
                  inactive-text="禁"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper">
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
        </div>
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="合作方详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentRow.type === 'SUPPLIER' ? '供应商' : '客户' }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码" :span="2">{{ currentRow.creditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentRow.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentRow.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'">
            {{ currentRow.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isFormEdit ? '编辑合作方' : '添加合作方'"
      width="600px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="130px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="formData.creditCode" placeholder="请输入统一社会信用代码" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="formData.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="formData.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县">
          <el-input v-model="formData.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="formData.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPartnerList,
  getPartnerById,
  createPartner,
  updatePartner,
  deletePartner,
  updatePartnerStatus,
  batchUpdatePartnerStatus
} from '@/api/partner'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const partnerType = ref('SUPPLIER')

const detailVisible = ref(false)
const currentRow = ref(null)

const formDialogVisible = ref(false)
const isFormEdit = ref(false)
const formRef = ref(null)
const formSubmitting = ref(false)

const queryParams = reactive({
  name: '',
  status: null,
  type: 'SUPPLIER',
  pageNum: 1,
  pageSize: 20
})

const formData = reactive({
  id: null,
  name: '',
  creditCode: '',
  province: '',
  city: '',
  district: '',
  address: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  type: 'SUPPLIER'
})

const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function handleTypeChange(type) {
  queryParams.type = type
  queryParams.pageNum = 1
  loadData()
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.name = ''
  queryParams.status = null
  queryParams.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPartnerList({ ...queryParams })
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleView(row) {
  currentRow.value = row
  detailVisible.value = true
}

function handleAdd() {
  isFormEdit.value = false
  resetForm()
  formData.type = partnerType.value
  formDialogVisible.value = true
}

async function handleEdit(row) {
  isFormEdit.value = true
  try {
    const res = await getPartnerById(row.id)
    Object.assign(formData, res.data)
  } catch (e) {
    Object.assign(formData, row)
  }
  formDialogVisible.value = true
}

function resetForm() {
  formData.id = null
  formData.name = ''
  formData.creditCode = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.address = ''
  formData.contactName = ''
  formData.contactPhone = ''
  formData.contactEmail = ''
  formRef.value?.clearValidate()
}

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  formSubmitting.value = true
  try {
    if (isFormEdit.value) {
      await updatePartner({ ...formData })
    } else {
      await createPartner({ ...formData })
    }
    ElMessage.success(isFormEdit.value ? '更新成功' : '添加成功')
    formDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    formSubmitting.value = false
  }
}

async function handleStatusChange(row, val) {
  const newStatus = val ? 1 : 0
  try {
    await updatePartnerStatus(row.id, newStatus)
    ElMessage.success(val ? '已启用' : '已禁用')
    row.status = newStatus
  } catch (e) {
    console.error(e)
  }
}

async function handleBatchDisable() {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedIds.value.length} 个合作方吗？`, '提示', { type: 'warning' })
    await batchUpdatePartnerStatus(selectedIds.value, 0)
    ElMessage.success('批量禁用成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该合作方吗？', '提示', { type: 'warning' })
    await deletePartner(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.partner-management {
  padding: 16px;
}
.partner-layout {
  display: flex;
  gap: 16px;
}
.partner-tabs {
  flex-shrink: 0;
}
.partner-content {
  flex: 1;
  min-width: 0;
}
.search-bar {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
