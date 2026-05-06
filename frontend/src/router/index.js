import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      // 个人信息
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人信息' }
      },
      // 用户管理
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户列表', roles: ['ADMIN', 'GUOHE'] }
      },
      {
        path: 'user-applies',
        name: 'UserApply',
        component: () => import('@/views/user/UserApply.vue'),
        meta: { title: '用户审批', roles: ['ADMIN', 'GUOHE'] }
      },
      // 租户管理
      {
        path: 'tenants',
        name: 'TenantList',
        component: () => import('@/views/tenant/index.vue'),
        meta: { title: '租户管理', roles: ['ADMIN', 'GUOHE'] }
      },
      // 配置管理
      {
        path: 'config',
        name: 'ConfigManage',
        component: () => import('@/views/config/index.vue'),
        meta: { title: '配置管理', roles: ['ADMIN'] }
      },
      // 模板管理
      {
        path: 'templates',
        name: 'TemplateManage',
        component: () => import('@/views/template/index.vue'),
        meta: { title: '模板管理', roles: ['ADMIN'] }
      },
      // 合同管理
      {
        path: 'contracts',
        name: 'ContractList',
        component: () => import('@/views/contract/ContractList.vue'),
        meta: { title: '合同列表' }
      },
      {
        path: 'contracts/create',
        name: 'ContractCreate',
        component: () => import('@/views/contract/ContractForm.vue'),
        meta: { title: '创建合同' }
      },
      {
        path: 'contracts/edit/:id',
        name: 'ContractEdit',
        component: () => import('@/views/contract/ContractForm.vue'),
        meta: { title: '编辑合同' }
      },
      {
        path: 'contracts/detail/:id',
        name: 'ContractDetail',
        component: () => import('@/views/contract/ContractDetail.vue'),
        meta: { title: '合同详情' }
      },
      {
        path: 'contracts/expiring',
        name: 'ContractExpiring',
        component: () => import('@/views/contract/ContractExpiring.vue'),
        meta: { title: '临期合同预警' }
      },
      // 货物管理
      {
        path: 'goods',
        name: 'GoodsList',
        component: () => import('@/views/goods/GoodsList.vue'),
        meta: { title: '货物管理' }
      },
      {
        path: 'goods/create',
        name: 'GoodsCreate',
        component: () => import('@/views/goods/GoodsForm.vue'),
        meta: { title: '添加货物' }
      },
      {
        path: 'goods/edit/:id',
        name: 'GoodsEdit',
        component: () => import('@/views/goods/GoodsForm.vue'),
        meta: { title: '编辑货物' }
      },
      // 订单管理
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'orders/create',
        name: 'OrderCreate',
        component: () => import('@/views/order/OrderForm.vue'),
        meta: { title: '创建订单' }
      },
      {
        path: 'orders/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      // 进口台账管理
      {
        path: 'ledger',
        name: 'LedgerList',
        component: () => import('@/views/ledger/LedgerList.vue'),
        meta: { title: '进口台账' }
      },
      {
        path: 'ledger/create',
        name: 'LedgerCreate',
        component: () => import('@/views/ledger/LedgerForm.vue'),
        meta: { title: '新增台账' }
      },
      {
        path: 'ledger/edit/:id',
        name: 'LedgerEdit',
        component: () => import('@/views/ledger/LedgerForm.vue'),
        meta: { title: '编辑台账' }
      },
      {
        path: 'ledger/detail/:id',
        name: 'LedgerDetail',
        component: () => import('@/views/ledger/LedgerDetail.vue'),
        meta: { title: '台账详情' }
      },
      // 报关单管理
      {
        path: 'customs',
        name: 'CustomsList',
        component: () => import('@/views/customs/CustomsList.vue'),
        meta: { title: '报关单列表' }
      },
      {
        path: 'customs/import',
        name: 'CustomsImport',
        component: () => import('@/views/customs/CustomsImport.vue'),
        meta: { title: '导入报关单' }
      },
      {
        path: 'customs/detail/:id',
        name: 'CustomsDetail',
        component: () => import('@/views/customs/CustomsDetail.vue'),
        meta: { title: '报关单详情' }
      },
      // 文件管理
      {
        path: 'files',
        name: 'FileList',
        component: () => import('@/views/file/index.vue'),
        meta: { title: '文件管理' }
      },
      // 供应商客户管理
      {
        path: 'partners',
        name: 'PartnerList',
        component: () => import('@/views/partner/index.vue'),
        meta: { title: '供应商与客户管理' }
      },
      // 退税管理
      {
        path: 'tax',
        name: 'TaxManage',
        component: () => import('@/views/tax/index.vue'),
        meta: { title: '退税管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title || '外综服平台') + ' - 外综服平台'
  if (to.meta.noAuth) {
    next()
    return
  }
  const token = getToken()
  if (!token) {
    next('/login')
    return
  }
  next()
})

export default router
