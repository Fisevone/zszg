<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Memo /></el-icon>
        题库管理
      </h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        添加题目
      </el-button>
    </div>

    <!-- 筛选器 -->
    <el-card shadow="never" class="filter-card">
      <el-space wrap>
        <el-select v-model="filters.subject" placeholder="学科" clearable>
          <el-option label="数学" value="数学" />
          <el-option label="语文" value="语文" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
          <el-option label="生物" value="生物" />
        </el-select>
        
        <el-select v-model="filters.difficulty" placeholder="难度" clearable>
          <el-option label="简单" value="简单" />
          <el-option label="中等" value="中等" />
          <el-option label="困难" value="困难" />
        </el-select>

        <el-button type="primary" @click="fetchQuestions">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" @click="importQuestions">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </el-space>
    </el-card>

    <!-- 题目列表 -->
    <el-card v-loading="loading">
      <el-table :data="questions" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="subject" label="学科" width="100">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.subject }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)">
              {{ row.difficulty || '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" type="primary" @click="viewQuestion(row)">
                查看
              </el-button>
              <el-button size="small" @click="editQuestion(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteQuestion(row.id)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingQuestion ? '编辑题目' : '添加题目'"
      width="800px"
    >
      <el-form :model="questionForm" label-width="100px">
        <el-form-item label="学科" required>
          <el-select v-model="questionForm.subject" placeholder="请选择学科">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度">
          <el-select v-model="questionForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" required>
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <el-form-item label="答案">
          <el-input
            v-model="questionForm.answer"
            type="textarea"
            :rows="3"
            placeholder="请输入答案"
          />
        </el-form-item>

        <el-form-item label="解析">
          <el-input
            v-model="questionForm.analysis"
            type="textarea"
            :rows="4"
            placeholder="请输入题目解析"
          />
        </el-form-item>

        <el-form-item label="来源">
          <el-input v-model="questionForm.source" placeholder="例如：2023年高考真题" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="题目详情" width="800px">
      <div v-if="selectedQuestion" class="question-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">
            {{ selectedQuestion.id }}
          </el-descriptions-item>
          <el-descriptions-item label="学科">
            {{ selectedQuestion.subject }}
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            {{ selectedQuestion.difficulty || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="来源">
            {{ selectedQuestion.source || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h3>题目内容</h3>
          <div class="content-box">{{ selectedQuestion.content }}</div>
        </div>

        <div v-if="selectedQuestion.answer" class="detail-section">
          <h3>答案</h3>
          <div class="content-box">{{ selectedQuestion.answer }}</div>
        </div>

        <div v-if="selectedQuestion.analysis" class="detail-section">
          <h3>解析</h3>
          <div class="content-box">{{ selectedQuestion.analysis }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Memo,
  Plus,
  Search,
  Upload
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const questions = ref<any[]>([])
const editingQuestion = ref<any>(null)
const selectedQuestion = ref<any>(null)

const filters = ref({
  subject: '',
  difficulty: ''
})

const questionForm = ref({
  subject: '',
  difficulty: '',
  content: '',
  answer: '',
  analysis: '',
  source: ''
})

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, any> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

const fetchQuestions = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.subject) params.subject = filters.value.subject
    if (filters.value.difficulty) params.difficulty = filters.value.difficulty
    
    const res = await api.get('/api/questions', { params })
    questions.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载题库失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { subject: '', difficulty: '' }
  fetchQuestions()
}

const viewQuestion = (question: any) => {
  selectedQuestion.value = question
  showDetailDialog.value = true
}

const editQuestion = (question: any) => {
  editingQuestion.value = question
  questionForm.value = {
    subject: question.subject,
    difficulty: question.difficulty || '',
    content: question.content,
    answer: question.answer || '',
    analysis: question.analysis || '',
    source: question.source || ''
  }
  showCreateDialog.value = true
}

const submitQuestion = async () => {
  if (!questionForm.value.subject || !questionForm.value.content) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    if (editingQuestion.value) {
      await api.put(`/api/questions/${editingQuestion.value.id}`, questionForm.value)
      ElMessage.success('更新成功')
    } else {
      await api.post('/api/questions', questionForm.value)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    fetchQuestions()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteQuestion = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/api/questions/${id}`)
    ElMessage.success('删除成功')
    fetchQuestions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const importQuestions = () => {
  ElMessage.info('批量导入功能开发中...')
}

const resetForm = () => {
  questionForm.value = {
    subject: '',
    difficulty: '',
    content: '',
    answer: '',
    analysis: '',
    source: ''
  }
  editingQuestion.value = null
}

onMounted(() => {
  fetchQuestions()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-card {
  margin-bottom: 20px;
}

.question-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section h3 {
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
