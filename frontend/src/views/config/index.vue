<template>
  <div class="config-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: Config Items -->
      <el-tab-pane label="配置项" name="items">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openItemDialog(null)">添加配置项</el-button>
          <el-input
            v-model="itemQuery.keyword"
            placeholder="搜索配置项名称/编码"
            clearable
            style="width: 240px; margin-left: 12px"
            @keyup.enter="loadItems"
          />
          <el-button style="margin-left: 8px" @click="loadItems">查询</el-button>
        </div>

        <el-table
          v-loading="itemLoading"
          :data="itemList"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="code" label="编码" min-width="150" />
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="ACTIVE"
                inactive-value="DISABLED"
                @change="(val) => handleItemStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openItemDialog(row)">编辑</el-button>
              <el-button type="success" link @click="viewItemValues(row)">查看配置值</el-button>
              <el-button type="danger" link @click="handleDeleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="itemQuery.pageNum"
            v-model:page-size="itemQuery.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="itemTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadItems"
            @current-change="loadItems"
          />
        </div>
      </el-tab-pane>

      <!-- Tab 2: Config Values -->
      <el-tab-pane label="配置值" name="values">
        <div class="tab-toolbar">
          <el-select
            v-model="valueQuery.configItemId"
            placeholder="请选择配置项"
            clearable
            filterable
            style="width: 240px"
            @change="loadValues"
          >
            <el-option
              v-for="item in allItems"
              :key="item.id"
              :label="`${item.name} (${item.code})`"
              :value="item.id"
            />
          </el-select>
          <el-button
            type="primary"
            style="margin-left: 12px"
            :disabled="!valueQuery.configItemId"
            @click="openValueDialog(null)"
          >
            添加配置值
          </el-button>
          <el-button
            :disabled="selectedValueIds.length === 0"
            @click="handleBatchValueStatus('ACTIVE')"
          >
            批量启用
          </el-button>
          <el-button
            :disabled="selectedValueIds.length === 0"
            @click="handleBatchValueStatus('DISABLED')"
          >
            批量禁用
          </el-button>
        </div>

        <el-table
          v-loading="valueLoading"
          :data="valueList"
          stripe
          border
          style="width: 100%"
          @selection-change="handleValueSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="code" label="编码" min-width="150" />
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="ACTIVE"
                inactive-value="DISABLED"
                @change="(val) => handleValueStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openValueDialog(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDeleteValue(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="valueQuery.pageNum"
            v-model:page-size="valueQuery.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="valueTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadValues"
            @current-change="loadValues"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Config Item Dialog -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="isItemEdit ? '编辑配置项' : '添加配置项'"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetItemForm"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemFormRules"
        label-width="100px"
      >
        <el-form-item label="编码" prop="code">
          <el-input
            v-model="itemForm.code"
            placeholder="请输入编码"
            :disabled="isItemEdit"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="itemForm.name" placeholder="请输入名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="itemForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitLoading" @click="handleItemSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- Config Value Dialog -->
    <el-dialog
      v-model="valueDialogVisible"
      :title="isValueEdit ? '编辑配置值' : '添加配置值'"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetValueForm"
    >
      <el-form
        ref="valueFormRef"
        :model="valueForm"
        :rules="valueFormRules"
        label-width="100px"
      >
        <el-form-item label="编码" prop="code">
          <el-input
            v-model="valueForm.code"
            placeholder="请输入编码"
            :disabled="isValueEdit"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="valueForm.name" placeholder="请输入名称" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="valueDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="valueSubmitLoading" @click="handleValueSubmit">
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
  getConfigItemList,
  createConfigItem,
  updateConfigItem,
  deleteConfigItem,
  updateConfigItemStatus,
  getConfigValueList,
  createConfigValue,
  updateConfigValue,
  deleteConfigValue,
  updateConfigValueStatus,
  batchUpdateValueStatus
} from '@/api/config'

const activeTab = ref('items')

// ============ Config Items ============
const itemLoading = ref(false)
const itemList = ref([])
const itemTotal = ref(0)
const allItems = ref([])

const itemQuery = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 20
})

const itemDialogVisible = ref(false)
const isItemEdit = ref(false)
const itemSubmitLoading = ref(false)
const itemFormRef = ref(null)

const defaultItemForm = { id: null, code: '', name: '', description: '' }
const itemForm = reactive({ ...defaultItemForm })

const itemFormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ]
}

async function loadItems() {
  itemLoading.value = true
  try {
    const params = { ...itemQuery }
    if (!params.keyword) delete params.keyword
    const res = await getConfigItemList(params)
    itemList.value = res.data?.records || res.data?.list || []
    itemTotal.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载配置项列表失败')
  } finally {
    itemLoading.value = false
  }
}

async function loadAllItems() {
  try {
    const res = await getConfigItemList({ pageNum: 1, pageSize: 999 })
    allItems.value = res.data?.records || res.data?.list || []
  } catch {
    allItems.value = []
  }
}

function openItemDialog(row) {
  if (row) {
    isItemEdit.value = true
    Object.assign(itemForm, {
      id: row.id,
      code: row.code,
      name: row.name,
      description: row.description || ''
    })
  } else {
    isItemEdit.value = false
    Object.assign(itemForm, { ...defaultItemForm })
  }
  itemDialogVisible.value = true
}

function resetItemForm() {
  itemFormRef.value?.resetFields()
  Object.assign(itemForm, { ...defaultItemForm })
}

async function handleItemSubmit() {
  const valid = await itemFormRef.value.validate().catch(() => false)
  if (!valid) return

  itemSubmitLoading.value = true
  try {
    const data = { ...itemForm }
    if (isItemEdit.value) {
      await updateConfigItem(data)
      ElMessage.success('更新成功')
    } else {
      delete data.id
      await createConfigItem(data)
      ElMessage.success('添加成功')
    }
    itemDialogVisible.value = false
    loadItems()
    loadAllItems()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    itemSubmitLoading.value = false
  }
}

async function handleItemStatusChange(row, val) {
  try {
    await updateConfigItemStatus(row.id, val)
    ElMessage.success('状态更新成功')
  } catch {
    row.status = val === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.error('状态更新失败')
  }
}

async function handleDeleteItem(row) {
  try {
    await ElMessageBox.confirm(
      `确认要删除配置项「${row.name}」吗？该操作将同时删除其下所有配置值。`,
      '删除确认',
      { type: 'warning' }
    )
    await deleteConfigItem(row.id)
    ElMessage.success('删除成功')
    loadItems()
    loadAllItems()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

function viewItemValues(row) {
  activeTab.value = 'values'
  valueQuery.configItemId = row.id
  loadValues()
}

// ============ Config Values ============
const valueLoading = ref(false)
const valueList = ref([])
const valueTotal = ref(0)
const selectedValueIds = ref([])

const valueQuery = reactive({
  configItemId: '',
  pageNum: 1,
  pageSize: 20
})

const valueDialogVisible = ref(false)
const isValueEdit = ref(false)
const valueSubmitLoading = ref(false)
const valueFormRef = ref(null)

const defaultValueForm = { id: null, configItemId: '', code: '', name: '' }
const valueForm = reactive({ ...defaultValueForm })

const valueFormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ]
}

async function loadValues() {
  if (!valueQuery.configItemId) {
    valueList.value = []
    valueTotal.value = 0
    return
  }
  valueLoading.value = true
  try {
    const params = { ...valueQuery }
    const res = await getConfigValueList(params)
    valueList.value = res.data?.records || res.data?.list || []
    valueTotal.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载配置值列表失败')
  } finally {
    valueLoading.value = false
  }
}

function handleValueSelectionChange(rows) {
  selectedValueIds.value = rows.map((r) => r.id)
}

function openValueDialog(row) {
  if (row) {
    isValueEdit.value = true
    Object.assign(valueForm, {
      id: row.id,
      configItemId: row.configItemId,
      code: row.code,
      name: row.name
    })
  } else {
    isValueEdit.value = false
    Object.assign(valueForm, {
      ...defaultValueForm,
      configItemId: valueQuery.configItemId
    })
  }
  valueDialogVisible.value = true
}

function resetValueForm() {
  valueFormRef.value?.resetFields()
  Object.assign(valueForm, { ...defaultValueForm })
}

async function handleValueSubmit() {
  const valid = await valueFormRef.value.validate().catch(() => false)
  if (!valid) return

  valueSubmitLoading.value = true
  try {
    const data = { ...valueForm }
    if (isValueEdit.value) {
      await updateConfigValue(data)
      ElMessage.success('更新成功')
    } else {
      delete data.id
      await createConfigValue(data)
      ElMessage.success('添加成功')
    }
    valueDialogVisible.value = false
    loadValues()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    valueSubmitLoading.value = false
  }
}

async function handleValueStatusChange(row, val) {
  try {
    await updateConfigValueStatus(row.id, val)
    ElMessage.success('状态更新成功')
  } catch {
    row.status = val === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.error('状态更新失败')
  }
}

async function handleDeleteValue(row) {
  try {
    await ElMessageBox.confirm(
      `确认要删除配置值「${row.name}」吗？`,
      '删除确认',
      { type: 'warning' }
    )
    await deleteConfigValue(row.id)
    ElMessage.success('删除成功')
    loadValues()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleBatchValueStatus(status) {
  if (selectedValueIds.value.length === 0) return
  const label = status === 'ACTIVE' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确认要批量${label}选中的 ${selectedValueIds.value.length} 条配置值吗？`,
      '批量操作确认',
      { type: 'info' }
    )
    await batchUpdateValueStatus(selectedValueIds.value, status)
    ElMessage.success(`批量${label}成功`)
    loadValues()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('批量操作失败')
    }
  }
}

onMounted(() => {
  loadItems()
  loadAllItems()
})
</script>

<style scoped>
.config-container {
  padding: 20px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
