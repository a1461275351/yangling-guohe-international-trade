<template>
  <div style="padding:20px">
    <el-card shadow="never">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div style="display:flex;align-items:center;gap:12px">
          <el-button @click="router.push('/ledger')">返回列表</el-button>
          <h3 style="margin:0">台账详情</h3>
          <el-tag>{{ ledger.ledgerNo }}</el-tag>
        </div>
        <div style="display:flex;gap:8px">
          <el-dropdown style="margin-right:8px" @command="handleDocGenerate">
            <el-button type="warning">
              生成单据 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="packing-list">箱单 PackingList</el-dropdown-item>
                <el-dropdown-item command="invoice">发票 Invoice</el-dropdown-item>
                <el-dropdown-item command="contract">合同 Contract</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" v-if="ledger.status==='DRAFT'" @click="router.push(`/ledger/edit/${ledger.id}`)">编辑</el-button>
          <el-button type="success" @click="handleGenerate">生成报关单</el-button>
        </div>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="台账编号">{{ ledger.ledgerNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="{DRAFT:'info',SUBMITTED:'warning',APPROVED:'success'}[ledger.status]" size="small">{{ {DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已审核'}[ledger.status] || ledger.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="拆分状态"><el-tag :type="ledger.splitStatus==='SPLIT'?'success':'info'" size="small">{{ {UNSPLIT:'未拆分',PARTIAL:'部分拆分',SPLIT:'已拆分'}[ledger.splitStatus] || '-' }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="总金额">{{ ledger.totalAmount ? Number(ledger.totalAmount).toFixed(2) : '-' }} {{ ledger.currency }}</el-descriptions-item>
        <el-descriptions-item label="主提单号">{{ ledger.masterBlNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分提单号">{{ ledger.subBlNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="箱案号">{{ ledger.caseNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ ledger.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="监管方式">{{ ledger.supervisionMode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同协议号">{{ ledger.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申报海关">{{ ledger.declareCustoms || '-' }}</el-descriptions-item>
        <el-descriptions-item label="进境关别">{{ ledger.entryCustoms || '-' }}</el-descriptions-item>
        <el-descriptions-item label="启运国">{{ ledger.originCountry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经停港">{{ ledger.transitPort || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入境口岸">{{ ledger.entryPort || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运输方式">{{ ledger.transportMode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ ledger.consignee || '-' }}</el-descriptions-item>
        <el-descriptions-item label="进口日期">{{ ledger.ieDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ ledger.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 商品明细 -->
      <h4 style="margin:20px 0 12px">商品明细 ({{ goodsList.length }} 条)</h4>
      <el-table :data="goodsList" border size="small">
        <el-table-column type="index" label="序号" width="50" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="hsCode" label="HS编码" width="120" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">{{ row.amount ? Number(row.amount).toFixed(2) : '' }}</template>
        </el-table-column>
        <el-table-column prop="originCountry" label="原产国" width="80" />
        <el-table-column prop="assignedQty" label="已分配" width="80" align="right" />
      </el-table>

      <!-- 备注 -->
      <div v-if="ledger.remark" style="margin-top:16px;padding:12px;background:#f5f7fa;border-radius:4px">
        <strong>备注:</strong> {{ ledger.remark }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getLedgerById, generateDeclaration } from '@/api/ledger'

const router = useRouter()
const route = useRoute()
const ledger = ref({})
const goodsList = ref([])

async function loadData() {
  try {
    const res = await getLedgerById(route.params.id)
    ledger.value = res.data.ledger || res.data
    goodsList.value = res.data.goodsList || []
  } catch { ElMessage.error('加载失败') }
}

function handleDocGenerate(type) {
  const token = localStorage.getItem('trade_platform_token')
  const url = `/api/ledger/${route.params.id}/doc/${type}`
  // 用 fetch 下载文件
  fetch(url, { headers: { 'Authorization': `Bearer ${token}` } })
    .then(res => {
      if (!res.ok) throw new Error('生成失败')
      const filename = { 'packing-list': '箱单', 'invoice': '发票', 'contract': '合同' }[type] || type
      return res.blob().then(blob => ({ blob, filename }))
    })
    .then(({ blob, filename }) => {
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `${filename}_${ledger.value.ledgerNo || ''}.xlsx`
      document.body.appendChild(a); a.click()
      URL.revokeObjectURL(a.href); document.body.removeChild(a)
      ElMessage.success(`${filename}生成成功`)
    })
    .catch(() => ElMessage.error('单据生成失败'))
}

async function handleGenerate() {
  try {
    await ElMessageBox.confirm('确认从此台账生成报关单？', '确认')
    const res = await generateDeclaration(route.params.id)
    ElMessage.success('报关单生成成功')
    router.push(`/customs/detail/${res.data.id}`)
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message || '生成失败') }
}

onMounted(() => loadData())
</script>
