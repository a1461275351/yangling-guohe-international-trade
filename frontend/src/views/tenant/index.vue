<template>
  <div class="tenant-container">
    <!-- Filter Area -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="企业名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="企业名称/企业号"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
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
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" @click="openDialog(null)">添加租户</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tenantList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="tenantCode" label="企业号" min-width="120" />
        <el-table-column prop="name" label="企业名称" min-width="200" />
        <el-table-column prop="creditCode" label="统一社会信用代码" min-width="200" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :before-change="() => beforeStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
        <el-table-column prop="createTime" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑租户' : '添加租户'"
      width="580px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="140px"
      >
        <el-form-item label="企业号" prop="tenantCode">
          <el-input
            v-model="form.tenantCode"
            placeholder="请输入企业号"
            :disabled="isEdit"
            maxlength="32"
          />
        </el-form-item>
        <el-form-item label="企业名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入企业名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" maxlength="18" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTenantList,
  createTenant,
  updateTenant,
  updateTenantStatus,
  deleteTenant
} from '@/api/tenant'

const loading = ref(false)
const tenantList = ref([])
const total = ref(0)
const dateRange = ref(null)

const queryParams = reactive({
  keyword: '',
  status: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 20
})

// Dialog state
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = {
  id: null,
  tenantCode: '',
  name: '',
  creditCode: '',
  contactPerson: '',
  contactPhone: '',
  address: ''
}

const form = reactive({ ...defaultForm })

const formRules = {
  tenantCode: [
    { required: true, message: '请输入企业号', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在2-32个字符之间', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '统一社会信用代码长度为18位', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
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

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (!params.keyword) delete params.keyword
    if (!params.status) delete params.status
    if (!params.startDate) delete params.startDate
    if (!params.endDate) delete params.endDate
    const res = await getTenantList(params)
    tenantList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载租户列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.pageNum = 1
  queryParams.pageSize = 20
  dateRange.value = null
  loadData()
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      id: row.id,
      tenantCode: row.tenantCode,
      name: row.name,
      creditCode: row.creditCode,
      contactPerson: row.contactPerson,
      contactPhone: row.contactPhone,
      address: row.address || ''
    })
  } else {
    isEdit.value = false
    Object.assign(form, { ...defaultForm })
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { ...defaultForm })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateTenant(data)
      ElMessage.success('更新成功')
    } else {
      delete data.id
      await createTenant(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

function beforeStatusChange(row) {
  const newVal = row.status === 1 ? 0 : 1
  return new Promise((resolve, reject) => {
    ElMessageBox.confirm(
      `确认要${newVal === 1 ? '启用' : '禁用'}企业「${row.name}」吗？`,
      '提示',
      { type: 'warning' }
    ).then(() => {
      return updateTenantStatus(row.id, newVal)
    }).then(() => {
      ElMessage.success('状态更新成功')
      loadData()
      resolve(true)
    }).catch((err) => {
      if (err !== 'cancel' && err !== 'close') {
        ElMessage.error('状态更新失败')
      }
      reject()
    })
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认要删除企业「${row.name}」吗？该操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    await ElMessageBox.confirm(
      `再次确认：删除企业「${row.name}」将同时清除该企业下的所有关联数据（用户、订单、合同等），是否继续？`,
      '二次确认',
      { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteTenant(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tenant-container {
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
</style>
