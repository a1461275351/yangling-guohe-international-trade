<template>
  <div class="enterprise-management">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryParams.enterpriseName" placeholder="企业名称" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="queryParams.creditCode" placeholder="统一社会信用代码" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.agreementStatus" placeholder="协议状态" clearable>
              <el-option label="未签署" value="UNSIGNED" />
              <el-option label="已签署" value="SIGNED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="queryParams.riskLevel" placeholder="风险等级" clearable>
              <el-option label="低" value="LOW" />
              <el-option label="正常" value="NORMAL" />
              <el-option label="高" value="HIGH" />
              <el-option label="黑名单" value="BLACKLIST" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增服务企业</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="enterpriseName" label="企业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="creditCode" label="统一社会信用代码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="industry" label="行业" width="120" />
        <el-table-column label="首次外贸" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isFirstTrade ? 'success' : 'info'" size="small">
              {{ row.isFirstTrade ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="协议状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="agreementTagType(row.agreementStatus)" size="small">
              {{ agreementLabel(row.agreementStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" size="small">
              {{ riskLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizContactName" label="业务联系人" width="110" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="val => handleStatusChange(row, val)" />
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

      <el-pagination
        class="pagination"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="服务企业详情" width="700px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="企业名称" :span="2">{{ currentRow.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ currentRow.creditCode }}</el-descriptions-item>
        <el-descriptions-item label="地区">{{ currentRow.region }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ currentRow.industry }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">{{ currentRow.productType }}</el-descriptions-item>
        <el-descriptions-item label="首次外贸">{{ currentRow.isFirstTrade ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="服务范围">{{ currentRow.serviceScope }}</el-descriptions-item>
        <el-descriptions-item label="协议编号">{{ currentRow.agreementNo }}</el-descriptions-item>
        <el-descriptions-item label="协议状态">{{ agreementLabel(currentRow.agreementStatus) }}</el-descriptions-item>
        <el-descriptions-item label="服务起始">{{ currentRow.serviceStartDate }}</el-descriptions-item>
        <el-descriptions-item label="服务结束">{{ currentRow.serviceEndDate }}</el-descriptions-item>
        <el-descriptions-item label="业务联系人">{{ currentRow.bizContactName }} {{ currentRow.bizContactPhone }}</el-descriptions-item>
        <el-descriptions-item label="财务联系人">{{ currentRow.finContactName }} {{ currentRow.finContactPhone }}</el-descriptions-item>
        <el-descriptions-item label="单证联系人">{{ currentRow.docContactName }} {{ currentRow.docContactPhone }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ riskLabel(currentRow.riskLevel) }}</el-descriptions-item>
        <el-descriptions-item label="年度服务次数">{{ currentRow.annualServiceCount }}</el-descriptions-item>
        <el-descriptions-item label="年度出口额(万元)">{{ currentRow.annualExportAmount }}</el-descriptions-item>
        <el-descriptions-item label="年度进口额(万元)">{{ currentRow.annualImportAmount }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="formTitle" width="750px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="130px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input v-model="formData.enterpriseName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一信用代码" prop="creditCode">
              <el-input v-model="formData.creditCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属地区">
              <el-input v-model="formData.region" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行业">
              <el-input v-model="formData.industry" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品类型">
              <el-input v-model="formData.productType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="首次外贸">
              <el-radio-group v-model="formData.isFirstTrade">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="协议编号">
              <el-input v-model="formData.agreementNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议状态">
              <el-select v-model="formData.agreementStatus" style="width: 100%">
                <el-option label="未签署" value="UNSIGNED" />
                <el-option label="已签署" value="SIGNED" />
                <el-option label="已过期" value="EXPIRED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务起始日期">
              <el-date-picker v-model="formData.serviceStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务结束日期">
              <el-date-picker v-model="formData.serviceEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="服务范围">
          <el-checkbox-group v-model="serviceScopeList">
            <el-checkbox value="通关">通关</el-checkbox>
            <el-checkbox value="物流">物流</el-checkbox>
            <el-checkbox value="退税">退税</el-checkbox>
            <el-checkbox value="结算">结算</el-checkbox>
            <el-checkbox value="信保">信保</el-checkbox>
            <el-checkbox value="融资">融资</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-divider content-position="left">联系人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务联系人">
              <el-input v-model="formData.bizContactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务电话">
              <el-input v-model="formData.bizContactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="财务联系人">
              <el-input v-model="formData.finContactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="财务电话">
              <el-input v-model="formData.finContactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单证联系人">
              <el-input v-model="formData.docContactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单证电话">
              <el-input v-model="formData.docContactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="风险等级">
              <el-select v-model="formData.riskLevel" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="正常" value="NORMAL" />
                <el-option label="高" value="HIGH" />
                <el-option label="黑名单" value="BLACKLIST" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEnterpriseList, createEnterprise, updateEnterprise, deleteEnterprise, updateEnterpriseStatus } from '@/api/enterprise'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ enterpriseName: '', creditCode: '', agreementStatus: '', riskLevel: '', current: 1, size: 20 })

const detailVisible = ref(false)
const currentRow = ref(null)
const formVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const formTitle = computed(() => isEdit.value ? '编辑服务企业' : '新增服务企业')
const serviceScopeList = ref([])

const formData = reactive({
  id: null, enterpriseName: '', creditCode: '', region: '', industry: '', productType: '',
  isFirstTrade: 0, agreementNo: '', agreementStatus: 'UNSIGNED', serviceStartDate: '', serviceEndDate: '',
  serviceScope: '', bizContactName: '', bizContactPhone: '', finContactName: '', finContactPhone: '',
  docContactName: '', docContactPhone: '', riskLevel: 'NORMAL', remark: ''
})

const formRules = {
  enterpriseName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }]
}

const agreementLabel = (s) => ({ UNSIGNED: '未签署', SIGNED: '已签署', EXPIRED: '已过期' }[s] || s)
const agreementTagType = (s) => ({ UNSIGNED: 'info', SIGNED: 'success', EXPIRED: 'danger' }[s] || '')
const riskLabel = (s) => ({ LOW: '低', NORMAL: '正常', HIGH: '高', BLACKLIST: '黑名单' }[s] || s)
const riskTagType = (s) => ({ LOW: 'success', NORMAL: '', HIGH: 'warning', BLACKLIST: 'danger' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await getEnterpriseList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.warn('加载服务企业列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.current = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, { enterpriseName: '', creditCode: '', agreementStatus: '', riskLevel: '', current: 1 })
  loadData()
}

function handleDetail(row) { currentRow.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false
  Object.keys(formData).forEach(k => formData[k] = k === 'isFirstTrade' ? 0 : k === 'agreementStatus' ? 'UNSIGNED' : k === 'riskLevel' ? 'NORMAL' : '')
  formData.id = null
  serviceScopeList.value = []
  formVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.keys(formData).forEach(k => { formData[k] = row[k] ?? '' })
  serviceScopeList.value = row.serviceScope ? row.serviceScope.split(',') : []
  formVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  formData.serviceScope = serviceScopeList.value.join(',')
  if (isEdit.value) {
    await updateEnterprise(formData)
    ElMessage.success('更新成功')
  } else {
    await createEnterprise(formData)
    ElMessage.success('创建成功')
  }
  formVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除 ${row.enterpriseName}？`, '提示', { type: 'warning' })
  await deleteEnterprise(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleStatusChange(row, val) {
  await updateEnterpriseStatus(row.id, val ? 1 : 0)
  ElMessage.success('状态更新成功')
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(() => loadData())
</script>

<style scoped>
.enterprise-management { padding: 16px; }
.search-bar { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
