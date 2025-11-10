image.png<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><ChatLineSquare /></el-icon>
        任务推送
      </h1>
      <p class="page-subtitle">使用自然语言发布任务，AI自动解析并推送给学生</p>
    </div>

    <!-- AI智能解析卡片 -->
    <el-card class="ai-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <span class="header-title">AI智能任务发布</span>
            <el-tag type="success" size="small">自动解析</el-tag>
          </div>
          <el-button type="primary" :icon="Refresh" @click="clearForm">重置</el-button>
        </div>
      </template>

      <div class="publish-form">
        <!-- 选择班级 -->
        <div class="form-section">
          <label class="form-label">
            <el-icon><School /></el-icon>
            选择班级
          </label>
          <el-select 
            v-model="selectedClasses" 
            multiple 
            placeholder="选择要推送的班级"
            style="width: 100%"
          >
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            >
              <span>{{ cls.name }}</span>
              <span style="color: #999; font-size: 12px; margin-left: 8px">
                ({{ cls.studentCount }}人)
              </span>
            </el-option>
          </el-select>
        </div>

        <!-- 自然语言输入 -->
        <div class="form-section">
          <label class="form-label">
            <el-icon><Edit /></el-icon>
            任务描述（自然语言）
          </label>
          <el-input
            v-model="taskDescription"
            type="textarea"
            :rows="6"
            placeholder="请用自然语言描述任务，例如：
「明天下午三点之前，完成试卷的订正」

AI会自动解析：
✓ 截止时间：明天 15:00
✓ 任务类型：学习任务
✓ 数量要求：不限
✓ 优先级：普通"
          />
          <div class="input-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>提示：输入任务描述后，点击"智能解析"按钮，AI会自动识别时间、任务、要求等关键信息</span>
          </div>
          
          <!-- 智能解析按钮 -->
          <div class="parse-button-wrapper">
            <el-button 
              type="primary" 
              :loading="parsing"
              :disabled="!taskDescription.trim()"
              @click="parseTask"
              size="large"
            >
              <el-icon><MagicStick /></el-icon>
              <span>{{ parsing ? 'AI智能解析中...' : '智能解析' }}</span>
            </el-button>
          </div>
        </div>

        <!-- AI解析结果预览 -->
        <transition name="slide-fade">
          <div v-if="parsedResult && taskDescription" class="parsed-result">
            <div class="result-header">
              <el-icon class="parsing-icon"><Loading v-if="parsing" /><Check v-else /></el-icon>
              <span class="result-title">{{ parsing ? 'AI正在解析...' : 'AI解析结果' }}</span>
            </div>
            
            <div v-if="!parsing" class="result-content">
              <!-- 基础信息网格 -->
              <div class="result-grid">
                <div class="result-item">
                  <div class="item-label">📅 截止时间</div>
                  <div class="item-value">{{ parsedResult.deadline || '未指定' }}</div>
                </div>
                <div class="result-item">
                  <div class="item-label">📋 任务类型</div>
                  <div class="item-value">{{ parsedResult.taskType || '未指定' }}</div>
                </div>
                <div class="result-item">
                  <div class="item-label">🎯 数量要求</div>
                  <div class="item-value">{{ parsedResult.quantity || '不限' }}</div>
                </div>
                <div class="result-item">
                  <div class="item-label">⭐ 优先级</div>
                  <div class="item-value">
                    <el-tag :type="getPriorityType(parsedResult.priority)" size="small">
                      {{ parsedResult.priority || '普通' }}
                    </el-tag>
                  </div>
                </div>
                
                <!-- 新增字段 -->
                <div v-if="parsedResult.difficulty" class="result-item">
                  <div class="item-label">💪 难度等级</div>
                  <div class="item-value">
                    <el-tag :type="getDifficultyType(parsedResult.difficulty)" size="small">
                      {{ parsedResult.difficulty }}
                    </el-tag>
                  </div>
                </div>
                <div v-if="parsedResult.subject" class="result-item">
                  <div class="item-label">📚 学科</div>
                  <div class="item-value">{{ parsedResult.subject }}</div>
                </div>
                <div v-if="parsedResult.timeRequirement" class="result-item">
                  <div class="item-label">⏱️ 预计时长</div>
                  <div class="item-value">{{ parsedResult.timeRequirement }}</div>
                </div>
              </div>

              <div v-if="parsedResult.keywords && parsedResult.keywords.length > 0" class="keywords-section">
                <div class="section-label">🔑 关键知识点</div>
                <div class="keywords-list">
                  <el-tag
                    v-for="(keyword, index) in parsedResult.keywords"
                    :key="index"
                    type="info"
                    size="small"
                  >
                    {{ keyword }}
                  </el-tag>
                </div>
              </div>

              <div v-if="parsedResult.requirements && parsedResult.requirements.length > 0" class="requirements-section">
                <div class="section-label">⚠️ 重要提醒</div>
                <ul class="requirements-list">
                  <li v-for="(req, index) in parsedResult.requirements" :key="index">
                    {{ req }}
                  </li>
                </ul>
              </div>

              <!-- 子任务展示 -->
              <div v-if="parsedResult.subTasks && parsedResult.subTasks.length > 0" class="subtasks-section">
                <div class="section-label">📋 AI智能拆解任务</div>
                <div class="subtasks-list">
                  <div v-for="(task, index) in parsedResult.subTasks" :key="index" class="subtask-item">
                    <div class="subtask-number">{{ index + 1 }}</div>
                    <div class="subtask-content">
                      <div class="subtask-name">{{ task.name }}</div>
                      <div class="subtask-desc">{{ task.description }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 地点和参与人群 -->
              <div v-if="parsedResult.location || parsedResult.participants" class="extra-info">
                <div v-if="parsedResult.location" class="info-item">
                  <span class="info-label">📍 地点：</span>
                  <span class="info-value">{{ parsedResult.location }}</span>
                </div>
                <div v-if="parsedResult.participants" class="info-item">
                  <span class="info-label">👥 参与人群：</span>
                  <span class="info-value">{{ parsedResult.participants }}</span>
                </div>
              </div>

              <!-- 学习目标 -->
              <div v-if="parsedResult.objectives && parsedResult.objectives.length > 0" class="objectives-section">
                <div class="section-label">🎓 学习目标</div>
                <ul class="objectives-list">
                  <li v-for="(obj, index) in parsedResult.objectives" :key="index">
                    {{ obj }}
                  </li>
                </ul>
              </div>

              <!-- 需要的材料 -->
              <div v-if="parsedResult.materials && parsedResult.materials.length > 0" class="materials-section">
                <div class="section-label">📦 需要准备</div>
                <div class="materials-list">
                  <el-tag
                    v-for="(material, index) in parsedResult.materials"
                    :key="index"
                    type="success"
                    size="small"
                  >
                    {{ material }}
                  </el-tag>
                </div>
              </div>

              <!-- 学习建议 -->
              <div v-if="parsedResult.tips && parsedResult.tips.length > 0" class="tips-section">
                <div class="section-label">💡 学习建议</div>
                <ul class="tips-list">
                  <li v-for="(tip, index) in parsedResult.tips" :key="index">
                    {{ tip }}
                  </li>
                </ul>
              </div>

              <!-- 评价标准 -->
              <div v-if="parsedResult.evaluationCriteria && parsedResult.evaluationCriteria.length > 0" class="evaluation-section">
                <div class="section-label">📊 评价标准</div>
                <ul class="evaluation-list">
                  <li v-for="(criterion, index) in parsedResult.evaluationCriteria" :key="index">
                    {{ criterion }}
                  </li>
                </ul>
              </div>

              <!-- 预期成果 -->
              <div v-if="parsedResult.expectedOutcome" class="outcome-section">
                <div class="section-label">🏆 预期成果</div>
                <div class="outcome-content">{{ parsedResult.expectedOutcome }}</div>
              </div>

              <!-- 相关主题 -->
              <div v-if="parsedResult.relatedTopics && parsedResult.relatedTopics.length > 0" class="related-section">
                <div class="section-label">🔗 相关主题</div>
                <div class="related-list">
                  <el-tag
                    v-for="(topic, index) in parsedResult.relatedTopics"
                    :key="index"
                    type="warning"
                    size="small"
                  >
                    {{ topic }}
                  </el-tag>
                </div>
              </div>

              <!-- AI备注 -->
              <div v-if="parsedResult.aiNotes" class="ai-notes">
                <div class="notes-header">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI智能分析</span>
                </div>
                <div class="notes-content">{{ parsedResult.aiNotes }}</div>
              </div>

              <!-- 可以手动调整 -->
              <div class="manual-adjust">
                <el-button text type="primary" @click="showAdjustDialog = true">
                  <el-icon><Edit /></el-icon>
                  手动调整解析结果
                </el-button>
              </div>
            </div>
          </div>
        </transition>

        <!-- 发布按钮 -->
        <div class="form-actions">
          <el-button
            type="primary"
            size="large"
            :loading="publishing"
            :disabled="!canPublish"
            @click="publishTask"
          >
            <el-icon><Promotion /></el-icon>
            <span>{{ publishing ? '发布中...' : '发布任务' }}</span>
          </el-button>
          <el-button size="large" @click="clearForm">取消</el-button>
        </div>
      </div>
    </el-card>

    <!-- 历史任务列表 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Clock /></el-icon>
            <span class="header-title">历史任务</span>
          </div>
        </div>
      </template>

      <el-table :data="historyTasks" stripe>
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="classes" label="推送班级" width="150">
          <template #default="{ row }">
            <el-tag v-for="cls in row.classes" :key="cls" size="small" style="margin: 2px">
              {{ cls }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column label="完成情况" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="row.completionRate"
              :color="getProgressColor(row.completionRate)"
            >
              <span>{{ row.completed }}/{{ row.total }}</span>
            </el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewTaskDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 手动调整对话框 -->
    <el-dialog
      v-model="showAdjustDialog"
      title="调整解析结果"
      width="600px"
    >
      <el-form :model="parsedResult" label-width="100px">
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="adjustedDeadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="parsedResult.taskType" style="width: 100%">
            <el-option label="错题整理" value="错题整理" />
            <el-option label="知识点复习" value="知识点复习" />
            <el-option label="习题练习" value="习题练习" />
            <el-option label="测验准备" value="测验准备" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量要求">
          <el-input v-model="parsedResult.quantity" placeholder="例如：5道题" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="parsedResult.priority">
            <el-radio label="低">低</el-radio>
            <el-radio label="普通">普通</el-radio>
            <el-radio label="高">高</el-radio>
            <el-radio label="紧急">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdjustDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust">确认</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="currentTask?.title"
      width="800px"
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务类型">
            {{ currentTask.taskType }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ currentTask.deadline }}
          </el-descriptions-item>
          <el-descriptions-item label="数量要求">
            {{ currentTask.quantity }}
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentTask.priority)">
              {{ currentTask.priority }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成情况" :span="2">
            <el-progress
              :percentage="currentTask.completionRate"
              :color="getProgressColor(currentTask.completionRate)"
            >
              <span>{{ currentTask.completed }}/{{ currentTask.total }}</span>
            </el-progress>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>任务描述</el-divider>
        <p class="task-description">{{ currentTask.description }}</p>

        <el-divider>学生完成情况</el-divider>
        <el-table :data="currentTask.students" stripe max-height="300">
          <el-table-column prop="name" label="学生姓名" width="120" />
          <el-table-column label="完成状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.completed ? 'success' : 'info'">
                {{ row.completed ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completedAt" label="完成时间" width="180" />
          <el-table-column prop="note" label="备注" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineSquare,
  MagicStick,
  School,
  Edit,
  InfoFilled,
  Check,
  Loading,
  Promotion,
  Clock,
  Refresh
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

// 响应式数据
const selectedClasses = ref<number[]>([])
const taskDescription = ref('')
const parsedResult = ref<any>(null)
const parsing = ref(false)
const publishing = ref(false)
const showAdjustDialog = ref(false)
const showDetailDialog = ref(false)
const adjustedDeadline = ref<Date>()
const currentTask = ref<any>(null)

// 班级列表 - 从后端动态获取
const classList = ref<Array<{id: number, name: string, studentCount: number}>>([])

const historyTasks = ref([
  {
    id: 1,
    title: '第三章错题整理',
    classes: ['高一1班', '高一2班'],
    taskType: '错题整理',
    deadline: '2025-10-30 18:00',
    quantity: '5道题',
    priority: '普通',
    completed: 65,
    total: 87,
    completionRate: 75,
    createdAt: new Date(Date.now() - 2 * 86400000).toISOString(),
    description: '同学们，本周五前完成第三章函数与导数的错题整理...',
    students: [
      { name: '张三', completed: true, completedAt: '2025-10-28 15:30', note: '已完成6道题' },
      { name: '李四', completed: false, completedAt: '', note: '' }
    ]
  }
])

// AI解析任务（手动触发）
const parseTask = async () => {
  if (!taskDescription.value.trim()) {
    ElMessage.warning('请先输入任务描述')
    return
  }

  parsing.value = true
  try {
    // 调用智能任务NLP解析接口
    const res = await api.post('/api/tasks/parse-preview', {
      content: taskDescription.value
    })
    
    if (res.data.success) {
      const data = res.data.data
      
      // 格式化截止时间
      let deadlineText = '未指定'
      if (data.deadline) {
        const deadlineDate = new Date(data.deadline)
        deadlineText = dayjs(deadlineDate).format('YYYY-MM-DD HH:mm')
      }
      
      parsedResult.value = {
        // 基础信息
        title: data.title,
        deadline: deadlineText,
        taskType: data.taskType || '学习任务',
        quantity: data.quantityRequirement || '不限',
        priority: data.priority || '普通',
        
        // 详细信息
        location: data.location,
        participants: data.participants,
        difficulty: data.difficulty,
        subject: data.subject,
        timeRequirement: data.timeRequirement,
        
        // 智能分析
        keywords: data.smartTags || [],
        knowledgePoints: data.knowledgePoints || [],
        requirements: data.importantReminders || data.reminders || [],
        subTasks: data.subTasks || [],
        
        // 学习指导
        objectives: data.objectives || [],
        materials: data.materials || [],
        tips: data.tips || [],
        evaluationCriteria: data.evaluationCriteria || [],
        relatedTopics: data.relatedTopics || [],
        expectedOutcome: data.expectedOutcome,
        
        // AI备注
        aiNotes: data.aiNotes
      }
      
      // 统计解析出的字段数量
      const fieldsCount = Object.values(parsedResult.value).filter(v => 
        (Array.isArray(v) && v.length > 0) || (v && !Array.isArray(v) && v !== '未指定' && v !== '不限' && v !== '学习任务' && v !== '普通')
      ).length
      
      ElMessage.success(`✅ AI智能解析完成 - 提取了 ${fieldsCount} 项任务信息`)
    }
  } catch (error) {
    console.error('解析失败:', error)
    ElMessage.error('AI解析失败，请重试')
    // 降级方案：使用本地简单解析
    parsedResult.value = simpleParseTask(taskDescription.value)
    ElMessage.warning('已使用本地解析（功能有限）')
  } finally {
    parsing.value = false
  }
}

// 简单本地解析（降级方案）
const simpleParseTask = (text: string) => {
  const result: any = {
    deadline: null,
    taskType: '学习任务',
    quantity: null,
    priority: '普通',
    keywords: [],
    requirements: []
  }

  // 提取时间
  const timePatterns = [
    /([本这]周[一二三四五六日天]|明天|后天|周[一二三四五六日])/,
    /(\d+月\d+[日号])/,
    /(\d+[日号])/
  ]
  for (const pattern of timePatterns) {
    const match = text.match(pattern)
    if (match) {
      result.deadline = match[1]
      break
    }
  }

  // 提取任务类型
  if (text.includes('错题')) result.taskType = '错题整理'
  if (text.includes('复习')) result.taskType = '知识点复习'
  if (text.includes('练习')) result.taskType = '习题练习'
  if (text.includes('测验') || text.includes('考试')) result.taskType = '测验准备'

  // 提取数量
  const quantityMatch = text.match(/(\d+)道/)
  if (quantityMatch) {
    result.quantity = `${quantityMatch[1]}道题`
  }

  // 提取优先级
  if (text.includes('紧急') || text.includes('立即') || text.includes('马上')) {
    result.priority = '紧急'
  } else if (text.includes('重要') || text.includes('必须')) {
    result.priority = '高'
  }

  // 提取关键词
  const subjects = ['函数', '导数', '积分', '几何', '代数', '三角函数', '向量', '概率', '统计']
  result.keywords = subjects.filter(s => text.includes(s))

  // 提取要求
  if (text.includes('标注知识点')) result.requirements.push('标注知识点')
  if (text.includes('写错因')) result.requirements.push('写清楚错因分析')
  if (text.includes('订正')) result.requirements.push('完成订正')

  return result
}

// 发布任务
const publishTask = async () => {
  if (!selectedClasses.value.length) {
    ElMessage.warning('请选择至少一个班级')
    return
  }

  publishing.value = true
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    
    // 为每个选中的班级创建并发布任务
    for (const classId of selectedClasses.value) {
      const targetClass = classList.value.find(c => c.id === classId)
      
      // 1. 创建任务
      const createRes = await api.post('/api/tasks', {
        teacherId: user.id,
        teacherName: user.realName || user.username,
        classId: String(classId),
        className: targetClass?.name || `班级${classId}`,
        title: parsedResult.value?.title || taskDescription.value.substring(0, 30),
        content: taskDescription.value,
        useAI: true
      })

      if (createRes.data.success) {
        const taskId = createRes.data.data.id
        
        // 2. 发布任务（下发给学生）
        await api.post(`/api/tasks/${taskId}/publish`)
      }
    }

    ElMessage.success('任务发布成功！已推送给所选班级的学生')
    clearForm()
    // 刷新历史列表
    fetchHistoryTasks()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    publishing.value = false
  }
}

// 获取历史任务列表
const fetchHistoryTasks = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const res = await api.get(`/api/tasks/teacher/${user.id}`)
    
    if (res.data.success) {
      historyTasks.value = res.data.data.map((task: any) => ({
        id: task.id,
        title: task.title,
        classes: [task.className],
        taskType: task.taskType,
        deadline: task.deadline || '未指定',
        quantity: task.quantityRequirement,
        priority: task.priority,
        completed: task.completedStudents || 0,
        total: task.totalStudents || 0,
        completionRate: task.completionRate || 0,
        createdAt: task.createdAt,
        description: task.content,
        students: []
      }))
    }
  } catch (error) {
    console.error('获取历史任务失败:', error)
  }
}

// 工具函数
const canPublish = computed(() => {
  return selectedClasses.value.length > 0 && 
         taskDescription.value.trim() && 
         parsedResult.value
})

const getPriorityType = (priority: string) => {
  const map: any = {
    '低': 'info',
    '普通': '',
    '高': 'warning',
    '紧急': 'danger',
    '特急': 'danger'
  }
  return map[priority] || ''
}

const getDifficultyType = (difficulty: string) => {
  const map: any = {
    '简单': 'success',
    '中等': '',
    '困难': 'warning',
    '挑战': 'danger'
  }
  return map[difficulty] || ''
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const clearForm = () => {
  selectedClasses.value = []
  taskDescription.value = ''
  parsedResult.value = null
}

const confirmAdjust = () => {
  if (adjustedDeadline.value) {
    parsedResult.value.deadline = dayjs(adjustedDeadline.value).format('YYYY-MM-DD HH:mm')
  }
  showAdjustDialog.value = false
  ElMessage.success('已更新解析结果')
}

const viewTaskDetail = (task: any) => {
  currentTask.value = task
  showDetailDialog.value = true
}

// 获取班级列表
const fetchClassList = async () => {
  try {
    const res = await api.get('/api/classroom/teacher/classes')
    if (res.data.success && res.data.data.length > 0) {
      classList.value = res.data.data.map((cls: any) => ({
        id: cls.id,
        name: cls.name || cls.className,
        studentCount: cls.studentCount || 0
      }))
      ElMessage.success(`已加载 ${classList.value.length} 个班级`)
    } else {
      // 如果没有班级，清空列表并提示
      classList.value = []
      ElMessage.warning('您还没有创建班级，请先在"班级管理"中创建班级')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    classList.value = []
    ElMessage.error('获取班级列表失败，请检查网络连接')
  }
}

onMounted(() => {
  fetchClassList()
  fetchHistoryTasks()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.ai-card, .history-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-icon {
  color: #667eea;
  font-size: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.input-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 13px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.parse-button-wrapper {
  margin-top: 16px;
  text-align: center;
}

/* AI解析结果 */
.parsed-result {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 24px;
  animation: slideIn 0.3s ease;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.parsing-icon {
  font-size: 24px;
  color: #667eea;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.result-item {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.item-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.item-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.keywords-section,
.requirements-section {
  margin-top: 16px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  margin-bottom: 12px;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements-list li {
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  margin-bottom: 8px;
  color: #666;
}

.requirements-list li::before {
  content: '✓';
  color: #67c23a;
  font-weight: bold;
  margin-right: 8px;
}

/* 子任务展示 */
.subtasks-section {
  margin-top: 16px;
}

.subtasks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subtask-item {
  display: flex;
  gap: 12px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.subtask-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.subtask-content {
  flex: 1;
}

.subtask-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.subtask-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 额外信息 */
.extra-info {
  margin-top: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-label {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

/* AI备注 */
.ai-notes {
  margin-top: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  padding: 16px;
}

.notes-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
}

.notes-content {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  white-space: pre-line;
}

/* 学习目标、建议、评价等列表样式 */
.objectives-section,
.tips-section,
.evaluation-section {
  margin-top: 16px;
}

.objectives-list,
.tips-list,
.evaluation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.objectives-list li,
.tips-list li,
.evaluation-list li {
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  margin-bottom: 8px;
  color: #666;
  border-left: 3px solid #667eea;
}

.objectives-list li::before {
  content: '🎯';
  margin-right: 8px;
}

.tips-list li::before {
  content: '💡';
  margin-right: 8px;
}

.evaluation-list li::before {
  content: '✓';
  color: #67c23a;
  font-weight: bold;
  margin-right: 8px;
}

/* 材料和相关主题标签列表 */
.materials-section,
.related-section {
  margin-top: 16px;
}

.materials-list,
.related-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 预期成果 */
.outcome-section {
  margin-top: 16px;
}

.outcome-content {
  padding: 16px;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(102, 126, 234, 0.1));
  border: 1px solid rgba(103, 194, 58, 0.3);
  border-radius: 8px;
  color: #666;
  line-height: 1.6;
  font-weight: 500;
}

.manual-adjust {
  margin-top: 16px;
  text-align: center;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.task-detail {
  padding: 16px 0;
}

.task-description {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.6;
  color: #666;
}

/* 动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

