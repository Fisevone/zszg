<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Bell /></el-icon>
        教师推送
      </h1>
      <p class="page-subtitle">查看教师布置的学习任务</p>
    </div>

    <!-- 任务统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card pending">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <div class="stat-value">{{ pendingTasks.length }}</div>
          <div class="stat-label">待完成</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ completedTasks.length }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 待完成任务 -->
    <el-card class="tasks-section">
      <template #header>
        <div class="section-header">
          <el-icon><Clock /></el-icon>
          <span>待完成任务</span>
          <el-badge :value="pendingTasks.length" class="badge" />
        </div>
      </template>

      <el-empty v-if="pendingTasks.length === 0" description="暂无待完成任务" />

      <div v-else class="tasks-list">
        <div 
          v-for="task in pendingTasks" 
          :key="task.id"
          class="task-card"
          :class="{ 
            urgent: task.priority === '紧急' || task.priority === '高',
            'is-new': isNewTask(task)
          }"
        >
          <!-- 新任务标记 -->
          <div v-if="isNewTask(task)" class="new-task-badge">
            🆕 新任务
          </div>
          
          <!-- 优先级标签 -->
          <div v-if="task.priority && task.priority !== '普通'" class="priority-badge" :class="task.priority">
            {{ task.priority }}
          </div>

          <div class="task-header">
            <h3 class="task-title">{{ task.title }}</h3>
            <el-tag :type="getTaskTypeColor(task.taskType)" size="small">
              {{ task.taskType }}
            </el-tag>
          </div>

          <div class="task-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ task.teacherName }} · {{ task.className }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDeadline(task.deadline) }}</span>
            </div>
          </div>

          <div class="task-content">
            <p>{{ task.description }}</p>
          </div>

          <!-- 结构化信息 -->
          <div v-if="task.keywords && task.keywords.length > 0" class="task-keywords">
            <span class="label">🔑 关键知识点：</span>
            <el-tag
              v-for="(keyword, index) in task.keywords"
              :key="index"
              size="small"
              type="info"
            >
              {{ keyword }}
            </el-tag>
          </div>

          <div v-if="task.quantity" class="task-requirement">
            <span class="label">🎯 数量要求：</span>
            <span class="value">{{ task.quantity }}</span>
          </div>

          <!-- 进度条 -->
          <div class="task-progress">
            <div class="progress-header">
              <span>完成进度</span>
              <span class="progress-text">{{ task.completedCount || 0 }}/{{ task.totalCount || 0 }}</span>
            </div>
            <el-progress
              :percentage="getProgressPercentage(task)"
              :color="getProgressColor(getProgressPercentage(task))"
              :stroke-width="8"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="task-actions">
            <el-button type="primary" @click="startTask(task)">
              <el-icon><Edit /></el-icon>
              {{ task.progress > 0 ? '继续完成' : '开始任务' }}
            </el-button>
            <el-button @click="viewTaskDetail(task)">查看详情</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 已完成任务 -->
    <el-card class="tasks-section">
      <template #header>
        <div class="section-header">
          <el-icon><CircleCheck /></el-icon>
          <span>已完成任务</span>
        </div>
      </template>

      <el-empty v-if="completedTasks.length === 0" description="暂无已完成任务" />

      <div v-else class="tasks-list completed-list">
        <div 
          v-for="task in completedTasks" 
          :key="task.id"
          class="task-card completed"
        >
          <div class="task-header">
            <h3 class="task-title">{{ task.title }}</h3>
            <el-tag type="success" size="small">已完成</el-tag>
          </div>

          <div class="task-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ task.teacherName }} · {{ task.className }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>完成于 {{ formatDate(task.completedAt) }}</span>
            </div>
          </div>

          <div class="task-actions">
            <el-button size="small" @click="viewTaskDetail(task)">查看详情</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="currentTask?.title"
      width="700px"
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeColor(currentTask.taskType)">
              {{ currentTask.taskType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTypeColor(currentTask.priority)">
              {{ currentTask.priority || '普通' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ formatDeadline(currentTask.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="数量要求">
            {{ currentTask.quantity || '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="发布教师" :span="2">
            {{ currentTask.teacherName }}（{{ currentTask.className }}）
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>任务描述</el-divider>
        <p class="task-description">{{ currentTask.description }}</p>

        <div v-if="currentTask.keywords && currentTask.keywords.length > 0">
          <el-divider>关键知识点</el-divider>
          <div class="keywords-display">
            <el-tag
              v-for="(keyword, index) in currentTask.keywords"
              :key="index"
              type="info"
              size="large"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </div>

        <div v-if="currentTask.requirements && currentTask.requirements.length > 0">
          <el-divider>具体要求</el-divider>
          <ul class="requirements-list">
            <li v-for="(req, index) in currentTask.requirements" :key="index">
              {{ req }}
            </li>
          </ul>
        </div>

        <div v-if="currentTask.status !== 'completed'">
          <el-divider>完成进度</el-divider>
          <el-progress
            :percentage="getProgressPercentage(currentTask)"
            :color="getProgressColor(getProgressPercentage(currentTask))"
          >
            <span>{{ currentTask.completedCount || 0 }}/{{ currentTask.totalCount || 0 }}</span>
          </el-progress>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button v-if="currentTask?.status !== 'completed'" type="primary" @click="startTask(currentTask)">
          前往完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  Clock,
  User,
  Edit,
  CircleCheck
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

const router = useRouter()
const showDetailDialog = ref(false)
const currentTask = ref<any>(null)

// 模拟任务数据
const allTasks = ref([
  {
    id: 1,
    title: '第三章错题整理',
    description: '同学们，本周五前完成第三章函数与导数的错题整理，每人至少录入5道错题，重点关注求导和极值问题。完成后记得标注知识点。',
    taskType: '错题整理',
    deadline: new Date(Date.now() + 2 * 86400000).toISOString(),
    quantity: '至少5道题',
    priority: '普通',
    keywords: ['函数', '导数', '求导', '极值'],
    requirements: ['标注知识点', '完成错因分析', '上传订正过程'],
    teacherName: '张老师',
    className: '高一1班',
    status: 'pending',
    completedCount: 2,
    totalCount: 5,
    progress: 40
  },
  {
    id: 2,
    title: '数学练习题',
    description: '明天前完成课本第45-48页的选择题',
    taskType: '习题练习',
    deadline: new Date(Date.now() + 86400000).toISOString(),
    quantity: '课本45-48页选择题',
    priority: '紧急',
    keywords: ['选择题'],
    requirements: ['独立完成', '不懂的标记出来'],
    teacherName: '李老师',
    className: '高一1班',
    status: 'pending',
    completedCount: 0,
    totalCount: 20,
    progress: 0
  },
  {
    id: 3,
    title: '第二章复习',
    description: '复习第二章内容，准备下周测验',
    taskType: '知识点复习',
    deadline: new Date(Date.now() - 86400000).toISOString(),
    teacherName: '张老师',
    className: '高一1班',
    status: 'completed',
    completedAt: new Date(Date.now() - 2 * 86400000).toISOString(),
    completedCount: 10,
    totalCount: 10,
    progress: 100
  }
])

const pendingTasks = computed(() => 
  allTasks.value.filter(t => t.status === 'pending')
)

const completedTasks = computed(() => 
  allTasks.value.filter(t => t.status === 'completed')
)

const formatDeadline = (deadline: string) => {
  const now = dayjs()
  const target = dayjs(deadline)
  const diffDays = target.diff(now, 'day')
  const diffHours = target.diff(now, 'hour')

  if (diffDays < 0) {
    return `已过期 ${Math.abs(diffDays)}天`
  } else if (diffDays === 0) {
    if (diffHours < 0) {
      return `已过期 ${Math.abs(diffHours)}小时`
    }
    return `今天 ${target.format('HH:mm')} (剩${diffHours}小时)`
  } else if (diffDays === 1) {
    return `明天 ${target.format('HH:mm')}`
  } else if (diffDays <= 3) {
    return `${target.format('MM-DD HH:mm')} (剩${diffDays}天)`
  }
  return target.format('YYYY-MM-DD HH:mm')
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getTaskTypeColor = (type: string) => {
  const map: any = {
    '错题整理': 'danger',
    '习题练习': 'primary',
    '知识点复习': 'success',
    '测验准备': 'warning'
  }
  return map[type] || ''
}

const getPriorityTypeColor = (priority: string) => {
  const map: any = {
    '紧急': 'danger',
    '高': 'warning',
    '普通': '',
    '低': 'info'
  }
  return map[priority] || ''
}

const getProgressPercentage = (task: any) => {
  if (!task.totalCount) return 0
  return Math.round((task.completedCount / task.totalCount) * 100)
}

const getProgressColor = (percentage: number) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 50) return '#e6a23c'
  return '#f56c6c'
}

// 判断是否是新任务（24小时内创建的）
const isNewTask = (task: any) => {
  if (!task.receivedAt) return false
  const receivedTime = dayjs(task.receivedAt)
  const now = dayjs()
  return now.diff(receivedTime, 'hour') < 24
}

const startTask = (task: any) => {
  ElMessage.success('正在跳转到错题本...')
  router.push('/student/errorbook')
}

const viewTaskDetail = (task: any) => {
  currentTask.value = task
  showDetailDialog.value = true
}

const fetchTasks = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      console.warn('⚠️ 用户信息不存在，请重新登录')
      ElMessage.warning('请重新登录')
      return
    }
    
    const user = JSON.parse(userStr)
    console.log('📌 当前登录用户:', user)
    
    if (!user.id) {
      console.warn('⚠️ 用户ID不存在，请重新登录')
      ElMessage.warning('用户信息不完整，请重新登录')
      return
    }
    
    console.log(`🔍 正在获取学生任务列表... 学生ID: ${user.id}`)
    const res = await api.get(`/api/tasks/student/${user.id}`)
    
    console.log('📡 后端返回数据:', res.data)
    
    if (res.data.success && res.data.data && res.data.data.length > 0) {
      console.log(`✅ 获取到 ${res.data.data.length} 个任务`)
      
      allTasks.value = res.data.data.map((task: any) => ({
        id: task.id,
        studentTaskId: task.studentTaskId,
        title: task.title,
        description: task.content,
        taskType: task.taskType || '学习任务',
        deadline: task.deadline,
        quantity: task.quantityRequirement,
        priority: task.priority,
        keywords: task.smartTags || [],
        requirements: task.importantReminders || [],
        teacherName: task.teacherName,
        className: task.className,
        status: task.isCompleted ? 'completed' : 'pending',
        completedAt: task.completedAt,
        completedCount: 0,
        totalCount: 0,
        progress: task.isCompleted ? 100 : 0
      }))
      
      console.log('📋 任务列表已更新:', allTasks.value)
    } else {
      // 如果没有真实数据，使用模拟数据（已在allTasks初始化时设置）
      console.log('ℹ️ 后端返回空数据，显示示例任务')
    }
  } catch (error: any) {
    console.error('❌ 获取任务失败:', error)
    console.error('错误详情:', error.response?.data)
    
    // 保留模拟数据，让用户可以看到界面
    if (error.response?.status === 403 || error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.warning('暂时无法获取任务列表，显示示例数据')
    }
  }
}

onMounted(() => {
  fetchTasks()
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-card.pending {
  border-left: 4px solid #409eff;
}

.stat-card.completed {
  border-left: 4px solid #67c23a;
}

.stat-icon {
  font-size: 48px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

.tasks-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}

.badge {
  margin-left: auto;
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  position: relative;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.task-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
}

.task-card.urgent {
  border-color: rgba(245, 108, 108, 0.3);
  background: linear-gradient(135deg, rgba(255, 240, 240, 0.5), #f8f9fa);
}

/* 新任务样式 */
.task-card.is-new {
  background: linear-gradient(135deg, #fff 0%, #fffbf0 100%);
  border-color: #ffa940;
  border-width: 2px;
  box-shadow: 0 4px 16px rgba(255, 169, 64, 0.2);
  animation: shine 2s ease-in-out infinite;
}

@keyframes shine {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(255, 169, 64, 0.2);
  }
  50% {
    box-shadow: 0 6px 20px rgba(255, 169, 64, 0.3);
  }
}

.new-task-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ffa940 0%, #ff7849 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 169, 64, 0.3);
  z-index: 10;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.priority-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.priority-badge.紧急 {
  background: #f56c6c;
}

.priority-badge.高 {
  background: #e6a23c;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.task-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-content {
  margin: 16px 0;
  color: #666;
  line-height: 1.6;
}

.task-keywords,
.task-requirement {
  margin: 12px 0;
  font-size: 14px;
}

.label {
  font-weight: 600;
  margin-right: 8px;
}

.task-keywords .el-tag {
  margin-right: 8px;
}

.task-progress {
  margin: 16px 0;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.progress-text {
  font-weight: 600;
  color: #409eff;
}

.task-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.task-card.completed {
  opacity: 0.8;
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

.keywords-display {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements-list li {
  padding: 12px;
  background: #f5f7fa;
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
</style>

