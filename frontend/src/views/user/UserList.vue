<template>
  <div class="user-list-container">
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
        <el-form-item label="状态">
          <el-radio-group v-model="queryParams.status" @change="handleQuery">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.keyword"
            placeholder="用户名/姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
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
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="tenantName" label="所属企业" min-width="160" />
        <el-table-column prop="role" label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" disable-transitions>
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :before-change="() => beforeStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="warning"
              link
              @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, resetUserPassword } from '@/api/user'
import { getAllTenants } from '@/api/tenant'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const tenantOptions = ref([])

const queryParams = reactive({
  tenantId: '',
  status: '',
  keyword: '',
  pageNum: 1,
  pageSize: 20
})

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

function roleTagType(role) {
  return roleTagMap[role] || 'info'
}

function roleLabel(role) {
  return roleLabelMap[role] || role
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
    if (!params.keyword) delete params.keyword
    const res = await getUserList(params)
    userList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.tenantId = ''
  queryParams.status = ''
  queryParams.keyword = ''
  queryParams.pageNum = 1
  queryParams.pageSize = 20
  loadData()
}

function beforeStatusChange(row) {
  const newVal = row.status === 1 ? 0 : 1
  return new Promise((resolve, reject) => {
    ElMessageBox.confirm(
      `确认要${newVal === 1 ? '启用' : '禁用'}用户「${row.username}」吗？`,
      '提示',
      { type: 'warning' }
    ).then(() => {
      return updateUserStatus(row.id, newVal)
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

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认要重置用户「${row.username}」的密码吗？重置后密码将恢复为默认密码。`,
      '重置密码确认',
      { type: 'warning' }
    )
    await resetUserPassword(row.id)
    ElMessage.success('密码重置成功')
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('密码重置失败')
    }
  }
}

onMounted(() => {
  loadTenants()
  loadData()
})
</script>

<style scoped>
.user-list-container {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
