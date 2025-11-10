<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><ChatDotRound /></el-icon>
        教学反馈
      </h1>
    </div>

    <!-- 待审核的共享错题 -->
    <el-card class="feedback-card">
      <template #header>
        <div class="card-title">
          <el-icon><Document /></el-icon>
          <span>待审核的共享错题</span>
          <el-badge :value="pendingShares.length" class="badge" />
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="pendingShares.length === 0" description="暂无待审核内容" />
        
        <div v-else class="pending-list">
          <el-card
            v-for="share in pendingShares"
            :key="share.id"
            class="pending-item"
            shadow="hover"
          >
            <div class="share-info">
              <div class="share-meta">
                <el-tag type="primary">{{ share.errorBook.question.subject }}</el-tag>
                <el-tag type="warning">{{ share.scope }}</el-tag>
              </div>
              
              <div class="share-content">
                <h4>题目：</h4>
                <p>{{ share.errorBook.question.content }}</p>
                
                <h4>错因分析：</h4>
                <p>{{ share.errorBook.errorReason || '无' }}</p>
                
                <h4>订正内容：</h4>
                <p>{{ share.errorBook.correction || '无' }}</p>
              </div>

              <div class="share-actions">
                <el-button type="success" @click="reviewShare(share.id, true)">
                  <el-icon><Select /></el-icon>
                  通过
                </el-button>
                <el-button type="danger" @click="reviewShare(share.id, false)">
                  <el-icon><CloseBold /></el-icon>
                  拒绝
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 学生学习进度跟踪 -->
    <el-card class="progress-card">
      <template #header>
        <div class="card-title">
          <el-icon><TrendCharts /></el-icon>
          <span>学生学习进度</span>
        </div>
      </template>

      <el-table :data="studentProgress" stripe>
        <el-table-column prop="name" label="学生姓名" width="120" />
        <el-table-column prop="totalErrors" label="错题总数" width="100" align="center" />
        <el-table-column prop="correctedCount" label="已订正" width="100" align="center" />
        <el-table-column label="订正率" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="row.correctionRate"
              :color="getProgressColor(row.correctionRate)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="weakSubject" label="薄弱学科" width="120" />
        <el-table-column label="最近活跃" width="180">
          <template #default="{ row }">
            {{ formatDate(row.lastActive) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewStudentDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 反馈建议 -->
    <el-card class="suggestion-card">
      <template #header>
        <div class="card-title">
          <el-icon><EditPen /></el-icon>
          <span>教学建议</span>
        </div>
      </template>

      <div class="suggestions">
        <el-alert
          v-for="(suggestion, index) in suggestions"
          :key="index"
          :title="suggestion.title"
          :type="suggestion.type"
          :description="suggestion.description"
          show-icon
          :closable="false"
          class="suggestion-item"
        />
      </div>
    </el-card>

    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="showStudentDetail"
      :title="`${currentStudent?.name} 的学习详情`"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentStudent" class="student-detail">
        <!-- 基本信息 -->
        <el-descriptions :column="3" border>
          <el-descriptions-item label="学生姓名">
            {{ currentStudent.name }}
          </el-descriptions-item>
          <el-descriptions-item label="错题总数">
            <el-tag type="danger">{{ currentStudent.totalErrors }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已订正">
            <el-tag type="success">{{ currentStudent.correctedCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订正率" :span="3">
            <el-progress
              :percentage="currentStudent.correctionRate"
              :color="getProgressColor(currentStudent.correctionRate)"
            />
          </el-descriptions-item>
          <el-descriptions-item label="薄弱学科">
            {{ currentStudent.weakSubject }}
          </el-descriptions-item>
          <el-descriptions-item label="最近活跃" :span="2">
            {{ formatDate(currentStudent.lastActive) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 学科分布 -->
        <el-divider content-position="left">
          <el-icon><PieChart /></el-icon>
          学科错题分布
        </el-divider>
        <div class="subject-distribution">
          <el-tag
            v-for="(count, subject) in currentStudent.subjectDistribution"
            :key="subject"
            size="large"
            class="subject-tag"
            type="info"
          >
            {{ subject }}: {{ count }}题
          </el-tag>
        </div>

        <!-- 给学生添加反馈 -->
        <el-divider content-position="left">
          <el-icon><EditPen /></el-icon>
          添加教学反馈
        </el-divider>
        <el-form :model="feedbackForm" label-width="100px">
          <el-form-item label="反馈内容">
            <el-input
              v-model="feedbackForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入对该学生的学习建议和反馈..."
            />
          </el-form-item>
          <el-form-item label="评分">
            <el-rate v-model="feedbackForm.rating" show-text />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback" :loading="submitting">
              <el-icon><Check /></el-icon>
              提交反馈
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 历史反馈记录 -->
        <el-divider content-position="left">
          <el-icon><Clock /></el-icon>
          历史反馈记录
        </el-divider>
        <el-timeline v-if="feedbackHistory.length > 0">
          <el-timeline-item
            v-for="item in feedbackHistory"
            :key="item.id"
            :timestamp="formatDate(item.createdAt)"
            placement="top"
          >
            <el-card>
              <p>{{ item.feedback }}</p>
              <el-rate :model-value="item.rating" disabled show-text />
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无历史反馈" :image-size="100" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Document,
  Select,
  CloseBold,
  TrendCharts,
  EditPen,
  Check,
  Clock,
  PieChart
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const pendingShares = ref<any[]>([])
const studentProgress = ref<any[]>([])
const showStudentDetail = ref(false)
const currentStudent = ref<any>(null)
const feedbackHistory = ref<any[]>([])

const feedbackForm = ref({
  content: '',
  rating: 5
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const suggestions = computed(() => {
  const result = []
  
  // 基于数据生成建议
  if (pendingShares.value.length > 10) {
    result.push({
      title: '共享审核提醒',
      type: 'warning',
      description: `当前有 ${pendingShares.value.length} 条待审核的共享错题，建议及时处理。`
    })
  }
  
  const lowProgressStudents = studentProgress.value.filter(s => s.correctionRate < 50)
  if (lowProgressStudents.length > 0) {
    result.push({
      title: '学习进度提醒',
      type: 'error',
      description: `有 ${lowProgressStudents.length} 名学生订正率低于50%，建议重点关注：${lowProgressStudents.map(s => s.name).join('、')}`
    })
  }
  
  // 找出薄弱学科
  const subjectMap = new Map()
  studentProgress.value.forEach(student => {
    const subject = student.weakSubject
    subjectMap.set(subject, (subjectMap.get(subject) || 0) + 1)
  })
  const weakestSubject = Array.from(subjectMap.entries()).sort((a, b) => b[1] - a[1])[0]
  if (weakestSubject) {
    result.push({
      title: '学科分析',
      type: 'info',
      description: `${weakestSubject[0]}是${weakestSubject[1]}名学生的薄弱学科，建议进行专题讲解。`
    })
  }
  
  result.push({
    title: '教学建议',
    type: 'success',
    description: '建议针对高频错题进行专题讲解，加强薄弱知识点的训练，同时关注订正率较低的学生。'
  })
  
  return result
})

const fetchPendingShares = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/share-pool/pending')
    if (res.data.success && res.data.data && res.data.data.length > 0) {
      pendingShares.value = res.data.data
    } else {
      // 如果后端返回空数据，使用模拟数据用于演示
      console.log('后端无数据，使用模拟数据')
      pendingShares.value = generateMockPendingShares()
    }
  } catch (error: any) {
    console.error('加载待审核的共享错题失败:', error)
    // 使用模拟数据用于演示
    console.log('API调用失败，使用模拟数据')
    pendingShares.value = generateMockPendingShares()
  } finally {
    loading.value = false
  }
}

// 生成模拟的待审核数据
const generateMockPendingShares = () => {
  return [
    {
      id: 1,
      scope: '班级',
      errorBook: {
        question: {
          subject: '数学',
          content: '求函数 f(x) = x³ - 3x² + 2 在区间 [-1, 3] 上的最大值和最小值。',
          difficulty: '中等'
        },
        errorReason: '忘记检查端点值，只计算了驻点的函数值',
        correction: '需要计算所有驻点和端点的函数值，然后比较得出最大值和最小值。正确答案：最大值为2（x=-1时），最小值为-2（x=2时）'
      },
      user: {
        realName: '张三',
        username: 'student_zhang'
      },
      createdAt: new Date(Date.now() - 2 * 3600000).toISOString()
    },
    {
      id: 2,
      scope: '年级',
      errorBook: {
        question: {
          subject: '物理',
          content: '一个质量为2kg的物体从10m高处自由落下，不计空气阻力，求物体落地时的速度。（g=10m/s²）',
          difficulty: '简单'
        },
        errorReason: '公式记错了，使用了v = gt而不是v² = 2gh',
        correction: '应该使用能量守恒或运动学公式 v² = 2gh，计算得 v = √(2×10×10) = √200 ≈ 14.14 m/s'
      },
      user: {
        realName: '李四',
        username: 'student_li'
      },
      createdAt: new Date(Date.now() - 5 * 3600000).toISOString()
    },
    {
      id: 3,
      scope: '班级',
      errorBook: {
        question: {
          subject: '化学',
          content: '计算0.1mol/L的醋酸溶液的pH值（醋酸的电离常数Ka=1.8×10⁻⁵）',
          difficulty: '困难'
        },
        errorReason: '忘记使用弱电解质的电离平衡计算，直接按强酸计算了pH',
        correction: '醋酸是弱酸，需要用电离平衡常数计算。设电离度为α，Ka = Cα²/(1-α)，由于α很小，近似为Ka ≈ Cα²，解得α≈0.0134，[H⁺]=Cα≈1.34×10⁻³，pH≈2.87'
      },
      user: {
        realName: '王五',
        username: 'student_wang'
      },
      createdAt: new Date(Date.now() - 1 * 3600000).toISOString()
    },
    {
      id: 4,
      scope: '全校',
      errorBook: {
        question: {
          subject: '英语',
          content: 'Translate: "这本书值得一读。" (使用worth)',
          difficulty: '简单'
        },
        errorReason: '句型用错了，写成了 "This book is worth to read"',
        correction: 'worth后面要接动名词，正确答案是 "This book is worth reading." 或 "This book is worthy of being read."'
      },
      user: {
        realName: '赵六',
        username: 'student_zhao'
      },
      createdAt: new Date(Date.now() - 30 * 60000).toISOString()
    },
    {
      id: 5,
      scope: '班级',
      errorBook: {
        question: {
          subject: '数学',
          content: '已知等差数列{aₙ}的前n项和为Sₙ，若a₃=7，S₅=35，求该数列的通项公式。',
          difficulty: '中等'
        },
        errorReason: '没有正确建立方程组，只用了一个条件',
        correction: '利用通项公式aₙ = a₁ + (n-1)d 和前n项和公式Sₙ = na₁ + n(n-1)d/2，可得：a₁+2d=7，5a₁+10d=35。解得a₁=3，d=2，所以aₙ=2n+1'
      },
      user: {
        realName: '孙七',
        username: 'student_sun'
      },
      createdAt: new Date(Date.now() - 6 * 3600000).toISOString()
    }
  ]
}

const reviewShare = async (id: number, approve: boolean) => {
  try {
    await api.put(`/api/share-pool/${id}/review`, {
      approve: approve
    })
    ElMessage.success(approve ? '已通过审核' : '已拒绝')
    fetchPendingShares()
  } catch (error: any) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败，请重试')
  }
}

const viewStudentDetail = async (student: any) => {
  currentStudent.value = student
  showStudentDetail.value = true
  
  // 加载该学生的反馈历史
  try {
    // 这里应该调用真实的API，暂时使用模拟数据
    feedbackHistory.value = [
      {
        id: 1,
        feedback: '学习态度认真，但需要加强基础知识的理解。建议多做练习题。',
        rating: 4,
        createdAt: new Date(Date.now() - 7 * 86400000).toISOString()
      },
      {
        id: 2,
        feedback: '近期进步明显，订正及时。继续保持！',
        rating: 5,
        createdAt: new Date(Date.now() - 3 * 86400000).toISOString()
      }
    ]
  } catch (error) {
    console.error('加载反馈历史失败:', error)
    feedbackHistory.value = []
  }
}

const submitFeedback = async () => {
  if (!feedbackForm.value.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  submitting.value = true
  try {
    // 这里应该调用真实的API提交反馈
    // await api.post(`/api/classroom/feedback`, {
    //   studentId: currentStudent.value.id,
    //   feedback: feedbackForm.value.content,
    //   rating: feedbackForm.value.rating
    // })
    
    // 模拟提交成功
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('反馈提交成功')
    
    // 添加到历史记录
    feedbackHistory.value.unshift({
      id: Date.now(),
      feedback: feedbackForm.value.content,
      rating: feedbackForm.value.rating,
      createdAt: new Date().toISOString()
    })
    
    // 重置表单
    feedbackForm.value = {
      content: '',
      rating: 5
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPendingShares()
  
  // 模拟学生进度数据
  studentProgress.value = [
    {
      id: 1,
      name: '张三',
      totalErrors: 45,
      correctedCount: 38,
      correctionRate: 84,
      weakSubject: '数学',
      lastActive: new Date().toISOString(),
      subjectDistribution: {
        '数学': 15,
        '物理': 12,
        '化学': 10,
        '英语': 8
      }
    },
    {
      id: 2,
      name: '李四',
      totalErrors: 52,
      correctedCount: 30,
      correctionRate: 58,
      weakSubject: '物理',
      lastActive: new Date(Date.now() - 86400000).toISOString(),
      subjectDistribution: {
        '物理': 18,
        '数学': 14,
        '化学': 12,
        '生物': 8
      }
    },
    {
      id: 3,
      name: '王五',
      totalErrors: 38,
      correctedCount: 35,
      correctionRate: 92,
      weakSubject: '化学',
      lastActive: new Date().toISOString(),
      subjectDistribution: {
        '化学': 10,
        '数学': 12,
        '物理': 8,
        '英语': 8
      }
    }
  ]
})
</script>

<style scoped>
.feedback-card,
.progress-card,
.suggestion-card {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  position: relative;
}

.badge {
  margin-left: auto;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pending-item {
  animation: fadeIn 0.3s ease;
}

.share-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.share-meta {
  display: flex;
  gap: 8px;
}

.share-content h4 {
  color: var(--el-color-primary);
  margin: 12px 0 8px 0;
  font-size: 14px;
}

.share-content p {
  line-height: 1.6;
  color: var(--el-text-color-regular);
  padding: 12px;
  background: var(--el-bg-color);
  border-radius: 6px;
  margin: 0;
}

.share-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--el-border-color);
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  animation: slideInRight 0.4s ease;
}

.student-detail {
  padding: 20px 0;
}

.subject-distribution {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px 0;
}

.subject-tag {
  padding: 12px 20px;
  font-size: 14px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
