<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><DataAnalysis /></el-icon>
        教学数据分析
      </h1>
    </div>

    <!-- 班级选择 -->
    <el-card shadow="never" class="class-selector">
      <el-space>
        <span>选择班级：</span>
        <el-select v-model="selectedClass" placeholder="请选择班级" @change="fetchClassStats">
          <el-option label="高一1班" value="高一1班" />
          <el-option label="高一2班" value="高一2班" />
          <el-option label="高一3班" value="高一3班" />
          <el-option label="高二1班" value="高二1班" />
          <el-option label="高二2班" value="高二2班" />
        </el-select>
        <el-button type="primary" @click="fetchClassStats">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="success" @click="generateAIReport" :loading="aiLoading">
          <el-icon><MagicStick /></el-icon>
          AI智能分析
        </el-button>
      </el-space>
    </el-card>

    <!-- AI分析报告 -->
    <el-card v-if="aiReport" class="ai-report-card" shadow="hover">
      <template #header>
        <div class="ai-report-header">
          <div class="header-left">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <span class="header-title">AI教学诊断报告</span>
            <el-tag type="success" size="small">智能分析</el-tag>
          </div>
          <el-button size="small" text @click="aiReport = null">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>
      <div class="ai-report-content" v-html="formatAIReport(aiReport)"></div>
      <div class="ai-report-footer">
        <el-button size="small" @click="exportAIReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
        <el-button size="small" type="primary" @click="generatePractice">
          <el-icon><Edit /></el-icon>
          生成练习题
        </el-button>
      </div>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <div class="stat-card gradient-bg-blue">
          <div class="stat-icon"><el-icon><UserFilled /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.studentCount || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-bg-green">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalErrorCount || 0 }}</div>
            <div class="stat-label">错题总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-bg-orange">
          <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.averageErrorPerStudent?.toFixed(1) || 0 }}</div>
            <div class="stat-label">人均错题</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-bg">
          <div class="stat-icon"><el-icon><Promotion /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ getWeakestSubject() }}</div>
            <div class="stat-label">薄弱学科</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 学科分布图 -->
      <el-col :span="12">
        <el-card v-loading="loading" class="chart-card">
          <template #header>
            <div class="card-title">
              <el-icon><PieChart /></el-icon>
              <span>学科错题分布</span>
            </div>
          </template>
          <div ref="subjectChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 难度分布图 -->
      <el-col :span="12">
        <el-card v-loading="loading" class="chart-card">
          <template #header>
            <div class="card-title">
              <el-icon><Histogram /></el-icon>
              <span>难度分布</span>
            </div>
          </template>
          <div ref="difficultyChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高频错题Top10 -->
    <el-card v-loading="loading" class="top-errors-card">
      <template #header>
        <div class="card-title">
          <el-icon><Warning /></el-icon>
          <span>高频错题 TOP 10</span>
        </div>
      </template>

      <el-table :data="topErrorsList" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="question" label="题目（前50字）" min-width="300" />
        <el-table-column prop="count" label="错误次数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="danger" size="large">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="getErrorPercentage(row.count)"
              :color="getProgressColor(row.count)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导出报告 -->
    <div class="export-section">
      <el-button type="success" size="large" @click="exportReport">
        <el-icon><Download /></el-icon>
        导出分析报告
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis,
  Refresh,
  UserFilled,
  Document,
  TrendCharts,
  Promotion,
  PieChart,
  Histogram,
  Warning,
  Download,
  MagicStick,
  Close,
  Edit
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import * as echarts from 'echarts'

const loading = ref(false)
const selectedClass = ref('高一1班')
const stats = ref<any>({})
const subjectChartRef = ref<HTMLElement | null>(null)
const difficultyChartRef = ref<HTMLElement | null>(null)
let subjectChart: echarts.ECharts | null = null
let difficultyChart: echarts.ECharts | null = null

// AI功能
const aiLoading = ref(false)
const aiReport = ref('')

const topErrorsList = computed(() => {
  const topErrors = stats.value.topErrors || {}
  return Object.entries(topErrors).map(([question, count]) => ({
    question,
    count
  }))
})

const getWeakestSubject = () => {
  const subjectStats = stats.value.subjectStats || {}
  if (Object.keys(subjectStats).length === 0) return '-'
  
  const max = Math.max(...Object.values(subjectStats).map(v => Number(v)))
  const subject = Object.entries(subjectStats).find(([_, count]) => count === max)
  return subject ? subject[0] : '-'
}

const getErrorPercentage = (count: number) => {
  const total = stats.value.totalErrorCount || 1
  return Math.round((count / total) * 100)
}

const getProgressColor = (count: number) => {
  const percentage = getErrorPercentage(count)
  if (percentage > 20) return '#f56c6c'
  if (percentage > 10) return '#e6a23c'
  return '#67c23a'
}

const fetchClassStats = async () => {
  loading.value = true
  try {
    const res = await api.get(`/api/stats/class/${selectedClass.value}`)
    stats.value = res.data.data || {}
    
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('加载班级统计失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  renderSubjectChart()
  renderDifficultyChart()
}

const renderSubjectChart = () => {
  if (!subjectChartRef.value) return
  
  if (subjectChart) {
    subjectChart.dispose()
  }
  
  subjectChart = echarts.init(subjectChartRef.value)
  
  const subjectStats = stats.value.subjectStats || {}
  const data = Object.entries(subjectStats).map(([name, value]) => ({
    name,
    value
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 题 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c} 题'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        data
      }
    ],
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
  }
  
  subjectChart.setOption(option)
}

const renderDifficultyChart = () => {
  if (!difficultyChartRef.value) return
  
  if (difficultyChart) {
    difficultyChart.dispose()
  }
  
  difficultyChart = echarts.init(difficultyChartRef.value)
  
  const difficultyStats = stats.value.difficultyStats || {}
  const categories = Object.keys(difficultyStats)
  const values = Object.values(difficultyStats)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '错题数',
        type: 'bar',
        barWidth: '60%',
        data: values,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  difficultyChart.setOption(option)
}

const exportReport = () => {
  ElMessage.success('报告导出功能开发中...')
}

// AI智能分析
const generateAIReport = async () => {
  if (!stats.value.subjectStats) {
    ElMessage.warning('请先加载班级数据')
    return
  }
  
  aiLoading.value = true
  ElMessage.info('AI正在分析班级数据，请稍候...')
  
  try {
    const res = await api.get('/api/ai/class-analysis', {
      params: {
        subject: '数学', // 可以根据选择的学科调整
        classId: selectedClass.value
      }
    })
    
    aiReport.value = res.data.data
    ElMessage.success('AI分析完成！')
    
    // 滚动到AI报告
    await nextTick()
    document.querySelector('.ai-report-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } catch (error) {
    ElMessage.error('AI分析失败')
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

// 导出AI报告
const exportAIReport = () => {
  const content = aiReport.value.replace(/<[^>]+>/g, '\n')
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `AI教学诊断报告_${selectedClass.value}_${new Date().toLocaleDateString()}.txt`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报告已导出')
}

// 生成练习题
const generatePractice = async () => {
  ElMessage.info('AI正在生成针对性练习题...')
  try {
    const weakSubject = getWeakestSubject()
    const res = await api.post('/api/ai/generate-practice', {
      subject: weakSubject,
      knowledgePoint: '综合练习',
      difficulty: '中等',
      count: 5
    })
    
    ElMessageBox.alert(formatAIReport(res.data.data), '生成的练习题', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了'
    })
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

onMounted(() => {
  fetchClassStats()
})
</script>

<style scoped>
.class-selector {
  margin-bottom: 20px;
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 12px;
  color: white;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  animation: scaleIn 0.4s ease;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  font-size: 48px;
  opacity: 0.9;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.chart-card {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.top-errors-card {
  margin-bottom: 20px;
}

.export-section {
  text-align: center;
  padding: 20px;
}

/* AI报告样式 */
.ai-report-card {
  margin-bottom: 20px;
  border: 2px solid #667eea;
  animation: slideDown 0.4s ease;
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

.ai-report-header {
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
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  line-height: 2;
  font-size: 15px;
  color: #2c3e50;
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
}

.ai-report-content :deep(h3) {
  color: #667eea;
  margin: 20px 0 10px 0;
  font-size: 18px;
}

.ai-report-content :deep(h4) {
  color: #409eff;
  margin: 16px 0 8px 0;
  font-size: 16px;
}

.ai-report-content :deep(strong) {
  color: #2c3e50;
}

.ai-report-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
  justify-content: flex-end;
}
</style>
