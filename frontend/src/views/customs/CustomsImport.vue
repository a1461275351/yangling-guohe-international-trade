<template>
  <div class="customs-import">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>导入报关单</span>
          <el-button @click="$router.push('/customs')">返回列表</el-button>
        </div>
      </template>

      <!-- Upload Area -->
      <div class="upload-area" v-if="!importResult">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          accept=".xlsx,.xls,.csv"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">支持 .xlsx, .xls, .csv 格式文件</div>
          </template>
        </el-upload>

        <div class="upload-actions">
          <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleImport">
            开始导入
          </el-button>
        </div>
      </div>

      <!-- Import Result -->
      <div class="import-result" v-if="importResult">
        <el-result
          :icon="importResult.failCount > 0 ? 'warning' : 'success'"
          :title="importResult.failCount > 0 ? '导入完成（存在错误）' : '导入成功'"
        >
          <template #sub-title>
            <div class="result-stats">
              <span class="stat-item success">成功: {{ importResult.successCount }} 条</span>
              <span class="stat-item fail" v-if="importResult.failCount > 0">失败: {{ importResult.failCount }} 条</span>
            </div>
          </template>
          <template #extra>
            <el-button type="primary" @click="$router.push('/customs')">返回列表</el-button>
            <el-button @click="handleRetry">继续导入</el-button>
          </template>
        </el-result>

        <!-- Error Details -->
        <div class="error-details" v-if="importResult.errors && importResult.errors.length > 0">
          <el-divider content-position="left">错误详情</el-divider>
          <el-table :data="importResult.errors" border stripe max-height="400">
            <el-table-column prop="row" label="行号" width="80" align="center" />
            <el-table-column prop="field" label="字段" width="140" />
            <el-table-column prop="message" label="错误信息" min-width="300" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { importCustoms } from '@/api/customs'

const uploadRef = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const importResult = ref(null)

function handleFileChange(uploadFile) {
  const allowedTypes = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-excel',
    'text/csv'
  ]
  const ext = uploadFile.name.split('.').pop().toLowerCase()
  if (!['xlsx', 'xls', 'csv'].includes(ext)) {
    ElMessage.error('仅支持 .xlsx, .xls, .csv 格式文件')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  selectedFile.value = uploadFile.raw
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

async function handleImport() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const res = await importCustoms(selectedFile.value)
    importResult.value = res.data || { successCount: 0, failCount: 0, errors: [] }
    ElMessage.success('导入完成')
  } catch (e) {
    console.error(e)
  } finally {
    uploading.value = false
  }
}

function handleRetry() {
  importResult.value = null
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}
</script>

<style scoped>
.customs-import {
  padding: 16px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.upload-area {
  max-width: 600px;
  margin: 0 auto;
}
.upload-actions {
  margin-top: 20px;
  text-align: center;
}
.result-stats {
  display: flex;
  gap: 24px;
  justify-content: center;
  font-size: 16px;
}
.stat-item.success {
  color: #67c23a;
}
.stat-item.fail {
  color: #f56c6c;
}
.error-details {
  margin-top: 20px;
}
</style>
