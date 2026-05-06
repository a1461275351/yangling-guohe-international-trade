<template>
  <div class="goods-form">
    <el-card shadow="never">
      <template #header>
        <span>{{ isEdit ? '编辑货物' : '添加货物' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        style="max-width: 700px"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="货物编号" prop="goodsNo">
          <el-input v-model="formData.goodsNo" placeholder="请输入货物编号" />
        </el-form-item>
        <el-form-item label="品名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入品名" />
        </el-form-item>
        <el-form-item label="HS编码" prop="hsCode">
          <el-input v-model="formData.hsCode" placeholder="请输入HS编码" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="原材料" value="原材料" />
            <el-option label="半成品" value="半成品" />
            <el-option label="成品" value="成品" />
            <el-option label="配件" value="配件" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 规格参数 -->
        <el-divider content-position="left">规格参数</el-divider>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="formData.spec" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="formData.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位，如：件、箱、吨" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="formData.price"
            :precision="2"
            :min="0"
            :controls="false"
            placeholder="请输入单价"
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
        <el-form-item label="图片地址">
          <el-input v-model="formData.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>

        <!-- Buttons -->
        <el-form-item>
          <el-button type="primary" @click="handleSave(false)" :loading="saving">保存并返回</el-button>
          <el-button v-if="!isEdit" type="success" @click="handleSave(true)" :loading="saving">保存并继续添加</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsById, createGoods, updateGoods } from '@/api/goods'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  id: null,
  goodsNo: '',
  name: '',
  hsCode: '',
  category: '',
  spec: '',
  model: '',
  unit: '',
  price: null,
  currency: 'CNY',
  imageUrl: ''
})

const rules = {
  goodsNo: [{ required: true, message: '请输入货物编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入品名', trigger: 'blur' }],
  hsCode: [{ required: true, message: '请输入HS编码', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
}

async function loadGoods() {
  if (!route.params.id) return
  try {
    const res = await getGoodsById(route.params.id)
    Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('加载货物信息失败')
    console.error(e)
  }
}

function resetForm() {
  formData.id = null
  formData.goodsNo = ''
  formData.name = ''
  formData.hsCode = ''
  formData.category = ''
  formData.spec = ''
  formData.model = ''
  formData.unit = ''
  formData.price = null
  formData.currency = 'CNY'
  formData.imageUrl = ''
  formRef.value?.clearValidate()
}

async function handleSave(continueAdd) {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateGoods({ ...formData })
    } else {
      await createGoods({ ...formData })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    if (continueAdd) {
      resetForm()
    } else {
      router.push('/goods')
    }
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  router.push('/goods')
}

onMounted(() => {
  loadGoods()
})
</script>

<style scoped>
.goods-form {
  padding: 16px;
}
</style>
