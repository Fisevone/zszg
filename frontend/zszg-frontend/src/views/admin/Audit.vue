<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Select /></el-icon>
        内容审核
      </h1>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 共享错题审核 -->
      <el-tab-pane label="共享错题审核" name="shares">
        <template #label>
          <span class="tab-label">
            <el-icon><Document /></el-icon>
            共享错题审核
            <el-badge :value="pendingShares.length" class="item" />
          </span>
        </template>

        <div v-loading="sharesLoading" class="audit-content">
          <el-empty v-if="pendingShares.length === 0" description="暂无待审核内容" />
          
          <div v-else class="audit-list">
            <el-card
              v-for="share in pendingShares"
              :key="share.id"
              class="audit-card"
              shadow="hover"
            >
              <div class="audit-header">
                <div class="audit-meta">
                  <el-tag type="primary">{{ share.errorBook.question.subject }}</el-tag>
                  <el-tag type="warning">{{ share.scope }}</el-tag>
                  <el-text type="info">ID: {{ share.id }}</el-text>
                </div>
              </div>

              <div class="audit-body">
                <div class="section">
                  <h4>题目内容：</h4>
                  <p>{{ share.errorBook.question.content }}</p>
                </div>

                <div v-if="share.errorBook.errorReason" class="section">
                  <h4>错因分析：</h4>
                  <p>{{ share.errorBook.errorReason }}</p>
                </div>

                <div v-if="share.errorBook.correction" class="section">
                  <h4>订正内容：</h4>
                  <p>{{ share.errorBook.correction }}</p>
                </div>

                <div v-if="share.tags" class="section">
                  <h4>标签：</h4>
                  <el-tag v-for="tag in share.tags.split(',')" :key="tag" size="small">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>

              <div class="audit-footer">
                <el-button type="success" @click="reviewShare(share.id, true)">
                  <el-icon><Select /></el-icon>
                  通过
                </el-button>
                <el-button type="danger" @click="reviewShare(share.id, false)">
                  <el-icon><CloseBold /></el-icon>
                  拒绝
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- 资源审核 -->
      <el-tab-pane label="资源审核" name="resources">
        <template #label>
          <span class="tab-label">
            <el-icon><Folder /></el-icon>
            资源审核
            <el-badge :value="pendingResources.length" class="item" />
          </span>
        </template>

        <div v-loading="resourcesLoading" class="audit-content">
          <el-empty v-if="pendingResources.length === 0" description="暂无待审核资源" />
          
          <el-table v-else :data="pendingResources" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="资源名称" min-width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getResourceTypeTag(row.type)">
                  {{ getResourceTypeLabel(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="subject" label="学科" width="100" />
            <el-table-column label="上传时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-space>
                  <el-button size="small" type="primary" @click="previewResource(row)">
                    预览
                  </el-button>
                  <el-button size="small" type="success" @click="reviewResource(row.id, true)">
                    通过
                  </el-button>
                  <el-button size="small" type="danger" @click="reviewResource(row.id, false)">
                    拒绝
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Select,
  Document,
  Folder,
  CloseBold
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

const activeTab = ref('shares')
const sharesLoading = ref(false)
const resourcesLoading = ref(false)
const pendingShares = ref<any[]>([])
const pendingResources = ref<any[]>([])

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getResourceTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'PAPER': '试卷',
    'VIDEO': '视频',
    'NOTES': '笔记'
  }
  return map[type] || type
}

const getResourceTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'PAPER': 'primary',
    'VIDEO': 'success',
    'NOTES': 'warning'
  }
  return map[type] || 'info'
}

const fetchPendingShares = async () => {
  sharesLoading.value = true
  try {
    const res = await api.get('/api/sharepool/pending')
    pendingShares.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载待审核共享失败')
  } finally {
    sharesLoading.value = false
  }
}

const fetchPendingResources = async () => {
  resourcesLoading.value = true
  try {
    const res = await api.get('/api/resources/pending')
    pendingResources.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载待审核资源失败')
  } finally {
    resourcesLoading.value = false
  }
}

const reviewShare = async (id: number, approve: boolean) => {
  try {
    await api.post(`/api/sharepool/${id}/review`, null, {
      params: { approve }
    })
    ElMessage.success(approve ? '已通过审核' : '已拒绝')
    fetchPendingShares()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const reviewResource = async (id: number, approve: boolean) => {
  try {
    await api.post(`/api/resources/${id}/review`, null, {
      params: { approve }
    })
    ElMessage.success(approve ? '已通过审核' : '已拒绝')
    fetchPendingResources()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const previewResource = (resource: any) => {
  window.open(resource.fileUrl, '_blank')
}

onMounted(() => {
  fetchPendingShares()
  fetchPendingResources()
})
</script>

<style scoped>
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.audit-content {
  min-height: 400px;
  padding: 20px;
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audit-card {
  animation: fadeIn 0.3s ease;
}

.audit-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--bg-color);
}

.audit-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.audit-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.section h4 {
  color: var(--primary-color);
  margin-bottom: 8px;
  font-size: 14px;
}

.section p {
  line-height: 1.6;
  color: var(--text-regular);
  padding: 12px;
  background: var(--bg-color);
  border-radius: 6px;
  margin: 0;
}

.audit-footer {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}
</style>
