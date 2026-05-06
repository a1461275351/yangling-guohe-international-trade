<template>
  <div class="template-container">
    <el-card shadow="never" class="main-card">
      <div class="layout-wrapper">
        <!-- 左侧：模板类型菜单 -->
        <div class="left-panel">
          <h3 class="panel-title">模板类型</h3>
          <el-menu :default-active="currentType" class="type-menu" @select="handleTypeSelect">
            <el-menu-item v-for="item in typeOptions" :key="item.value" :index="item.value">
              <el-icon><Document /></el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="right-panel">
          <!-- ========== 列表模式 ========== -->
          <template v-if="!currentTemplate">
            <div class="right-toolbar">
              <h3 class="panel-title">{{ currentTypeLabel }}</h3>
              <el-button type="primary" @click="uploadDialogVisible = true">
                <el-icon><Upload /></el-icon> 上传模板
              </el-button>
            </div>

            <el-table v-loading="loading" :data="templateList" stripe border>
              <el-table-column prop="name" label="模板名称" min-width="180" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column label="文件" min-width="200">
                <template #default="{ row }">
                  <div v-if="row.fileName" style="display:flex;align-items:center;gap:6px">
                    <el-icon color="#409eff"><Document /></el-icon>
                    <span>{{ row.fileName }}</span>
                    <el-tag size="small" type="info">{{ formatFileSize(row.fileSize) }}</el-tag>
                  </div>
                  <span v-else style="color:#999">未上传文件</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="170" />
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="scope">
                  <el-button type="primary" link size="small" @click="viewDetail(scope.row)">预览</el-button>
                  <el-button type="warning" link size="small" @click="openReupload(scope.row)">替换</el-button>
                  <el-button type="primary" link size="small" :disabled="!scope.row.filePath" @click="handleDownload(scope.row)">下载</el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div style="display:flex;justify-content:flex-end;margin-top:16px">
              <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
                :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadData" @current-change="loadData" />
            </div>
          </template>

          <!-- ========== 预览模式 ========== -->
          <template v-else>
            <div class="preview-header">
              <div style="display:flex;align-items:center;gap:12px">
                <el-button @click="currentTemplate = null" :icon="Back">返回列表</el-button>
                <span class="preview-title">模板预览</span>
                <el-tag size="small">{{ currentTypeLabel }}</el-tag>
              </div>
              <div style="display:flex;gap:8px">
                <el-button type="warning" @click="openReupload(currentTemplate)">
                  <el-icon><Upload /></el-icon> 重新上传
                </el-button>
                <el-button :disabled="!currentTemplate.filePath" @click="handleDownload(currentTemplate)">
                  <el-icon><Download /></el-icon> 下载文件
                </el-button>
              </div>
            </div>

            <!-- 基本信息 -->
            <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
              <el-descriptions-item label="模板名称">{{ currentTemplate.name }}</el-descriptions-item>
              <el-descriptions-item label="描述">{{ currentTemplate.description || '无' }}</el-descriptions-item>
              <el-descriptions-item label="文件">
                <span v-if="currentTemplate.fileName">{{ currentTemplate.fileName }} ({{ formatFileSize(currentTemplate.fileSize) }})</span>
                <span v-else style="color:#999">无</span>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 预览内容 -->
            <div class="preview-body">
              <!-- 有PDF预览文件：用iframe展示，100%还原原文件 -->
              <iframe
                v-if="currentTemplate.pdfPath || (currentTemplate.fileName && currentTemplate.fileName.endsWith('.pdf'))"
                :src="'/api/templates/preview/' + currentTemplate.id"
                class="pdf-preview"
                frameborder="0"
              />
              <!-- 有HTML内容：用HTML渲染 -->
              <div v-else-if="currentTemplate.htmlContent" class="preview-content" v-html="currentTemplate.htmlContent"></div>
              <!-- 都没有 -->
              <el-empty v-else description="此模板暂无预览，请上传 .docx 文件后自动生成预览" />
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传模板" width="560px" :close-on-click-modal="false" @closed="resetUploadForm">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="模板描述（可选）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="模板文件" required>
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="f => uploadFile = f.raw" :on-remove="() => uploadFile = null" accept=".doc,.docx,.xls,.xlsx,.pdf" drag>
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或 <em>点击上传</em></div>
            <template #tip><div class="el-upload__tip">支持 doc、docx、xls、xlsx、pdf 格式，不超过 20MB</div></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitUpload">确定上传</el-button>
      </template>
    </el-dialog>

    <!-- 替换文件对话框 -->
    <el-dialog v-model="reuploadDialogVisible" title="替换模板文件" width="500px" :close-on-click-modal="false" @closed="reuploadFile = null">
      <div style="padding:12px 16px;background:#f5f7fa;border-radius:6px;margin-bottom:16px">
        <p style="margin:0">当前模板：<strong>{{ reuploadTemplate?.name }}</strong></p>
        <p v-if="reuploadTemplate?.fileName" style="margin:4px 0 0">当前文件：{{ reuploadTemplate.fileName }}</p>
      </div>
      <el-upload ref="reuploadRef" :auto-upload="false" :limit="1" :on-change="f => reuploadFile = f.raw" :on-remove="() => reuploadFile = null" accept=".doc,.docx,.xls,.xlsx,.pdf" drag>
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽或 <em>点击选择</em> 新文件</div>
      </el-upload>
      <template #footer>
        <el-button @click="reuploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reuploadLoading" :disabled="!reuploadFile" @click="submitReupload">确认替换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Upload, Download, UploadFilled, Back } from '@element-plus/icons-vue'
import {
  getTemplateList, getTemplateById, deleteTemplate,
  uploadTemplate, updateTemplateFile, updateTemplate
} from '@/api/template'

const typeOptions = [
  { label: '合同模板', value: 'CONTRACT' },
  { label: '订单模板', value: 'ORDER' },
  { label: '发票模板', value: 'INVOICE' },
  { label: '箱单模板', value: 'PACKING' },
  { label: '报关单模板', value: 'CUSTOMS' }
]
const typeLabelMap = { CONTRACT:'合同模板', ORDER:'订单模板', INVOICE:'发票模板', PACKING:'箱单模板', CUSTOMS:'报关单模板' }

const currentType = ref('CONTRACT')
const currentTypeLabel = computed(() => typeLabelMap[currentType.value])
const loading = ref(false)
const templateList = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 20 })

// 预览
const currentTemplate = ref(null)

// 上传
const uploadDialogVisible = ref(false)
const submitLoading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const uploadFile = ref(null)
const uploadForm = reactive({ name: '', description: '' })
const uploadRules = { name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }] }

// 替换
const reuploadDialogVisible = ref(false)
const reuploadLoading = ref(false)
const reuploadRef = ref(null)
const reuploadTemplate = ref(null)
const reuploadFile = ref(null)

// ========== 方法 ==========

function handleTypeSelect(type) {
  currentType.value = type
  currentTemplate.value = null
  queryParams.current = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTemplateList({ current: queryParams.current, size: queryParams.size, type: currentType.value })
    templateList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

// 预览详情
async function viewDetail(row) {
  try {
    const res = await getTemplateById(row.id)
    currentTemplate.value = res.code === 200 ? res.data : { ...row }
  } catch {
    currentTemplate.value = { ...row }
  }
}

// 上传
function resetUploadForm() {
  uploadFormRef.value?.resetFields()
  uploadFile.value = null
  uploadRef.value?.clearFiles()
}

async function submitUpload() {
  const valid = await uploadFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!uploadFile.value) { ElMessage.warning('请选择文件'); return }

  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile.value)
    fd.append('type', currentType.value)
    fd.append('name', uploadForm.name)
    if (uploadForm.description) fd.append('description', uploadForm.description)
    await uploadTemplate(fd)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '上传失败') }
  finally { submitLoading.value = false }
}

// 替换
function openReupload(row) {
  reuploadTemplate.value = row
  reuploadFile.value = null
  reuploadDialogVisible.value = true
}

async function submitReupload() {
  if (!reuploadFile.value) { ElMessage.warning('请选择新文件'); return }
  reuploadLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', reuploadFile.value)
    await updateTemplateFile(reuploadTemplate.value.id, fd)
    ElMessage.success('替换成功，预览已更新')
    reuploadDialogVisible.value = false
    // 刷新预览
    if (currentTemplate.value?.id === reuploadTemplate.value.id) {
      viewDetail(reuploadTemplate.value)
    }
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '替换失败') }
  finally { reuploadLoading.value = false }
}

// 下载
async function handleDownload(row) {
  if (!row.filePath) { ElMessage.warning('没有文件'); return }
  try {
    const token = localStorage.getItem('trade_platform_token')
    const resp = await fetch(`/api/templates/download/${row.id}`, { headers: { 'Authorization': `Bearer ${token}` } })
    if (!resp.ok) throw new Error()
    const blob = await resp.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = row.fileName; document.body.appendChild(a); a.click()
    window.URL.revokeObjectURL(url); document.body.removeChild(a)
  } catch { ElMessage.error('下载失败') }
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name}」？`, '删除确认', { type: 'warning' })
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error('删除失败') }
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

onMounted(() => { loadData() })
</script>

<style scoped>
.template-container { padding: 20px; }
.main-card { min-height: calc(100vh - 140px); }
.layout-wrapper { display: flex; gap: 20px; }
.left-panel { width: 200px; flex-shrink: 0; border-right: 1px solid #ebeef5; padding-right: 16px; }
.right-panel { flex: 1; min-width: 0; }
.panel-title { margin: 0 0 12px; font-size: 16px; font-weight: 600; color: #303133; }
.type-menu { border-right: none; }
.right-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }

/* 预览模式 */
.preview-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 16px; border-bottom: 1px solid #ebeef5; margin-bottom: 16px;
}
.preview-title { font-size: 16px; font-weight: 600; }
.preview-body {
  border: 1px solid #ebeef5; border-radius: 8px; min-height: 700px;
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  overflow: hidden;
}
.pdf-preview {
  width: 100%; height: 750px; border: none;
}
.preview-content { line-height: 1.8; font-size: 14px; color: #303133; }
.preview-content h1, .preview-content h2, .preview-content h3 { margin: 16px 0 8px; }
.preview-content p { margin: 4px 0; }
.preview-content table { border-collapse: collapse; width: 100%; margin: 12px 0; }
.preview-content th, .preview-content td { border: 1px solid #333; padding: 6px 10px; }
.preview-content strong { font-weight: bold; }
</style>
