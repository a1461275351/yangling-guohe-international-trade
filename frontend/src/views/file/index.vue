<template>
  <div class="file-management">
    <el-card shadow="never">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="uploadDialogVisible = true">上传文件</el-button>
          <el-button :disabled="selectedIds.length === 0" @click="handleBatchDownload">批量下载 ({{ selectedIds.length }})</el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除 ({{ selectedIds.length }})</el-button>
        </div>
        <div class="toolbar-right">
          <el-select v-model="queryParams.businessType" placeholder="业务类型" clearable style="width: 150px" @change="handleSearch">
            <el-option label="企业资质" value="企业资质" />
            <el-option label="合同附件" value="合同附件" />
            <el-option label="报关单据" value="报关单据" />
          </el-select>
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索文件名"
            clearable
            style="width: 200px; margin-left: 8px"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- File Table -->
      <el-table v-loading="loading" :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="originalName" label="文件名" min-width="240" show-overflow-tooltip />
        <el-table-column label="大小" width="100" align="right">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="100" align="center" />
        <el-table-column prop="businessType" label="业务类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.businessType" size="small">{{ row.businessType }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
            <el-button link type="primary" @click="handleRename(row)">重命名</el-button>
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

    <!-- Upload Dialog -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="业务类型">
          <el-select v-model="uploadBusinessType" placeholder="请选择业务类型" style="width: 100%">
            <el-option label="企业资质" value="企业资质" />
            <el-option label="合同附件" value="合同附件" />
            <el-option label="报关单据" value="报关单据" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :on-change="handleUploadChange"
            :on-remove="handleUploadRemove"
            :before-upload="beforeUpload"
            multiple
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">单个文件不超过 5MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">确认上传</el-button>
      </template>
    </el-dialog>

    <!-- Rename Dialog -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px">
      <el-input v-model="renameValue" placeholder="请输入新文件名" @keyup.enter="submitRename" />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="renaming" @click="submitRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getFileList, uploadFile, deleteFile, batchDeleteFiles, renameFile, getDownloadUrl } from '@/api/file'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const selectedRows = ref([])

const uploadDialogVisible = ref(false)
const uploadBusinessType = ref('')
const uploadRef = ref(null)
const uploadFiles = ref([])
const uploading = ref(false)

const renameDialogVisible = ref(false)
const renameValue = ref('')
const renameRow = ref(null)
const renaming = ref(false)

const queryParams = reactive({
  keyword: '',
  businessType: '',
  pageNum: 1,
  pageSize: 20
})

function formatSize(bytes) {
  if (bytes == null || bytes === 0) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFileList(queryParams)
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
  selectedIds.value = rows.map(r => r.id)
}

function handleDownload(row) {
  const url = getDownloadUrl(row.id)
  fetch(url, { headers: { 'Authorization': 'Bearer ' + getToken() } })
    .then(res => res.blob())
    .then(blob => {
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = row.originalName
      a.click()
      URL.revokeObjectURL(a.href)
    })
    .catch(() => {
      ElMessage.error('下载失败')
    })
}

function handleBatchDownload() {
  selectedRows.value.forEach(row => {
    handleDownload(row)
  })
}

function handleRename(row) {
  renameRow.value = row
  renameValue.value = row.originalName
  renameDialogVisible.value = true
}

async function submitRename() {
  if (!renameValue.value.trim()) {
    ElMessage.warning('文件名不能为空')
    return
  }
  renaming.value = true
  try {
    await renameFile(renameRow.value.id, renameValue.value.trim())
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    renaming.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', { type: 'warning' })
    await deleteFile(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个文件吗？`, '提示', { type: 'warning' })
    await batchDeleteFiles(selectedIds.value)
    ElMessage.success('批量删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function beforeUpload(file) {
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

function handleUploadChange(file) {
  if (file.raw && file.raw.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 5MB')
    uploadRef.value?.handleRemove(file)
    return
  }
  uploadFiles.value.push(file.raw)
}

function handleUploadRemove(file) {
  const idx = uploadFiles.value.indexOf(file.raw)
  if (idx > -1) uploadFiles.value.splice(idx, 1)
}

async function submitUpload() {
  if (uploadFiles.value.length === 0) {
    ElMessage.warning('请选择至少一个文件')
    return
  }
  uploading.value = true
  try {
    let successCount = 0
    for (const file of uploadFiles.value) {
      try {
        await uploadFile(file, uploadBusinessType.value)
        successCount++
      } catch (e) {
        console.error('上传失败:', file.name, e)
      }
    }
    ElMessage.success(`成功上传 ${successCount} 个文件`)
    uploadDialogVisible.value = false
    uploadFiles.value = []
    uploadBusinessType.value = ''
    loadData()
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-management {
  padding: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.toolbar-left {
  display: flex;
  gap: 8px;
}
.toolbar-right {
  display: flex;
  align-items: center;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
