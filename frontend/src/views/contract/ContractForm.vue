<template>
  <div class="contract-form">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑合同' : '创建合同' }}</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        v-loading="pageLoading"
        style="max-width: 800px"
      >
        <el-form-item label="合同编号" prop="contractNo">
          <el-input
            v-model="formData.contractNo"
            placeholder="系统自动生成"
            :disabled="isEdit"
            :readonly="!isEdit"
          />
        </el-form-item>

        <el-form-item label="合同标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入合同标题" />
        </el-form-item>

        <el-form-item label="合同类型" prop="contractType">
          <el-select v-model="formData.contractType" placeholder="请选择合同类型" style="width: 100%">
            <el-option label="采购合同" value="PURCHASE" />
            <el-option label="销售合同" value="SALES" />
            <el-option label="服务合同" value="SERVICE" />
            <el-option label="代理合同" value="AGENCY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务企业" prop="enterpriseId">
          <el-select
            v-model="formData.enterpriseId"
            placeholder="请选择关联服务企业"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in enterpriseList"
              :key="item.id"
              :label="item.enterpriseName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="本方企业" prop="ourCompany">
          <el-input v-model="formData.ourCompany" placeholder="请输入本方企业名称" />
        </el-form-item>

        <el-form-item label="对方类型" prop="partnerType">
          <el-radio-group v-model="formData.partnerType" @change="handlePartnerTypeChange">
            <el-radio value="SUPPLIER">供应商</el-radio>
            <el-radio value="CUSTOMER">客户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="对方名称" prop="partnerId">
          <el-select
            v-model="formData.partnerId"
            placeholder="请选择对方名称"
            filterable
            @change="handlePartnerChange"
            style="width: 100%"
          >
            <el-option
              v-for="item in partnerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="签署日期" prop="signDate">
          <el-date-picker
            v-model="formData.signDate"
            type="date"
            placeholder="请选择签署日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="到期日期" prop="expireDate">
          <el-date-picker
            v-model="formData.expireDate"
            type="date"
            placeholder="请选择到期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="合同金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :precision="2"
            :min="0"
            :controls="false"
            placeholder="请输入合同金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="币种" prop="currency">
          <el-select v-model="formData.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="人民币 (CNY)" value="CNY" />
            <el-option label="美元 (USD)" value="USD" />
            <el-option label="欧元 (EUR)" value="EUR" />
          </el-select>
        </el-form-item>

        <el-form-item label="合同条款" prop="terms">
          <el-input
            v-model="formData.terms"
            type="textarea"
            :rows="6"
            placeholder="请输入合同条款"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更新' : '保存' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContractById, createContract, updateContract } from '@/api/contract'
import { getActivePartners } from '@/api/partner'
import { getActiveEnterprises } from '@/api/enterprise'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const pageLoading = ref(false)
const submitLoading = ref(false)
const partnerList = ref([])
const enterpriseList = ref([])

const contractId = computed(() => route.params.id)
const isEdit = computed(() => !!contractId.value)

const formData = reactive({
  contractNo: '',
  title: '',
  contractType: '',
  enterpriseId: null,
  ourCompany: '',
  partnerType: 'SUPPLIER',
  partnerId: null,
  partnerName: '',
  signDate: '',
  expireDate: '',
  amount: null,
  currency: 'CNY',
  terms: '',
  remark: ''
})

const formRules = {
  title: [{ required: true, message: '请输入合同标题', trigger: 'blur' }],
  ourCompany: [{ required: true, message: '请输入本方企业名称', trigger: 'blur' }],
  partnerType: [{ required: true, message: '请选择对方类型', trigger: 'change' }],
  partnerId: [{ required: true, message: '请选择对方名称', trigger: 'change' }],
  signDate: [{ required: true, message: '请选择签署日期', trigger: 'change' }],
  expireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
}

async function fetchPartners() {
  try {
    const res = await getActivePartners(formData.partnerType)
    partnerList.value = res.data || []
  } catch (error) {
    console.error('获取合作伙伴列表失败:', error)
    partnerList.value = []
  }
}

function handlePartnerTypeChange() {
  formData.partnerId = null
  formData.partnerName = ''
  fetchPartners()
}

function handlePartnerChange(val) {
  const selected = partnerList.value.find((item) => item.id === val)
  if (selected) {
    formData.partnerName = selected.name
  }
}

async function fetchContractDetail() {
  if (!contractId.value) return
  pageLoading.value = true
  try {
    const res = await getContractById(contractId.value)
    const data = res.data || {}
    Object.keys(formData).forEach((key) => {
      if (data[key] !== undefined) {
        formData[key] = data[key]
      }
    })
    await fetchPartners()
  } catch (error) {
    ElMessage.error('获取合同详情失败')
    console.error(error)
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      // 清理空字符串为 null，避免后端 LocalDate 解析失败
      const data = { ...formData }
      Object.keys(data).forEach(key => {
        if (data[key] === '') data[key] = null
      })
      if (isEdit.value) {
        data.id = contractId.value
        await updateContract(data)
        ElMessage.success('合同更新成功')
      } else {
        await createContract(data)
        ElMessage.success('合同创建成功')
      }
      router.push('/contracts')
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error(error)
    } finally {
      submitLoading.value = false
    }
  })
}

async function fetchEnterprises() {
  try {
    const res = await getActiveEnterprises()
    enterpriseList.value = res.data || []
  } catch (e) {
    console.warn('获取服务企业列表失败', e)
  }
}

onMounted(() => {
  fetchEnterprises()
  if (isEdit.value) {
    fetchContractDetail()
  } else {
    fetchPartners()
  }
})
</script>

<style scoped>
.contract-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
