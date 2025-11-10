<template>
  <div class="class-manage-container">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><School /></el-icon>
        班级管理
      </h2>
      <el-button type="primary" @click="createClassDialog = true">
        <el-icon><Plus /></el-icon>
        创建班级
      </el-button>
    </div>

    <!-- 班级列表 -->
    <div class="classes-grid">
      <el-card 
        v-for="cls in classes" 
        :key="cls.id" 
        class="class-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <div class="class-info">
              <h3 class="class-name">{{ cls.name }}</h3>
              <el-tag type="info" size="small">{{ cls.gradeLevel }}</el-tag>
            </div>
            <el-dropdown @command="(cmd: string) => handleCommand(cmd, cls)">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="students">
                    <el-icon><User /></el-icon>学生管理
                  </el-dropdown-item>
                  <el-dropdown-item command="push">
                    <el-icon><Promotion /></el-icon>推送资源
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    <span style="color: #f56c6c">删除班级</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>

        <div class="class-content">
          <div class="class-desc">
            <el-icon><Document /></el-icon>
            <span>{{ cls.description || '暂无描述' }}</span>
          </div>

          <div class="class-stats">
            <div class="stat-item">
              <el-icon class="stat-icon"><User /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ cls.studentCount }}</div>
                <div class="stat-label">学生人数</div>
              </div>
            </div>
          </div>

          <div class="invite-code-section">
            <div class="invite-label">
              <el-icon><Key /></el-icon>
              <span>邀请码</span>
            </div>
            <div class="invite-code">
              <span class="code-text">{{ cls.inviteCode }}</span>
              <el-button 
                size="small" 
                @click="copyInviteCode(cls.inviteCode)"
                :icon="DocumentCopy"
              >
                复制
              </el-button>
            </div>
          </div>

          <div class="class-time">
            <el-icon><Clock /></el-icon>
            创建于 {{ formatTime(cls.createdAt) }}
          </div>

          <!-- AI功能按钮 -->
          <el-button 
            type="success" 
            class="ai-analysis-btn"
            @click="openAIAnalysis(cls)"
          >
            🤖 AI智能分析
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty 
        v-if="classes.length === 0" 
        description="还没有创建班级，点击右上角创建第一个班级吧！"
        :image-size="200"
      />
    </div>

    <!-- 创建班级对话框 -->
    <el-dialog
      v-model="createClassDialog"
      title="创建班级"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="classForm" :rules="classRules" ref="classFormRef" label-width="80px">
        <el-form-item label="班级名称" prop="name">
          <el-input 
            v-model="classForm.name" 
            placeholder="如：高一1班"
            clearable
          />
        </el-form-item>

        <el-form-item label="年级" prop="gradeLevel">
          <el-select v-model="classForm.gradeLevel" placeholder="请选择年级" style="width: 100%">
            <el-option label="小学一年级" value="小学一年级" />
            <el-option label="小学二年级" value="小学二年级" />
            <el-option label="小学三年级" value="小学三年级" />
            <el-option label="小学四年级" value="小学四年级" />
            <el-option label="小学五年级" value="小学五年级" />
            <el-option label="小学六年级" value="小学六年级" />
            <el-option label="初一" value="初一" />
            <el-option label="初二" value="初二" />
            <el-option label="初三" value="初三" />
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </el-form-item>

        <el-form-item label="班级描述">
          <el-input 
            v-model="classForm.description" 
            type="textarea"
            :rows="3"
            placeholder="介绍一下这个班级..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createClassDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateClass" :loading="creating">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentsDialog"
      :title="`${selectedClass?.name} - 学生列表`"
      width="800px"
    >
      <el-table :data="students" v-loading="loadingStudents">
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="errorCount" label="错题数" width="100">
          <template #default="{ row }">
            <el-tag :type="row.errorCount > 20 ? 'danger' : row.errorCount > 10 ? 'warning' : 'success'">
              {{ row.errorCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinedAt" label="加入时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.joinedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewStudentErrors(row)">
              查看错题
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 推送资源对话框 -->
    <el-dialog
      v-model="pushDialog"
      :title="`向 ${selectedClass?.name} 推送资源`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="pushForm" :rules="pushRules" ref="pushFormRef" label-width="100px">
        <el-form-item label="推送类型" prop="resourceType">
          <el-radio-group v-model="pushForm.resourceType">
            <el-radio value="NOTICE">
              <el-icon><Bell /></el-icon>
              通知
            </el-radio>
            <el-radio value="QUESTION">
              <el-icon><Document /></el-icon>
              题目
            </el-radio>
            <el-radio value="KNOWLEDGE">
              <el-icon><Reading /></el-icon>
              知识点
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="推送标题" prop="title">
          <el-input 
            v-model="pushForm.title" 
            placeholder="如：本周数学作业"
            clearable
          />
        </el-form-item>

        <el-form-item label="推送内容" prop="content">
          <el-input 
            v-model="pushForm.content" 
            type="textarea"
            :rows="5"
            placeholder="输入详细内容..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="pushDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePush" :loading="pushing">
          发送推送
        </el-button>
      </template>
    </el-dialog>

    <!-- AI智能分析对话框 -->
    <el-dialog
      v-model="aiAnalysisDialog"
      :title="`🤖 ${selectedClass?.name} - AI智能分析`"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeAITab" type="border-card">
        <!-- 班级学情分析 -->
        <el-tab-pane label="📊 班级学情分析" name="analysis">
          <div class="ai-panel">
            <el-button 
              type="primary" 
              @click="generateClassAnalysis"
              :loading="aiLoading.analysis"
            >
              🔍 生成学情分析报告
            </el-button>
            
            <div v-if="aiResults.analysis" class="ai-result">
              <div class="result-header">
                <el-icon><Document /></el-icon>
                <span>AI分析报告</span>
              </div>
              <div class="result-content" v-html="aiResults.analysis"></div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 教学重点推荐 -->
        <el-tab-pane label="🎯 教学重点推荐" name="recommend">
          <div class="ai-panel">
            <el-button 
              type="primary" 
              @click="generateTeachingRecommendation"
              :loading="aiLoading.recommend"
            >
              💡 AI推荐教学重点
            </el-button>
            
            <div v-if="aiResults.recommend" class="ai-result">
              <div class="result-header">
                <el-icon><Star /></el-icon>
                <span>教学重点推荐</span>
              </div>
              <div class="result-content" v-html="aiResults.recommend"></div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 个性化作业生成 -->
        <el-tab-pane label="📝 个性化作业" name="homework">
          <div class="ai-panel">
            <el-form :inline="true">
              <el-form-item label="选择学生">
                <el-select 
                  v-model="selectedStudentForHomework" 
                  placeholder="请选择学生"
                  style="width: 200px"
                >
                  <el-option 
                    v-for="student in students" 
                    :key="student.id"
                    :label="student.realName"
                    :value="student.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="generatePersonalizedHomework"
                  :loading="aiLoading.homework"
                  :disabled="!selectedStudentForHomework"
                >
                  🎯 生成个性化作业
                </el-button>
              </el-form-item>
            </el-form>
            
            <div v-if="aiResults.homework" class="ai-result">
              <div class="result-header">
                <el-icon><Edit /></el-icon>
                <span>个性化作业方案</span>
              </div>
              <div class="result-content" v-html="aiResults.homework"></div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 智能分组建议 -->
        <el-tab-pane label="👥 智能分组" name="grouping">
          <div class="ai-panel">
            <el-form :inline="true">
              <el-form-item label="分组数量">
                <el-input-number 
                  v-model="groupCount" 
                  :min="2" 
                  :max="10"
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="generateSmartGrouping"
                  :loading="aiLoading.grouping"
                >
                  🎲 AI智能分组
                </el-button>
              </el-form-item>
            </el-form>
            
            <div v-if="aiResults.grouping" class="ai-result">
              <div class="result-header">
                <el-icon><Grid /></el-icon>
                <span>智能分组方案</span>
              </div>
              <div class="result-content" v-html="aiResults.grouping"></div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 学情报告生成 -->
        <el-tab-pane label="📈 学情报告" name="report">
          <div class="ai-panel">
            <el-button 
              type="primary" 
              @click="generateDetailedReport"
              :loading="aiLoading.report"
            >
              📄 生成详细学情报告
            </el-button>
            
            <div v-if="aiResults.report" class="ai-result">
              <div class="result-header">
                <el-icon><Tickets /></el-icon>
                <span>详细学情报告</span>
              </div>
              <div class="result-content" v-html="aiResults.report"></div>
              <el-button type="success" @click="downloadReport" class="download-btn">
                📥 下载报告
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  School, Plus, User, Document, Clock, Key, MoreFilled,
  Promotion, DocumentCopy, Bell, Reading, Star, Edit, Grid, Tickets, Delete
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

const classes = ref<any[]>([])
const students = ref<any[]>([])
const selectedClass = ref<any>(null)

const createClassDialog = ref(false)
const studentsDialog = ref(false)
const pushDialog = ref(false)
const aiAnalysisDialog = ref(false)

const creating = ref(false)
const loadingStudents = ref(false)
const pushing = ref(false)

const classFormRef = ref()
const pushFormRef = ref()

// AI功能相关
const activeAITab = ref('analysis')
const selectedStudentForHomework = ref<number | null>(null)
const groupCount = ref(3)

const aiLoading = ref({
  analysis: false,
  recommend: false,
  homework: false,
  grouping: false,
  report: false
})

const aiResults = ref({
  analysis: '',
  recommend: '',
  homework: '',
  grouping: '',
  report: ''
})

const classForm = ref({
  name: '',
  gradeLevel: '',
  description: ''
})

const pushForm = ref({
  resourceType: 'NOTICE',
  title: '',
  content: ''
})

const classRules = {
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const pushRules = {
  resourceType: [{ required: true, message: '请选择推送类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入推送标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入推送内容', trigger: 'blur' }]
}

// 加载班级列表
const loadClasses = async () => {
  try {
    const res = await api.get('/api/classroom/teacher/classes')
    if (res.data.success) {
      classes.value = res.data.data
    }
  } catch (error) {
    console.error('加载班级列表失败', error)
    ElMessage.error('加载班级列表失败')
  }
}

// 创建班级
const handleCreateClass = async () => {
  if (!classFormRef.value) return
  
  try {
    await classFormRef.value.validate()
    creating.value = true
    
    const res = await api.post('/api/classroom/create', classForm.value)
    
    if (res.data.success) {
      ElMessage.success('班级创建成功！')
      createClassDialog.value = false
      classForm.value = { name: '', gradeLevel: '', description: '' }
      
      // 显示邀请码
      const inviteCode = res.data.data.inviteCode
      ElMessageBox.alert(
        `<div style="text-align: center; padding: 20px;">
          <h3 style="margin-bottom: 20px;">班级创建成功！</h3>
          <p style="margin-bottom: 10px; color: #606266;">请将以下邀请码分享给学生：</p>
          <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 4px; margin: 20px 0;">
            ${inviteCode}
          </div>
          <p style="color: #909399; font-size: 14px;">学生可通过此邀请码加入班级</p>
        </div>`,
        '邀请码',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '知道了'
        }
      )
      
      await loadClasses()
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  } finally {
    creating.value = false
  }
}

// 复制邀请码
const copyInviteCode = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('邀请码已复制到剪贴板')
  })
}

// 处理菜单命令
const handleCommand = (command: string, cls: any) => {
  selectedClass.value = cls
  if (command === 'students') {
    loadStudents(cls.id)
  } else if (command === 'push') {
    pushDialog.value = true
  } else if (command === 'delete') {
    handleDeleteClass(cls)
  }
}

// 删除班级
const handleDeleteClass = async (cls: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除班级「${cls.name}」吗？\n\n` +
      `此操作将同时删除：\n` +
      `• 班级的所有成员 (${cls.studentCount}人)\n` +
      `• 该班级的所有推送记录\n` +
      `• 该班级的所有反馈记录\n\n` +
      `此操作不可恢复！`,
      '删除班级',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 执行删除
    const res = await api.delete(`/api/classroom/class/${cls.id}`)
    
    if (res.data.success) {
      ElMessage.success('班级已删除')
      await loadClasses()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 加载学生列表
const loadStudents = async (classId: number) => {
  studentsDialog.value = true
  loadingStudents.value = true
  
  try {
    const res = await api.get(`/api/classroom/class/${classId}/students`)
    if (res.data.success) {
      students.value = res.data.data
    }
  } catch (error) {
    console.error('加载学生列表失败', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

// 推送资源
const handlePush = async () => {
  if (!pushFormRef.value) return
  
  try {
    await pushFormRef.value.validate()
    pushing.value = true
    
    const res = await api.post('/api/classroom/push', {
      ...pushForm.value,
      classId: selectedClass.value.id
    })
    
    if (res.data.success) {
      ElMessage.success('推送成功！')
      pushDialog.value = false
      pushForm.value = { resourceType: 'NOTICE', title: '', content: '' }
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  } finally {
    pushing.value = false
  }
}

// 查看学生错题
const viewStudentErrors = (student: any) => {
  ElMessage.info(`查看 ${student.realName} 的错题（功能开发中）`)
  // TODO: 跳转到学生错题详情页
}

// 打开AI分析对话框
const openAIAnalysis = async (cls: any) => {
  selectedClass.value = cls
  aiAnalysisDialog.value = true
  // 先加载学生列表，以便AI分析使用
  await loadStudents(cls.id)
}

// 生成班级学情分析
const generateClassAnalysis = async () => {
  if (!selectedClass.value) return
  
  aiLoading.value.analysis = true
  try {
    // 获取班级所有学生的错题数据
    const prompt = `作为一名资深教师，请分析以下班级的学习情况：

班级：${selectedClass.value.name}
年级：${selectedClass.value.gradeLevel}
学生人数：${students.value.length}人

学生错题情况：
${students.value.map((s, i) => `${i + 1}. ${s.realName}: ${s.errorCount}道错题`).join('\n')}

请生成一份详细的班级学情分析报告，包括：
1. 整体学习状况评估
2. 共性问题分析
3. 优秀与待提高学生分布
4. 教学建议

请用HTML格式输出，包含标题、段落、列表等元素。`

    const res = await api.post('/api/ai/ask', { 
      question: prompt,
      subject: '教学分析',
      context: ''
    })
    if (res.data.success) {
      aiResults.value.analysis = res.data.data
      ElMessage.success('学情分析报告生成成功！')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成失败')
  } finally {
    aiLoading.value.analysis = false
  }
}

// 生成教学重点推荐
const generateTeachingRecommendation = async () => {
  if (!selectedClass.value) return
  
  aiLoading.value.recommend = true
  try {
    const prompt = `作为一名教学专家，基于以下班级数据，推荐下次课应该重点讲解的内容：

班级：${selectedClass.value.name}
年级：${selectedClass.value.gradeLevel}
学生数：${students.value.length}人
平均错题数：${(students.value.reduce((sum, s) => sum + s.errorCount, 0) / students.value.length).toFixed(1)}道

请推荐：
1. 下次课重点讲解的知识点（3-5个）
2. 每个知识点的重要性说明
3. 建议的教学顺序
4. 预计课时安排

请用HTML格式输出，使用标题、段落、编号列表等。`

    const res = await api.post('/api/ai/ask', { 
      question: prompt,
      subject: '教学分析',
      context: ''
    })
    if (res.data.success) {
      aiResults.value.recommend = res.data.data
      ElMessage.success('教学重点推荐生成成功！')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成失败')
  } finally {
    aiLoading.value.recommend = false
  }
}

// 生成个性化作业
const generatePersonalizedHomework = async () => {
  if (!selectedStudentForHomework.value) return
  
  const student = students.value.find(s => s.id === selectedStudentForHomework.value)
  if (!student) return
  
  aiLoading.value.homework = true
  try {
    const prompt = `作为一名教师，请为以下学生设计个性化作业：

学生姓名：${student.realName}
班级：${selectedClass.value.name}
年级：${selectedClass.value.gradeLevel}
错题数量：${student.errorCount}道

请生成：
1. 针对性练习题（5-8道）
2. 每道题的知识点说明
3. 难度分级（基础/提高/拓展）
4. 预计完成时间
5. 学习建议

请用HTML格式输出，包含清晰的分类和格式。`

    const res = await api.post('/api/ai/ask', { 
      question: prompt,
      subject: '教学分析',
      context: ''
    })
    if (res.data.success) {
      aiResults.value.homework = res.data.data
      ElMessage.success('个性化作业生成成功！')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成失败')
  } finally {
    aiLoading.value.homework = false
  }
}

// 生成智能分组
const generateSmartGrouping = async () => {
  if (!selectedClass.value || students.value.length === 0) return
  
  aiLoading.value.grouping = true
  try {
    const prompt = `作为教学助手，请根据以下学生的学习情况，进行智能分组（分成${groupCount.value}组）：

班级：${selectedClass.value.name}
学生列表：
${students.value.map((s, i) => `${i + 1}. ${s.realName} - 错题数：${s.errorCount}`).join('\n')}

分组要求：
1. 每组人数尽量均衡
2. 每组学生能力水平搭配合理（优秀+中等+待提高）
3. 有利于互帮互助

请输出：
1. 每组的成员名单
2. 每组的能力水平分析
3. 分组的教学建议
4. 注意事项

请用HTML格式输出，使用表格或列表清晰展示分组结果。`

    const res = await api.post('/api/ai/ask', { 
      question: prompt,
      subject: '教学分析',
      context: ''
    })
    if (res.data.success) {
      aiResults.value.grouping = res.data.data
      ElMessage.success('智能分组方案生成成功！')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成失败')
  } finally {
    aiLoading.value.grouping = false
  }
}

// 生成详细学情报告
const generateDetailedReport = async () => {
  if (!selectedClass.value) return
  
  aiLoading.value.report = true
  try {
    const prompt = `作为教育专家，请生成一份详细的班级学情报告：

【基本信息】
班级：${selectedClass.value.name}
年级：${selectedClass.value.gradeLevel}
学生人数：${students.value.length}人
报告日期：${dayjs().format('YYYY年MM月DD日')}

【学生情况】
${students.value.map((s, i) => `${i + 1}. ${s.realName} - 错题数：${s.errorCount}，加入时间：${dayjs(s.joinedAt).format('YYYY-MM-DD')}`).join('\n')}

请生成包含以下内容的详细报告：
1. 班级整体情况摘要
2. 学习数据统计分析（最高/最低/平均错题数等）
3. 学生分层情况（优秀/良好/待提高）
4. 共性问题与个性问题
5. 教学效果评估
6. 改进建议与下阶段计划

请用HTML格式输出，包含完整的报告结构、标题、表格、图表说明等。`

    const res = await api.post('/api/ai/ask', { 
      question: prompt,
      subject: '教学分析',
      context: ''
    })
    if (res.data.success) {
      aiResults.value.report = res.data.data
      ElMessage.success('学情报告生成成功！')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '生成失败')
  } finally {
    aiLoading.value.report = false
  }
}

// 下载报告
const downloadReport = () => {
  const content = aiResults.value.report
  const blob = new Blob([`
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>${selectedClass.value?.name} 学情报告</title>
      <style>
        body { font-family: "Microsoft YaHei", sans-serif; padding: 20px; }
        h1, h2, h3 { color: #333; }
        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #667eea; color: white; }
      </style>
    </head>
    <body>
      <h1>${selectedClass.value?.name} - 学情报告</h1>
      <p>生成时间：${dayjs().format('YYYY年MM月DD日 HH:mm')}</p>
      ${content}
    </body>
    </html>
  `], { type: 'text/html;charset=utf-8' })
  
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${selectedClass.value?.name}_学情报告_${dayjs().format('YYYYMMDD')}.html`
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('报告已下载！')
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.class-manage-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.class-card {
  transition: all 0.3s ease;
}

.class-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.class-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.class-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.more-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.more-icon:hover {
  color: var(--neon-blue);
}

.class-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.class-desc {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.class-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 12px;
}

.stat-icon {
  font-size: 32px;
  color: var(--neon-blue);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.invite-code-section {
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 2px dashed var(--neon-blue);
  border-radius: 12px;
}

.invite-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.invite-code {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.code-text {
  font-size: 24px;
  font-weight: 900;
  color: var(--neon-blue);
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
}

.class-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-tertiary);
}

@media (max-width: 768px) {
  .classes-grid {
    grid-template-columns: 1fr;
  }
}

/* AI分析按钮 */
.ai-analysis-btn {
  width: 100%;
  margin-top: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.ai-analysis-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* AI面板样式 */
.ai-panel {
  padding: 20px;
  min-height: 400px;
}

.ai-result {
  margin-top: 24px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

.result-content {
  line-height: 1.8;
  color: var(--text-primary);
}

.result-content h1,
.result-content h2,
.result-content h3 {
  color: #667eea;
  margin: 20px 0 12px 0;
}

.result-content h1 {
  font-size: 24px;
  border-bottom: 3px solid #667eea;
  padding-bottom: 10px;
}

.result-content h2 {
  font-size: 20px;
  border-left: 4px solid #667eea;
  padding-left: 12px;
}

.result-content h3 {
  font-size: 18px;
}

.result-content ul,
.result-content ol {
  margin: 16px 0;
  padding-left: 24px;
}

.result-content li {
  margin: 8px 0;
}

.result-content p {
  margin: 12px 0;
}

.result-content strong {
  color: #667eea;
  font-weight: 700;
}

.result-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.result-content th,
.result-content td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}

.result-content th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.result-content tr:nth-child(even) {
  background-color: rgba(102, 126, 234, 0.05);
}

.download-btn {
  margin-top: 20px;
  width: 100%;
  font-weight: 600;
}

/* Tab样式增强 */
:deep(.el-tabs--border-card) {
  border-radius: 12px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-tabs__item) {
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}

:deep(.el-tabs__item:hover) {
  color: #667eea;
}
</style>

