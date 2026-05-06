<template>
  <div class="onlyoffice-editor">
    <div v-if="loading" class="editor-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>正在加载Office编辑器...</p>
    </div>
    <div v-if="error" class="editor-error">
      <el-result icon="error" :title="error" sub-title="请检查ONLYOFFICE服务是否正常运行">
        <template #extra>
          <el-button @click="$emit('close')">关闭</el-button>
          <el-button type="primary" @click="initEditor">重试</el-button>
        </template>
      </el-result>
    </div>
    <div id="onlyoffice-container" ref="editorContainer" class="editor-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getOnlyOfficeConfig } from '@/api/template'

const props = defineProps({
  templateId: { type: [Number, String], required: true },
  mode: { type: String, default: 'edit' }  // 'edit' 或 'view'
})

const emit = defineEmits(['close', 'saved'])

const loading = ref(true)
const error = ref('')
let editorInstance = null

async function initEditor() {
  loading.value = true
  error.value = ''

  try {
    // 1. 获取配置
    const res = await getOnlyOfficeConfig(props.templateId, props.mode)
    if (res.code !== 200) {
      error.value = res.message || '获取编辑器配置失败'
      loading.value = false
      return
    }

    const { config, documentServerUrl } = res.data

    // 2. 加载 ONLYOFFICE JS API
    await loadScript(documentServerUrl + '/web-apps/apps/api/documents/api.js')

    // 3. 设置模式和事件
    if (!config.editorConfig) config.editorConfig = {}
    // 只读模式
    if (props.mode === 'view') {
      config.editorConfig.mode = 'view'
      config.editorConfig.customization = {
        chat: false,
        toolbarNoTabs: true,
        compactHeader: true
      }
    } else {
      config.editorConfig.mode = 'edit'
      config.editorConfig.customization = {
        forcesave: true,
        chat: false,
        compactHeader: true
      }
    }
    config.events = {
      onDocumentStateChange: (event) => {
        // 文档内容变化
      },
      onRequestClose: () => {
        emit('close')
      },
      onError: (event) => {
        console.error('ONLYOFFICE error:', event)
      }
    }
    config.height = '100%'
    config.width = '100%'

    // 4. 创建编辑器
    loading.value = false
    editorInstance = new window.DocsAPI.DocEditor('onlyoffice-container', config)

  } catch (e) {
    console.error('ONLYOFFICE init error:', e)
    error.value = '加载Office编辑器失败，请确认ONLYOFFICE服务已启动'
    loading.value = false
  }
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    // 检查是否已加载
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = src
    script.onload = resolve
    script.onerror = () => reject(new Error('Failed to load ONLYOFFICE API'))
    document.head.appendChild(script)
  })
}

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (editorInstance) {
    try { editorInstance.destroyEditor() } catch (e) { /* ignore */ }
    editorInstance = null
  }
})
</script>

<style scoped>
.onlyoffice-editor {
  width: 100%;
  height: 100%;
  min-height: 700px;
  position: relative;
}
.editor-container {
  width: 100%;
  height: 100%;
  min-height: 700px;
}
.editor-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
}
.editor-loading p {
  margin-top: 12px;
}
.editor-error {
  padding: 40px;
}
</style>
