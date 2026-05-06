import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUser, setUser, getToken, setToken, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(getUser())
  const token = ref(getToken())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const tenantId = computed(() => userInfo.value?.tenantId)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isGuohe = computed(() => role.value === 'GUOHE')
  const isEnterprise = computed(() => role.value === 'ENTERPRISE')

  function login(data) {
    token.value = data.token
    const user = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      tenantId: data.tenantId,
      tenantName: data.tenantName,
      tenantCode: data.tenantCode
    }
    userInfo.value = user
    setToken(data.token)
    setUser(user)
  }

  function logout() {
    token.value = null
    userInfo.value = null
    clearAuth()
  }

  function updateInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    setUser(userInfo.value)
  }

  return { userInfo, token, isLoggedIn, role, tenantId, isAdmin, isGuohe, isEnterprise, login, logout, updateInfo }
})
