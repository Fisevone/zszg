<template>
  <div class="resources-page">
    <div class="page-header glass-card">
      <div>
        <h1 class="page-title">
          <el-icon><Folder /></el-icon>
          学习资源管理
        </h1>
        <p class="page-subtitle">上传和管理教学资源</p>
      </div>
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        上传资源
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ resourceStats.total }}</div>
              <div class="stat-label">资源总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-green" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ resourceStats.videoCount }}</div>
              <div class="stat-label">视频资源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-orange" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ resourceStats.docCount }}</div>
              <div class="stat-label">文档资源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-purple" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ resourceStats.weekUploads }}</div>
              <div class="stat-label">本周上传</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选器和视图切换 -->
    <div class="filter-card glass-card">
      <div class="filter-content">
        <el-space wrap>
          <el-select v-model="filterType" placeholder="资源类型" clearable @change="fetchResources" style="width: 150px">
            <el-option label="讲义" value="讲义" />
            <el-option label="课件" value="课件" />
            <el-option label="视频" value="视频" />
            <el-option label="练习题" value="练习题" />
            <el-option label="其他" value="其他" />
          </el-select>
          
          <el-select v-model="filterSubject" placeholder="学科" clearable @change="fetchResources" style="width: 150px">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
          </el-select>

          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索资源标题..." 
            clearable 
            style="width: 200px"
            @input="fetchResources"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-space>

        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button value="card">
            <el-icon><Grid /></el-icon> 卡片视图
          </el-radio-button>
          <el-radio-button value="table">
            <el-icon><List /></el-icon> 列表视图
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" v-loading="loading" class="resources-grid">
      <el-card 
        v-for="resource in resources" 
        :key="resource.id" 
        class="resource-card glass-card"
        shadow="hover"
        @click="previewResource(resource)"
      >
        <div class="resource-card-content">
          <div class="resource-icon">
            <el-icon v-if="resource.type === '视频'" style="font-size: 48px; color: #e6a23c;">
              <VideoPlay />
            </el-icon>
            <el-icon v-else-if="resource.type === '讲义' || resource.type === '课件'" style="font-size: 48px; color: #409eff;">
              <Reading />
            </el-icon>
            <el-icon v-else-if="resource.type === '练习题'" style="font-size: 48px; color: #67c23a;">
              <EditPen />
            </el-icon>
            <el-icon v-else style="font-size: 48px; color: #909399;">
              <Document />
            </el-icon>
          </div>
          
          <div class="resource-info">
            <div class="resource-title" :title="resource.title">{{ resource.title }}</div>
            <div class="resource-meta">
              <el-tag size="small" type="primary">{{ resource.subject }}</el-tag>
              <el-tag size="small" type="info">{{ resource.type }}</el-tag>
            </div>
            <div class="resource-footer">
              <div class="resource-uploader">
                <el-icon><User /></el-icon>
                {{ resource.uploadedBy?.realName || '未知' }}
              </div>
              <div class="resource-time">
                {{ formatDate(resource.createdAt) }}
              </div>
            </div>
          </div>
        </div>
        
        <div class="resource-actions" @click.stop>
          <el-button size="small" type="primary" @click="previewResource(resource)">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button size="small" @click="downloadResource(resource)">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button size="small" type="danger" @click="deleteResource(resource.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="resources.length === 0" class="empty-state">
        <el-empty description="暂无资源">
          <el-button type="primary" @click="showUploadDialog = true">
            <el-icon><Upload /></el-icon>
            上传第一个资源
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else v-loading="loading" class="resources-list">
      <el-table :data="resources" stripe>
        <el-table-column prop="title" label="资源标题" show-overflow-tooltip min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="学科" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.subject }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadedBy.realName" label="上传者" width="120">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 6px;">
              <el-icon><User /></el-icon>
              {{ row.uploadedBy?.realName || '未知' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="previewResource(row)">
              <el-icon><View /></el-icon>
              在线查看
            </el-button>
            <el-button size="small" @click="downloadResource(row)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button size="small" type="danger" @click="deleteResource(row.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="resources.length === 0" description="暂无资源">
        <el-button type="primary" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          上传第一个资源
        </el-button>
      </el-empty>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreviewDialog"
      :title="currentResource?.title"
      width="80%"
      :close-on-click-modal="false"
      class="preview-dialog"
    >
      <div class="preview-container">
        <!-- PDF预览 -->
        <iframe
          v-if="isPreviewType('pdf')"
          :src="currentResource?.fileUrl"
          class="preview-frame"
          frameborder="0"
        />
        
        <!-- 图片预览 -->
        <img
          v-else-if="isPreviewType('image')"
          :src="currentResource?.fileUrl"
          class="preview-image"
          alt="预览"
        />
        
        <!-- 视频预览 -->
        <video
          v-else-if="isPreviewType('video')"
          :src="currentResource?.fileUrl"
          class="preview-video"
          controls
        />
        
        <!-- 文本预览 -->
        <iframe
          v-else-if="isPreviewType('doc')"
          :src="`https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(currentResource?.fileUrl || '')}`"
          class="preview-frame"
          frameborder="0"
        />
        
        <!-- 不支持预览 -->
        <div v-else class="preview-unsupported">
          <el-empty description="该文件类型不支持在线预览">
            <el-button v-if="currentResource" type="primary" @click="downloadResource(currentResource)">
              <el-icon><Download /></el-icon>
              下载查看
            </el-button>
          </el-empty>
        </div>
      </div>
      
      <template #footer>
        <el-space>
          <el-button @click="showPreviewDialog = false">关闭</el-button>
          <el-button v-if="currentResource" type="primary" @click="downloadResource(currentResource)">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </el-space>
      </template>
    </el-dialog>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传资源" width="600px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="资源标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入资源标题" />
        </el-form-item>
        
        <el-form-item label="资源类型" required>
          <el-select v-model="uploadForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="讲义" value="讲义" />
            <el-option label="课件" value="课件" />
            <el-option label="视频" value="视频" />
            <el-option label="练习题" value="练习题" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="学科" required>
          <el-select v-model="uploadForm.subject" placeholder="请选择" style="width: 100%">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="上传文件" required>
          <el-upload
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".pdf,.doc,.docx,.ppt,.pptx,.mp4,.avi"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、PPT、视频等格式，大小不超过100MB<br>
                教师端上传的资源无需审核，上传后立即可用
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Folder, Upload, Download, Delete, View, Document, VideoPlay, 
  Reading, EditPen, User, Search, Grid, List 
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

interface Resource {
  id: number
  title: string
  type: string
  subject: string
  fileUrl: string
  approved: boolean
  uploadedBy: any
  createdAt: string
}

const loading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const resources = ref<Resource[]>([])
const currentResource = ref<Resource | null>(null)
const filterType = ref('')
const filterSubject = ref('')
const searchKeyword = ref('')
const viewMode = ref('card') // 'card' 或 'table'

// 资源统计
const resourceStats = computed(() => {
  const total = resources.value.length
  const videoCount = resources.value.filter(r => r.type === '视频').length
  const docCount = resources.value.filter(r => r.type === '讲义' || r.type === '课件').length
  
  // 计算本周上传的资源
  const weekAgo = dayjs().subtract(7, 'day')
  const weekUploads = resources.value.filter(r => dayjs(r.createdAt).isAfter(weekAgo)).length
  
  return {
    total,
    videoCount,
    docCount,
    weekUploads
  }
})

const uploadForm = ref({
  title: '',
  type: '',
  subject: '',
  file: null as File | null
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchResources = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filterType.value) params.type = filterType.value
    if (filterSubject.value) params.subject = filterSubject.value
    
    const res = await api.get('/api/resources', { params })
    if (res.data.success) {
      resources.value = res.data.data || []
    } else {
      resources.value = []
    }
  } catch (error: any) {
    console.error('加载资源失败:', error)
    ElMessage.warning('暂无资源数据')
    resources.value = []
  } finally {
    loading.value = false
  }
}

const handleFileChange = (file: any) => {
  uploadForm.value.file = file.raw
}

const submitUpload = async () => {
  if (!uploadForm.value.title || !uploadForm.value.type || 
      !uploadForm.value.subject || !uploadForm.value.file) {
    ElMessage.warning('请填写完整信息并选择文件')
    return
  }
  
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadForm.value.file)
    formData.append('title', uploadForm.value.title)
    formData.append('type', uploadForm.value.type)
    formData.append('subject', uploadForm.value.subject)
    
    const res = await api.post('/api/resources/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    // 检查响应是否成功
    if (res.data.success) {
      ElMessage.success('上传成功！')
      showUploadDialog.value = false
      resetUploadForm()
      fetchResources()
    } else {
      ElMessage.error(res.data.message || '上传失败，请重试')
    }
  } catch (error: any) {
    console.error('上传资源失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '上传失败'
    
    // 根据不同的错误类型给出不同的提示
    if (error.response?.status === 404) {
      ElMessage.error('上传接口未实现，请联系管理员配置后端API')
    } else if (error.response?.status === 413) {
      ElMessage.error('文件过大，请选择小于100MB的文件')
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(errorMsg)
    }
  } finally {
    uploading.value = false
  }
}

// 获取完整的文件URL（兼容新旧格式）
const getFullFileUrl = (fileUrl: string) => {
  if (!fileUrl) return ''
  // 如果已经是完整URL，直接返回
  if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
    return fileUrl
  }
  // 否则拼接后端地址
  return `http://localhost:8080${fileUrl}`
}

// 预览资源
const previewResource = (resource: Resource) => {
  // 确保使用完整URL
  currentResource.value = {
    ...resource,
    fileUrl: getFullFileUrl(resource.fileUrl)
  }
  showPreviewDialog.value = true
}

// 判断预览类型
const isPreviewType = (type: string) => {
  if (!currentResource.value?.fileUrl) return false
  
  const url = currentResource.value.fileUrl.toLowerCase()
  
  if (type === 'pdf') {
    return url.endsWith('.pdf')
  } else if (type === 'image') {
    return url.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/)
  } else if (type === 'video') {
    return url.match(/\.(mp4|webm|ogg|avi|mov)$/)
  } else if (type === 'doc') {
    return url.match(/\.(doc|docx|ppt|pptx|xls|xlsx)$/)
  }
  
  return false
}

const downloadResource = (resource: Resource) => {
  const fullUrl = getFullFileUrl(resource.fileUrl)
  window.open(fullUrl, '_blank')
}

const deleteResource = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该资源吗？', '提示', {
      type: 'warning'
    })
    
    await api.delete(`/api/resources/${id}`)
    ElMessage.success('删除成功')
    fetchResources()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetUploadForm = () => {
  uploadForm.value = {
    title: '',
    type: '',
    subject: '',
    file: null
  }
}

onMounted(() => {
  fetchResources()
})
</script>

<style scoped>
.resources-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.filter-card {
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 12px;
}

.resources-list {
  background: white;
  padding: 20px;
  border-radius: 12px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-card-orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card-purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.9;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 筛选器 */
.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

/* 卡片视图 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  min-height: 400px;
}

.resource-card {
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}

.resource-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.resource-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.resource-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.resource-info {
  width: 100%;
  text-align: center;
}

.resource-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 48px;
}

.resource-meta {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 16px;
}

.resource-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e0e6ff;
  font-size: 13px;
  color: #909399;
}

.resource-uploader {
  display: flex;
  align-items: center;
  gap: 4px;
}

.resource-time {
  font-size: 12px;
}

.resource-actions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.resource-actions .el-button {
  flex: 1;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-dialog {
  height: 80vh;
}

.preview-container {
  width: 100%;
  height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.preview-frame {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-video {
  max-width: 100%;
  max-height: 100%;
}

.preview-unsupported {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}
</style>
