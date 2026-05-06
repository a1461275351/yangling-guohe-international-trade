<template>
  <div style="padding:20px">
    <el-card shadow="never">
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:20px">
        <el-button @click="router.back()">返回</el-button>
        <h3 style="margin:0">{{ isEdit ? '编辑台账' : '新增台账' }}</h3>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width:900px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="主提单号"><el-input v-model="form.masterBlNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="分提单号"><el-input v-model="form.subBlNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="箱案号"><el-input v-model="form.caseNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="供应商名称" prop="supplierName"><el-input v-model="form.supplierName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同协议号"><el-input v-model="form.contractNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="监管方式"><el-input v-model="form.supervisionMode" placeholder="如: 一般贸易" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="申报海关"><el-input v-model="form.declareCustoms" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="进境关别"><el-input v-model="form.entryCustoms" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="启运国"><el-input v-model="form.originCountry" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="经停港"><el-input v-model="form.transitPort" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="入境口岸"><el-input v-model="form.entryPort" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="运输方式"><el-input v-model="form.transportMode" placeholder="如: 海运" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="收货人"><el-input v-model="form.consignee" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="币种"><el-input v-model="form.currency" placeholder="USD" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="进口日期"><el-date-picker v-model="form.ieDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

        <el-divider content-position="left">商品明细</el-divider>
        <el-button type="primary" size="small" style="margin-bottom:12px" @click="addGoods">+ 添加商品</el-button>
        <el-table :data="form.goodsList" border size="small">
          <el-table-column label="序号" width="50" align="center"><template #default="{ $index }">{{ $index + 1 }}</template></el-table-column>
          <el-table-column label="商品名称" min-width="150"><template #default="{ row }"><el-input v-model="row.name" size="small" /></template></el-table-column>
          <el-table-column label="HS编码" width="130"><template #default="{ row }"><el-input v-model="row.hsCode" size="small" /></template></el-table-column>
          <el-table-column label="规格" width="120"><template #default="{ row }"><el-input v-model="row.spec" size="small" /></template></el-table-column>
          <el-table-column label="数量" width="100"><template #default="{ row }"><el-input-number v-model="row.quantity" size="small" :min="0" :controls="false" @change="calcAmount(row)" style="width:80px" /></template></el-table-column>
          <el-table-column label="单位" width="70"><template #default="{ row }"><el-input v-model="row.unit" size="small" /></template></el-table-column>
          <el-table-column label="单价" width="100"><template #default="{ row }"><el-input-number v-model="row.price" size="small" :min="0" :precision="4" :controls="false" @change="calcAmount(row)" style="width:80px" /></template></el-table-column>
          <el-table-column label="金额" width="110"><template #default="{ row }"><span>{{ row.amount ? Number(row.amount).toFixed(2) : '' }}</span></template></el-table-column>
          <el-table-column label="原产国" width="90"><template #default="{ row }"><el-input v-model="row.originCountry" size="small" /></template></el-table-column>
          <el-table-column label="" width="60" align="center">
            <template #default="{ $index }"><el-button type="danger" link size="small" @click="form.goodsList.splice($index, 1)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <div style="text-align:right;margin-top:8px;font-weight:bold;color:#303133">
          合计金额: {{ calcTotal() }}
        </div>

        <div style="margin-top:24px;text-align:center">
          <el-button type="primary" :loading="saving" @click="handleSave" size="large">{{ isEdit ? '保存修改' : '创建台账' }}</el-button>
          <el-button @click="router.back()" size="large">取消</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createLedger, updateLedger, getLedgerById } from '@/api/ledger'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.params.id
const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  id: null, masterBlNo: '', subBlNo: '', caseNo: '', supplierName: '', contractNo: '',
  supervisionMode: '', declareCustoms: '', entryCustoms: '', originCountry: '', transitPort: '',
  entryPort: '', transportMode: '', consignee: '', currency: 'USD', ieDate: '', remark: '',
  goodsList: []
})

const rules = { supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }] }

function addGoods() {
  form.goodsList.push({ name: '', hsCode: '', spec: '', quantity: null, unit: '个', price: null, amount: null, originCountry: '' })
}

function calcAmount(row) {
  if (row.quantity && row.price) row.amount = (row.quantity * row.price).toFixed(2)
}

function calcTotal() {
  const t = form.goodsList.reduce((s, g) => s + (Number(g.amount) || 0), 0)
  return t ? t.toFixed(2) : '0.00'
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit) {
      await updateLedger(form)
      ElMessage.success('保存成功')
    } else {
      await createLedger(form)
      ElMessage.success('创建成功')
    }
    router.push('/ledger')
  } catch (e) { ElMessage.error(e.message || '保存失败') }
  finally { saving.value = false }
}

onMounted(async () => {
  if (isEdit) {
    try {
      const res = await getLedgerById(route.params.id)
      const d = res.data
      Object.assign(form, d.ledger || d)
      form.goodsList = d.goodsList || []
    } catch { ElMessage.error('加载失败') }
  }
})
</script>
