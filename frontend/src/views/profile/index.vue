<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- Left: User Info Card -->
      <el-col :xs="24" :sm="8" :md="6">
        <el-card shadow="hover" class="user-card">
          <div class="user-avatar-section">
            <el-avatar :size="80" :icon="UserFilled" class="user-avatar" />
            <h3 class="user-name">{{ userInfo.realName || userInfo.username }}</h3>
            <el-tag :type="roleTagType" size="large">{{ roleLabel }}</el-tag>
          </div>
          <el-divider />
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="用户名">
              {{ userInfo.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="所属企业">
              {{ userStore.userInfo?.tenantName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="企业号">
              {{ userStore.userInfo?.tenantCode || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- Right: Tabs -->
      <el-col :xs="24" :sm="16" :md="18">
        <el-card shadow="hover">
          <el-tabs v-model="activeTab">
            <!-- Basic Info Tab -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                :model="infoForm"
                :rules="infoRules"
                ref="infoFormRef"
                label-width="100px"
                style="max-width: 500px; margin-top: 10px;"
              >
                <el-form-item label="用户名">
                  <el-input :model-value="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="角色">
                  <el-input :model-value="roleLabel" disabled />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="handleSaveInfo">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- Security Tab -->
            <el-tab-pane label="安全设置" name="security">
              <el-form
                :model="passwordForm"
                :rules="passwordRules"
                ref="passwordFormRef"
                label-width="100px"
                style="max-width: 500px; margin-top: 10px;"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref('basic')
const infoFormRef = ref(null)
const passwordFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const userInfo = reactive({
  username: '',
  realName: '',
  role: '',
  phone: '',
  email: ''
})

const infoForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  const map = { ADMIN: '系统管理员', GUOHE: '国合员工', ENTERPRISE: '企业员工' }
  return map[userInfo.role] || userInfo.role
})

const roleTagType = computed(() => {
  const map = { ADMIN: 'danger', GUOHE: '', ENTERPRISE: 'success' }
  return map[userInfo.role] || 'info'
})

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const infoRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function loadUserInfo() {
  try {
    const res = await getUserInfo()
    const data = res.data
    Object.assign(userInfo, {
      username: data.username,
      realName: data.realName,
      role: data.role,
      phone: data.phone,
      email: data.email
    })
    Object.assign(infoForm, {
      realName: data.realName || '',
      phone: data.phone || '',
      email: data.email || ''
    })
  } catch (err) {
    ElMessage.error('获取用户信息失败')
  }
}

async function handleSaveInfo() {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  infoLoading.value = true
  try {
    await updateUserInfo(infoForm)
    ElMessage.success('信息更新成功')
    userStore.updateInfo({ realName: infoForm.realName })
    await loadUserInfo()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '更新失败')
  } finally {
    infoLoading.value = false
  }
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordFormRef.value.resetFields()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '密码修改失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.user-card {
  text-align: center;
}

.user-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}

.user-avatar {
  background-color: #409eff;
}

.user-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-container .el-col {
    margin-bottom: 16px;
  }
}
</style>
