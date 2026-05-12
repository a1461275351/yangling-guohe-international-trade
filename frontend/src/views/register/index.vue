<template>
  <div class="register-container">
    <el-card class="register-card" shadow="always">
      <h2 class="register-title">注册账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" size="large">
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="国合员工" value="GUOHE" />
            <el-option label="企业员工" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业号" prop="tenantCode">
          <el-input v-model="form.tenantCode" placeholder="请输入企业号" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="8-32位，含大小写字母、数字和特殊字符"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="footer-link">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  role: '',
  tenantCode: '',
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules = {
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  tenantCode: [
    { required: true, message: '请输入企业号', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度为8-32个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=])/, message: '需包含大小写字母、数字和特殊字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { confirmPassword, ...submitData } = form
    await register(submitData)
    ElMessage.success('注册申请已提交，请等待审批')
    router.push('/login')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f0fe 0%, #d1e3f8 50%, #c3d9f2 100%);
}

.register-card {
  width: 520px;
  padding: 30px 20px 10px;
  border-radius: 12px;
}

.register-title {
  text-align: center;
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.register-footer {
  text-align: center;
  margin-top: 4px;
  font-size: 14px;
  color: #909399;
}

.footer-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.footer-link:hover {
  text-decoration: underline;
}
</style>
