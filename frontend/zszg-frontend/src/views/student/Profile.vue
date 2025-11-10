<template>
  <div class="profile-page">
    <!-- 个人信息卡片 -->
    <div class="profile-header glass-card">
      <el-avatar :size="100" class="avatar">
        {{ userInitial }}
      </el-avatar>
      <div class="user-info">
        <h2 class="username">{{ userInfo?.realName || userInfo?.username }}</h2>
        <p class="user-role">{{ getRoleLabel(userInfo?.role) }}</p>
        <p v-if="userInfo?.email" class="user-email">
          <el-icon><Message /></el-icon>
          {{ userInfo.email }}
        </p>
      </div>
      <div class="header-actions">
        <el-button type="success" @click="generateAIReport" :loading="aiLoading">
          <el-icon><MagicStick /></el-icon>
          AI学习报告
        </el-button>
        <el-button type="primary" @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-button>
      </div>
    </div>
    
    <!-- AI学习报告卡片 -->
    <el-card v-if="aiReport" class="ai-report-card glass-card" shadow="hover">
      <template #header>
        <div class="card-header-content">
          <div class="header-left">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <span class="header-title">AI个性化学习报告</span>
            <el-tag type="success" size="small">智能分析</el-tag>
          </div>
          <el-button size="small" text @click="aiReport = null">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>
      <div class="ai-report-content" v-html="formatAIReport(aiReport)"></div>
      <div class="ai-report-footer">
        <span class="report-time">生成时间：{{ new Date().toLocaleString() }}</span>
        <el-button size="small" @click="downloadReport">
          <el-icon><Download /></el-icon>
          下载报告
        </el-button>
      </div>
    </el-card>

    <!-- 统计数据卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon error">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalErrors }}</div>
            <div class="stat-label">总错题数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon corrected">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.correctedErrors }}</div>
            <div class="stat-label">已订正</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon shared">
            <el-icon><Share /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.sharedErrors }}</div>
            <div class="stat-label">已分享</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon rate">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ correctionRate }}%</div>
            <div class="stat-label">订正率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 学习轨迹 -->
    <div class="learning-track glass-card">
      <h3 class="section-title">
        <el-icon><DataLine /></el-icon>
        学习轨迹
      </h3>
      <div ref="chartRef" style="width: 100%; height: 300px;"></div>
    </div>

    <!-- 学科分布 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <div class="subject-distribution glass-card">
          <h3 class="section-title">
            <el-icon><PieChart /></el-icon>
            学科错题分布
          </h3>
          <div ref="subjectChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <div class="difficulty-distribution glass-card">
          <h3 class="section-title">
            <el-icon><Histogram /></el-icon>
            难度分布
          </h3>
          <div ref="difficultyChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <div class="recent-activity glass-card">
      <h3 class="section-title">
        <el-icon><Clock /></el-icon>
        最近活动
      </h3>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="formatDate(activity.createdAt)"
          placement="top"
        >
          <div class="activity-item">
            <strong>{{ activity.type }}</strong>
            <p>{{ activity.description }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" type="email" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="password" placeholder="不修改请留空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Message,
  Edit,
  Document,
  CircleCheck,
  Share,
  TrendCharts,
  DataLine,
  PieChart,
  Histogram,
  Clock,
  MagicStick,
  Close,
  Download
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

// AI功能
const aiLoading = ref(false)
const aiReport = ref('')

const userInfo = ref<any>(null)
const stats = ref({
  totalErrors: 0,
  correctedErrors: 0,
  sharedErrors: 0
})
const recentActivities = ref<any[]>([])
const showEditDialog = ref(false)
const editForm = ref({
  realName: '',
  email: '',
  password: ''
})

const chartRef = ref<HTMLElement>()
const subjectChartRef = ref<HTMLElement>()
const difficultyChartRef = ref<HTMLElement>()

const userInitial = computed(() => {
  if (!userInfo.value) return '?'
  const name = userInfo.value.realName || userInfo.value.username
  return name.charAt(0).toUpperCase()
})

const correctionRate = computed(() => {
  if (stats.value.totalErrors === 0) return 0
  return Math.round((stats.value.correctedErrors / stats.value.totalErrors) * 100)
})

const getRoleLabel = (role?: string) => {
  const labels: any = {
    'ROLE_STUDENT': '学生',
    'ROLE_TEACHER': '教师',
    'ROLE_ADMIN': '管理员'
  }
  return labels[role || ''] || '用户'
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchUserInfo = async () => {
  try {
    const res = await api.get('/api/auth/me')
    if (res.data.success) {
      userInfo.value = res.data.data
      editForm.value = {
        realName: userInfo.value.realName || '',
        email: userInfo.value.email || '',
        password: ''
      }
    }
  } catch (error: any) {
    console.error('获取用户信息失败:', error)
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('获取用户信息失败')
    }
  }
}

const fetchStats = async () => {
  try {
    const res = await api.get('/api/stats/user')
    if (res.data.success) {
      stats.value = res.data.data || stats.value
    }
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    // 保留默认值，不影响页面显示
  }
}

const fetchRecentActivities = async () => {
  try {
    const res = await api.get('/api/error-books')
    if (res.data.success) {
      const errorBooks = res.data.data || []
      recentActivities.value = errorBooks.slice(0, 10).map((eb: any) => ({
        type: '添加错题',
        description: `学科：${eb.question?.subject || '未知'}`,
        createdAt: eb.createdAt
      }))
    }
  } catch (error: any) {
    console.error('获取最近活动失败:', error)
    // 保留空数组，不影响页面显示
  }
}

const initCharts = async () => {
  await nextTick()
  
  // 学习轨迹图
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{
        name: '错题数',
        type: 'line',
        smooth: true,
        data: [5, 8, 6, 10, 7, 9, 4],
        itemStyle: { color: '#667eea' }
      }]
    })
  }
  
  // 学科分布饼图
  if (subjectChartRef.value) {
    const chart = echarts.init(subjectChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        name: '学科分布',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 335, name: '数学' },
          { value: 310, name: '语文' },
          { value: 234, name: '英语' },
          { value: 135, name: '物理' },
          { value: 120, name: '化学' }
        ]
      }]
    })
  }
  
  // 难度分布柱状图
  if (difficultyChartRef.value) {
    const chart = echarts.init(difficultyChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['简单', '中等', '困难'] },
      yAxis: { type: 'value' },
      series: [{
        name: '题目数',
        type: 'bar',
        data: [20, 45, 15],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      }]
    })
  }
}

const saveProfile = async () => {
  try {
    await api.put('/api/auth/update', editForm.value)
    ElMessage.success('保存成功')
    showEditDialog.value = false
    fetchUserInfo()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// AI学习报告功能
const generateAIReport = async () => {
  aiLoading.value = true
  ElMessage.info('AI正在分析你的学习数据，请稍候...')
  
  try {
    const res = await api.get('/api/ai/learning-report')
    aiReport.value = res.data.data
    ElMessage.success('AI报告生成完成！')
    
    // 滚动到AI报告
    await nextTick()
    document.querySelector('.ai-report-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } catch (error) {
    ElMessage.error('生成报告失败')
  } finally {
    aiLoading.value = false
  }
}

// 格式化AI报告
const formatAIReport = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/【(.*?)】/g, '<h3 style="color: #667eea; margin: 20px 0 10px 0;">【$1】</h3>')
    .replace(/(\d+\.)\s/g, '<br><strong style="color: #2c3e50;">$1</strong> ')
    .replace(/•/g, '<br>• ')
    .replace(/一、|二、|三、|四、|五、/g, '<h4 style="color: #409eff; margin: 16px 0 8px 0;">$&</h4>')
}

// 下载报告
const downloadReport = () => {
  const content = aiReport.value.replace(/<[^>]+>/g, '\n')
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `AI学习报告_${userInfo.value?.realName || 'student'}_${new Date().toLocaleDateString()}.txt`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报告已下载')
}

onMounted(async () => {
  await Promise.all([
    fetchUserInfo(),
    fetchStats(),
    fetchRecentActivities()
  ])
  initCharts()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 30px;
  margin-bottom: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.avatar {
  background: rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 36px;
  font-weight: 600;
}

.user-info {
  flex: 1;
}

.username {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.user-role,
.user-email {
  margin: 4px 0;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 6px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  background: white;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.error {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.corrected {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.shared {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.learning-track,
.subject-distribution,
.difficulty-distribution,
.recent-activity {
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  background: white;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #333;
  margin: 0 0 20px 0;
}

.activity-item strong {
  color: #667eea;
}

.activity-item p {
  margin: 4px 0 0 0;
  color: #666;
  font-size: 14px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* AI报告样式 */
.header-actions {
  display: flex;
  gap: 12px;
}

.ai-report-card {
  margin-bottom: 20px;
  border: 2px solid #667eea;
  animation: slideInDown 0.4s ease;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header-content {
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
  font-size: 24px;
  color: #667eea;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.ai-report-content {
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  line-height: 2;
  font-size: 15px;
  color: #2c3e50;
  min-height: 400px;
  max-height: 800px;
  overflow-y: auto;
}

.ai-report-content :deep(h3) {
  color: #667eea;
  margin: 24px 0 12px 0;
  font-size: 18px;
  font-weight: 700;
}

.ai-report-content :deep(h4) {
  color: #409eff;
  margin: 16px 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.ai-report-content :deep(strong) {
  color: #2c3e50;
  font-weight: 600;
}

.ai-report-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
}

.report-time {
  color: #909399;
  font-size: 14px;
}
</style>
