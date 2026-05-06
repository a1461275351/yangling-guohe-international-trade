<template>
  <div class="goods-list">
    <el-card shadow="never">
      <!-- Search bar -->
      <div class="search-bar">
        <el-form :model="queryParams" inline>
          <el-form-item label="品名">
            <el-input v-model="queryParams.name" placeholder="请输入品名" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="HS编码">
            <el-input v-model="queryParams.hsCode" placeholder="请输入HS编码" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="货物编号">
            <el-input v-model="queryParams.goodsNo" placeholder="请输入货物编号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryParams.category" placeholder="全部分类" clearable>
              <el-option label="原材料" value="原材料" />
              <el-option label="半成品" value="半成品" />
              <el-option label="成品" value="成品" />
              <el-option label="配件" value="配件" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <el-button type="primary" @click="$router.push('/goods/create')">添加货物</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除 <span v-if="selectedIds.length">({{ selectedIds.length }})</span>
        </el-button>
      </div>

      <!-- Table -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="goodsNo" label="货物编号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="品名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="hsCode" label="HS编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" min-width="120" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" min-width="100" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="{ row }">
            {{ row.price != null ? Number(row.price).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="primary" @click="$router.push(`/goods/edit/${row.id}`)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="货物详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="货物编号">{{ currentRow.goodsNo }}</el-descriptions-item>
        <el-descriptions-item label="品名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="HS编码">{{ currentRow.hsCode }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentRow.spec }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentRow.model }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ currentRow.price }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ currentRow.currency }}</el-descriptions-item>
        <el-descriptions-item label="图片">{{ currentRow.imageUrl || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoodsList, deleteGoods, batchDeleteGoods } from '@/api/goods'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const detailVisible = ref(false)
const currentRow = ref(null)

const queryParams = reactive({
  name: '',
  hsCode: '',
  goodsNo: '',
  category: '',
  pageNum: 1,
  pageSize: 20
})

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.name = ''
  queryParams.hsCode = ''
  queryParams.goodsNo = ''
  queryParams.category = ''
  queryParams.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getGoodsList(queryParams)
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleView(row) {
  currentRow.value = row
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该货物吗？', '提示', { type: 'warning' })
    await deleteGoods(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', { type: 'warning' })
    await batchDeleteGoods(selectedIds.value)
    ElMessage.success('批量删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.goods-list {
  padding: 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
