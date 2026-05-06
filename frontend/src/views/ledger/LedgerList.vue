<template>
  <div style="padding:20px">
    <el-card shadow="never">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h3 style="margin:0;font-size:16px;font-weight:600">进口台账</h3>
        <div>
          <el-button type="primary" @click="router.push('/ledger/create')"><el-icon><Plus /></el-icon> 新增台账</el-button>
        </div>
      </div>

      <!-- 搜索 -->
      <el-form :inline="true" :model="query" style="margin-bottom:12px">
        <el-form-item label="台账编号"><el-input v-model="query.ledgerNo" placeholder="请输入" clearable style="width:160px" /></el-form-item>
        <el-form-item label="供应商"><el-input v-model="query.supplierName" placeholder="请输入" clearable style="width:160px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已审核" value="APPROVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="query.ledgerNo='';query.supplierName='';query.status='';loadData()">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="list" stripe border>
        <el-table-column prop="ledgerNo" label="台账编号" width="170" />
        <el-table-column prop="masterBlNo" label="主提单号" width="140" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
        <el-table-column prop="supervisionMode" label="监管方式" width="100" />
        <el-table-column prop="contractNo" label="合同协议号" width="150" />
        <el-table-column prop="declareCustoms" label="申报海关" width="100" />
        <el-table-column prop="originCountry" label="启运国" width="80" />
        <el-table-column prop="entryPort" label="入境口岸" width="80" />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">{{ row.totalAmount ? Number(row.totalAmount).toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="goodsCount" label="商品数" width="70" align="center" />
        <el-table-column prop="splitStatus" label="拆分状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.splitStatus==='SPLIT'?'success':row.splitStatus==='PARTIAL'?'warning':'info'" size="small">
              {{ {UNSPLIT:'未拆分',PARTIAL:'部分拆分',SPLIT:'已拆分'}[row.splitStatus] || row.splitStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="{DRAFT:'info',SUBMITTED:'warning',APPROVED:'success'}[row.status]" size="small">
              {{ {DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已审核'}[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="router.push(`/ledger/detail/${scope.row.id}`)">详情</el-button>
            <el-button type="primary" link size="small" v-if="scope.row.status==='DRAFT'" @click="router.push(`/ledger/edit/${scope.row.id}`)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleGenerate(scope.row)">生成报关单</el-button>
            <el-button type="danger" link size="small" v-if="scope.row.status==='DRAFT'" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size"
          :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getLedgerList, deleteLedger, generateDeclaration } from '@/api/ledger'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ ledgerNo: '', supplierName: '', status: '', current: 1, size: 20 })

async function loadData() {
  loading.value = true
  try {
    const res = await getLedgerList(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {} finally { loading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除台账「${row.ledgerNo}」？`, '删除确认', { type: 'warning' })
    await deleteLedger(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error('删除失败') }
}

async function handleGenerate(row) {
  try {
    await ElMessageBox.confirm(`从台账「${row.ledgerNo}」生成报关单？`, '确认', { type: 'info' })
    const res = await generateDeclaration(row.id)
    ElMessage.success('报关单生成成功')
    router.push(`/customs/detail/${res.data.id}`)
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message || '生成失败') }
}

onMounted(() => loadData())
</script>
