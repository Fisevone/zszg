<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Collection /></el-icon>
        知识点溯源
      </h1>
    </div>

    <!-- 学科选择 -->
    <el-card shadow="never" class="subject-card">
      <div class="subject-card-content">
        <el-radio-group v-model="selectedSubject" @change="fetchKnowledge">
          <el-radio-button label="数学">数学</el-radio-button>
          <el-radio-button label="语文">语文</el-radio-button>
          <el-radio-button label="英语">英语</el-radio-button>
          <el-radio-button label="物理">物理</el-radio-button>
          <el-radio-button label="化学">化学</el-radio-button>
          <el-radio-button label="生物">生物</el-radio-button>
        </el-radio-group>
        <el-button type="success" @click="extractKnowledgeFromErrors" :loading="extracting">
          <el-icon><MagicStick /></el-icon>
          AI提取知识点
        </el-button>
      </div>
    </el-card>

    <!-- 搜索框 -->
    <el-card shadow="never">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索知识点..."
        clearable
        @keyup.enter="searchKnowledge"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button :icon="Search" @click="searchKnowledge">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <!-- 知识点树 -->
    <el-card v-loading="loading" class="knowledge-tree-card">
      <el-tree
        :data="knowledgeTree"
        :props="treeProps"
        node-key="id"
        default-expand-all
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <el-icon><Notebook /></el-icon>
            <span>{{ node.label }}</span>
            <el-tag v-if="data.code" size="small" type="info">{{ data.code }}</el-tag>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 知识点详情 -->
    <el-dialog
      v-model="showDetail"
      :title="selectedKnowledge?.title"
      width="900px"
      destroy-on-close
    >
      <div v-if="selectedKnowledge" class="knowledge-detail">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学科">
            {{ selectedKnowledge.subject }}
          </el-descriptions-item>
          <el-descriptions-item label="编号">
            {{ selectedKnowledge.code || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 课本原文 -->
        <div v-if="selectedKnowledge.textbookRef" class="detail-section">
          <h3><el-icon><Reading /></el-icon> 课本原文</h3>
          <div class="content-box">{{ selectedKnowledge.textbookRef }}</div>
        </div>

        <!-- 教师笔记 -->
        <div v-if="selectedKnowledge.teacherNotesUrl" class="detail-section">
          <h3><el-icon><Edit /></el-icon> 教师笔记</h3>
          <el-link :href="selectedKnowledge.teacherNotesUrl" type="primary" target="_blank">
            <el-icon><Link /></el-icon>
            查看笔记
          </el-link>
        </div>

        <!-- 讲解视频 -->
        <div v-if="selectedKnowledge.videoUrl" class="detail-section">
          <h3><el-icon><VideoPlay /></el-icon> 讲解视频</h3>
          <el-link :href="selectedKnowledge.videoUrl" type="primary" target="_blank">
            <el-icon><Link /></el-icon>
            观看视频
          </el-link>
        </div>

        <!-- 相关题目推荐 -->
        <div class="detail-section">
          <h3><el-icon><List /></el-icon> 相关练习题</h3>
          <el-button type="primary" @click="goToRecommendations">
            查看推荐题目
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Collection,
  Search,
  Notebook,
  Reading,
  Edit,
  Link,
  VideoPlay,
  List,
  MagicStick
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const extracting = ref(false)
const selectedSubject = ref('数学')
const searchKeyword = ref('')
const knowledgeTree = ref<any[]>([])
const selectedKnowledge = ref<any>(null)
const showDetail = ref(false)

const treeProps = {
  children: 'children',
  label: 'title'
}

const fetchKnowledge = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/knowledge/root', {
      params: { subject: selectedSubject.value }
    })
    
    const roots = res.data.data || []
    
    // 递归加载子节点
    for (const root of roots) {
      await loadChildren(root)
    }
    
    knowledgeTree.value = roots
  } catch (error) {
    ElMessage.error('加载知识点失败')
  } finally {
    loading.value = false
  }
}

const loadChildren = async (node: any) => {
  try {
    const res = await api.get(`/api/knowledge/${node.id}/children`)
    const children = res.data.data || []
    
    if (children.length > 0) {
      node.children = children
      for (const child of children) {
        await loadChildren(child)
      }
    }
  } catch (error) {
    console.error('加载子节点失败:', error)
  }
}

const searchKnowledge = async () => {
  if (!searchKeyword.value.trim()) {
    fetchKnowledge()
    return
  }
  
  loading.value = true
  try {
    const res = await api.get('/api/knowledge/search', {
      params: { keyword: searchKeyword.value }
    })
    knowledgeTree.value = res.data.data || []
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleNodeClick = (data: any) => {
  selectedKnowledge.value = data
  showDetail.value = true
}

const goToRecommendations = () => {
  router.push('/student/recommend')
  showDetail.value = false
}

// AI提取知识点
const extractKnowledgeFromErrors = async () => {
  extracting.value = true
  ElMessage.info('AI正在从你的错题中提取知识点...')
  
  try {
    const res = await api.post('/api/ai/extract-knowledge', {
      subject: selectedSubject.value
    })
    
    const extractedKnowledge = res.data.data
    ElMessage.success(`已提取 ${extractedKnowledge.count || 0} 个知识点！`)
    
    // 重新加载知识点树
    await fetchKnowledge()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'AI提取失败')
  } finally {
    extracting.value = false
  }
}

onMounted(() => {
  fetchKnowledge()
})
</script>

<style scoped>
.subject-card {
  margin-bottom: 16px;
}

.subject-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.knowledge-tree-card {
  margin-top: 16px;
  min-height: 400px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.tree-node:hover {
  color: var(--primary-color);
}

.knowledge-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary-color);
  margin-bottom: 12px;
  font-size: 16px;
}

.content-box {
  padding: 16px;
  background: var(--bg-color);
  border-radius: 8px;
  line-height: 1.8;
  color: var(--text-regular);
}
</style>
