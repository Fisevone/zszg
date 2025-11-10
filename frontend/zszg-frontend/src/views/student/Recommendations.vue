<template>
  <div class="recommendations-page">
    <div class="page-header glass-card">
      <div>
        <h1 class="page-title">
          <el-icon><StarFilled /></el-icon>
          个性化推荐
        </h1>
        <p class="page-subtitle">基于你的错题历史，为你推荐相关练习题</p>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-card glass-card">
      <el-space wrap>
        <el-select v-model="selectedSubject" placeholder="选择学科" clearable @change="fetchRecommendations">
          <el-option label="数学" value="数学" />
          <el-option label="语文" value="语文" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
          <el-option label="生物" value="生物" />
        </el-select>
        
        <el-select v-model="selectedDifficulty" placeholder="选择难度" clearable @change="applyFilter">
          <el-option label="简单" value="简单" />
          <el-option label="中等" value="中等" />
          <el-option label="困难" value="困难" />
        </el-select>
        
        <el-button type="primary" @click="fetchRecommendations">
          <el-icon><Refresh /></el-icon>
          刷新推荐
        </el-button>
      </el-space>
    </div>

    <!-- 推荐题目列表 -->
    <div v-loading="loading" class="questions-list">
      <div v-for="question in filteredQuestions" :key="question.id" class="question-card glass-card">
        <div class="card-header">
          <div class="tags">
            <el-tag type="primary" effect="dark">{{ question.subject }}</el-tag>
            <el-tag :type="getDifficultyType(question.difficulty)" effect="plain">
              {{ question.difficulty }}
            </el-tag>
            <el-tag v-if="question.source" type="info" size="small">
              {{ question.source }}
            </el-tag>
          </div>
        </div>

        <div class="card-content">
          <div class="question-content" v-html="renderContent(question.content)"></div>
          
          <el-divider />
          
          <el-collapse>
            <el-collapse-item title="查看答案" name="answer">
              <div class="answer-section">
                <p><strong>答案：</strong>{{ question.answer }}</p>
                <p v-if="question.analysis"><strong>解析：</strong>{{ question.analysis }}</p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <div class="card-footer">
          <el-button type="primary" @click="addToErrorBook(question)">
            <el-icon><Plus /></el-icon>
            加入错题本
          </el-button>
          <el-button @click="markAsCompleted(question.id)">
            <el-icon><Check /></el-icon>
            已掌握
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredQuestions.length === 0" class="empty-state">
        <el-empty description="暂无推荐题目">
          <el-button type="primary" @click="fetchRecommendations">刷新</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, Refresh, Plus, Check } from '@element-plus/icons-vue'
import api from '@/lib/api'
import { renderMath } from '@/utils/mathRenderer'

// 渲染包含数学公式的内容
const renderContent = (content: string) => {
  return renderMath(content || '')
}

interface Question {
  id: number
  subject: string
  difficulty: string
  content: string
  answer: string
  analysis?: string
  source?: string
}

const loading = ref(false)
const recommendations = ref<Question[]>([])
const selectedSubject = ref('')
const selectedDifficulty = ref('')

const filteredQuestions = computed(() => {
  let result = recommendations.value
  
  if (selectedSubject.value) {
    result = result.filter(q => q.subject === selectedSubject.value)
  }
  
  if (selectedDifficulty.value) {
    result = result.filter(q => q.difficulty === selectedDifficulty.value)
  }
  
  return result
})

const fetchRecommendations = async () => {
  loading.value = true
  try {
    const params: any = { limit: 20 }
    
    if (selectedSubject.value) {
      const res = await api.get('/api/recommendations/by-subject', {
        params: { subject: selectedSubject.value, limit: 20 }
      })
      recommendations.value = res.data.data || []
    } else {
      const res = await api.get('/api/recommendations', { params })
      recommendations.value = res.data.data || []
    }
  } catch (error) {
    ElMessage.error('加载推荐失败')
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  // 过滤逻辑在 computed 中处理
}

const getDifficultyType = (difficulty: string) => {
  const types: any = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}

const addToErrorBook = async (question: Question) => {
  try {
    console.log('📝 准备添加到错题本:', question)
    
    const res = await api.post('/api/errorbook', {
      subject: question.subject || '数学',
      difficulty: question.difficulty || '中等',
      content: question.content,
      answer: question.answer || '',
      analysis: question.analysis || '',
      errorReason: '',
      correction: '',
      tags: '',
      images: '[]'  // 修复：使用有效的JSON格式
    })
    
    if (res.data.success || res.data.code === 200) {
      ElMessage.success({
        message: '✅ 已加入错题本！',
        duration: 2000
      })
    } else {
      ElMessage.error(res.data.message || '添加失败')
    }
  } catch (error: any) {
    console.error('添加到错题本失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '添加失败'
    ElMessage.error(errorMsg)
  }
}

const markAsCompleted = (questionId: number) => {
  recommendations.value = recommendations.value.filter(q => q.id !== questionId)
  ElMessage.success('已标记为掌握')
}

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.recommendations-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
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

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  padding: 24px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.question-card:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.card-header {
  margin-bottom: 16px;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.card-content {
  margin-bottom: 16px;
}

.question-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 16px;
}

.answer-section {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
}

.answer-section p {
  margin: 8px 0;
}

.card-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
</style>
