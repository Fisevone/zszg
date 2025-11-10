<template>
  <div class="home-container">
    <!-- 柔和渐变背景 -->
    <div class="background-overlay"></div>

    <!-- 主内容区 -->
    <div class="hero-section">
      <div class="hero-content">
        <!-- 标题区域 -->
        <div class="title-section">
          <h1 class="main-title">知错就改</h1>
          <p class="subtitle">
            <span class="tag">AI 智能学习</span>
            <span class="divider">·</span>
            <span class="tag">错题共享</span>
            <span class="divider">·</span>
            <span class="tag">知识溯源</span>
          </p>
        </div>

        <!-- 新任务提醒卡片（学生端） -->
        <div v-if="role === 'ROLE_STUDENT' && pendingTasks.length > 0" class="task-alert-section">
          <div class="task-alert-card">
            <div class="alert-icon">
              <el-icon :size="32"><BellFilled /></el-icon>
            </div>
            <div class="alert-content">
              <div class="alert-title">📢 您有 {{ pendingTasks.length }} 个待完成任务</div>
              <div class="alert-subtitle">{{ latestTask?.teacherName }}老师发布了新任务</div>
            </div>
            <button class="alert-btn" @click="goToTasks">立即查看</button>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-section">
          <div 
            class="stat-card" 
            v-for="(stat, index) in statsData" 
            :key="index"
          >
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="28">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <button class="btn btn-primary" @click="goToErrorBook">
            <el-icon><Edit /></el-icon>
            <span>开始学习</span>
          </button>
          <button class="btn btn-secondary" @click="goToShare">
            <el-icon><Share /></el-icon>
            <span>浏览共享池</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 特色功能区 -->
    <div class="features-section">
      <h2 class="section-title">平台特色</h2>
      
      <div class="features-grid">
        <div 
          class="feature-card" 
          v-for="(feature, index) in features" 
          :key="index"
        >
          <div class="feature-icon" :style="{ background: feature.color }">
            <el-icon :size="36">
              <component :is="feature.icon" />
            </el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  Check,
  Share,
  Edit,
  Collection,
  MagicStick,
  Camera,
  TrendCharts,
  BellFilled
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const router = useRouter()
const stats = ref<any>({})
const role = computed(() => localStorage.getItem('role'))

// 待完成任务列表
const pendingTasks = ref<any[]>([])
const latestTask = computed(() => pendingTasks.value[0] || null)

// 获取待完成任务
async function fetchPendingTasks() {
  if (role.value !== 'ROLE_STUDENT') return
  
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) return
    
    const user = JSON.parse(userStr)
    const res = await api.get(`/api/tasks/student/${user.id}`)
    
    if (res.data.success && res.data.data) {
      // 只保留未完成的任务
      pendingTasks.value = res.data.data
        .filter((task: any) => !task.isCompleted)
        .slice(0, 3) // 只显示最新的3个
    }
  } catch (error) {
    console.error('获取任务失败:', error)
  }
}

// 跳转到任务页面
function goToTasks() {
  router.push('/student/tasks')
}

// 统计数据 - 使用更柔和的配色
const statsData = ref([
  {
    icon: Document,
    value: 0,
    label: '我的错题',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    icon: Check,
    value: 0,
    label: '已订正',
    color: 'linear-gradient(135deg, #56ab2f 0%, #a8e063 100%)'
  },
  {
    icon: Share,
    value: 0,
    label: '已共享',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
])

// 特色功能 - 优化配色
const features = ref([
  {
    icon: Camera,
    title: 'AI拍照识题',
    desc: '拍照自动识别题目内容，快速录入错题',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    icon: MagicStick,
    title: 'AI智能解析',
    desc: '自动生成题目解析和学习建议',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    icon: Share,
    title: '错题共享',
    desc: '班级、年级共享优质错题',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    icon: TrendCharts,
    title: '学习统计',
    desc: '可视化学习进度，追踪提升效果',
    color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  }
])

const fetchStats = async () => {
  try {
    const role = localStorage.getItem('role')
    if (role === 'ROLE_STUDENT') {
      const res = await api.get('/api/stats/user')
      if (res.data.success) {
        stats.value = res.data.data
        statsData.value[0].value = stats.value.totalErrors || 0
        statsData.value[1].value = stats.value.correctedErrors || 0
        statsData.value[2].value = stats.value.sharedErrors || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const goToErrorBook = () => {
  const role = localStorage.getItem('role')
  if (role === 'ROLE_STUDENT') {
    router.push('/student/errorbook')
  } else if (role === 'ROLE_TEACHER') {
    router.push('/teacher/dashboard')
  } else {
    router.push('/admin/users')
  }
}

const goToShare = () => {
  router.push('/student/share')
}

onMounted(() => {
  fetchStats()
  fetchPendingTasks()
})
</script>

<style scoped>
/* ========== 全局容器 ========== */
.home-container {
  min-height: calc(100vh - 60px);
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.background-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
  z-index: 0;
  pointer-events: none;
}

/* ========== 主内容区 ========== */
.hero-section {
  position: relative;
  z-index: 1;
  padding: 80px 20px 60px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 48px;
  animation: fadeInUp 0.8s ease;
}

/* ========== 标题区域 ========== */
.title-section {
  text-align: center;
  animation: fadeInUp 0.6s ease;
}

.main-title {
  font-size: 64px;
  font-weight: 800;
  margin: 0 0 20px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 20px;
  color: #666;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tag {
  background: rgba(102, 126, 234, 0.1);
  padding: 6px 18px;
  border-radius: 20px;
  color: #667eea;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tag:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.divider {
  color: #ccc;
  font-weight: 300;
}

/* ========== 统计卡片区域 ========== */
/* ========== 任务提醒卡片 ========== */
.task-alert-section {
  margin: 30px 0;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.task-alert-card {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  padding: 24px 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.3);
  position: relative;
  overflow: hidden;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.3);
  }
  50% {
    box-shadow: 0 8px 32px rgba(255, 107, 107, 0.5);
  }
}

.alert-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 6px;
}

.alert-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.alert-btn {
  padding: 12px 28px;
  background: white;
  color: #ff6b6b;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.alert-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  animation: fadeInUp 0.8s ease 0.2s both;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

/* ========== 操作按钮 ========== */
.action-section {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  animation: fadeInUp 1s ease 0.4s both;
}

.btn {
  padding: 16px 40px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.btn-secondary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
}

.btn:active {
  transform: translateY(-1px);
}

/* ========== 特色功能区 ========== */
.features-section {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px 80px;
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin-bottom: 48px;
  position: relative;
  animation: fadeInUp 0.8s ease;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 28px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 36px 28px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease both;
}

.feature-card:nth-child(1) { animation-delay: 0.1s; }
.feature-card:nth-child(2) { animation-delay: 0.2s; }
.feature-card:nth-child(3) { animation-delay: 0.3s; }
.feature-card:nth-child(4) { animation-delay: 0.4s; }

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.feature-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* ========== 动画 ========== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .main-title {
    font-size: 42px;
  }

  .subtitle {
    font-size: 16px;
    flex-direction: column;
    gap: 8px;
  }

  .divider {
    display: none;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .action-section {
    flex-direction: column;
    max-width: 300px;
    margin: 0 auto;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }

  .section-title {
    font-size: 28px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 36px;
  }

  .hero-section {
    padding: 60px 16px 40px;
  }

  .features-section {
    padding: 40px 16px 60px;
  }
}
</style>
