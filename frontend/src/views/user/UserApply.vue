<template>
  <div class="user-apply-container">
    <!-- Filter Area -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="所属企业">
          <el-select
            v-model="queryParams.tenantId"
            placeholder="全部企业"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="t in tenantOptions"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button
          type="primary"
          :disabled="selectedIds.length === 0"
          @click="handleBatchApprove"
        >
          批量通过 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="applyList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          :selectable="(row) => row.status === 'PENDING'"
        />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" disable-transitions>
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="所属企业" min-width="160" />
        <el-table-column prop="createTime" label="申请时间" min-width="170" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" disable-transitions>
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" link @click="openRejectDialog(row)">
                拒绝
              </el-button>
            </template>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- Reject Dialog -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申请"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="handleReject">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplyList, approveApply, rejectApply, batchApprove } from '@/api/user'
import { getAllTenants } from '@/api/tenant'

const loading = ref(false)
const applyList = ref([])
const total = ref(0)
const tenantOptions = ref([])
const tableRef = ref(null)
const selectedIds = ref([])
const dateRange = ref(null)

const queryParams = reactive({
  tenantId: '',
  status: '',
  startDate: '',
  endDate: '',
  current: 1,
  size: 20
})

// Reject dialog
const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const currentRejectRow = ref(null)
const rejectForm = reactive({
  reason: ''
})
const rejectRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 2, max: 200, message: '原因长度在2-200个字符之间', trigger: 'blur' }
  ]
}

const roleTagMap = {
  ADMIN: 'danger',
  GUOHE: 'warning',
  ENTERPRISE: ''
}
const roleLabelMap = {
  ADMIN: '系统管理员',
  GUOHE: '国合员工',
  ENTERPRISE: '企业员工'
}
const statusTagMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}
const statusLabelMap = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

function roleTagType(role) {
  return roleTagMap[role] || 'info'
}
function roleLabel(role) {
  return roleLabelMap[role] || role
}
function statusTagType(status) {
  return statusTagMap[status] || 'info'
}
function statusLabel(status) {
  return statusLabelMap[status] || status
}

function handleDateChange(val) {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

async function loadTenants() {
  try {
    const res = await getAllTenants()
    tenantOptions.value = res.data || []
  } catch {
    tenantOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (!params.tenantId) delete params.tenantId
    if (!params.status) delete params.status
    if (!params.startDate) delete params.startDate
    if (!params.endDate) delete params.endDate
    const res = await getApplyList(params)
    applyList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.current = 1
  loadData()
}

function resetQuery() {
  queryParams.tenantId = ''
  queryParams.status = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.current = 1
  queryParams.size = 20
  dateRange.value = null
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id)
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确认通过用户「${row.realName || row.username}」的注册申请吗？`,
      '审批确认',
      { type: 'info' }
    )
    await approveApply(row.id)
    ElMessage.success('审批通过')
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('操作失败')
    }
  }
}

function openRejectDialog(row) {
  currentRejectRow.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await rejectApply(currentRejectRow.value.id, rejectForm.reason)
    ElMessage.success('已拒绝该申请')
    rejectDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    rejectLoading.value = false
  }
}

async function handleBatchApprove() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确认批量通过选中的 ${selectedIds.value.length} 条申请吗？`,
      '批量审批确认',
      { type: 'info' }
    )
    await batchApprove(selectedIds.value)
    ElMessage.success('批量审批通过')
    tableRef.value?.clearSelection()
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('批量操作失败')
    }
  }
}

onMounted(() => {
  loadTenants()
  loadData()
})
</script>

<style scoped>
.user-apply-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.table-card {
  margin-bottom: 16px;
}

.table-toolbar {
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #c0c4cc;
}
</style>
