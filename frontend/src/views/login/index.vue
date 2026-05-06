<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <h2 class="login-title">外综服平台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="tenantCode">
          <el-input
            v-model="form.tenantCode"
            placeholder="请输入企业号"
            :prefix-icon="OfficeBuilding"
          />
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/register" class="footer-link">注册新账号</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, OfficeBuilding } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  tenantCode: '',
  username: '',
  password: ''
})

const rules = {
  tenantCode: [
    { required: true, message: '请输入企业号', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    userStore.login(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f0fe 0%, #d1e3f8 50%, #c3d9f2 100%);
}

.login-card {
  width: 420px;
  padding: 30px 20px 10px;
  border-radius: 12px;
}

.login-title {
  text-align: center;
  margin: 0 0 30px;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 2px;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
}

.footer-link {
  font-size: 14px;
  color: #409eff;
  text-decoration: none;
}

.footer-link:hover {
  text-decoration: underline;
}
</style>
