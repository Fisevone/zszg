<template>
  <div class="my-classes-container">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><School /></el-icon>
        我的班级
      </h2>
      <el-button type="primary" @click="joinDialog = true">
        <el-icon><Plus /></el-icon>
        加入班级
      </el-button>
    </div>

    <!-- 班级列表 -->
    <div v-if="classes.length > 0" class="classes-grid">
      <el-card 
        v-for="cls in classes" 
        :key="cls.id" 
        class="class-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <div class="class-info">
              <h3 class="class-name">{{ cls.className }}</h3>
              <el-tag type="success" size="small">{{ cls.gradeLevel }}</el-tag>
            </div>
            <el-button 
              type="danger" 
              size="small" 
              text
              @click="handleLeave(cls)"
            >
              退出班级
            </el-button>
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
                <div class="stat-value">{{ cls.studentCount || 0 }}</div>
                <div class="stat-label">班级人数</div>
              </div>
            </div>

            <div class="stat-item">
              <el-icon class="stat-icon"><Bell /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ cls.pushCount || 0 }}</div>
                <div class="stat-label">教师推送</div>
              </div>
            </div>
          </div>

          <div class="teacher-info">
            <el-icon><Monitor /></el-icon>
            <span>教师：{{ cls.teacherName || '未知' }}</span>
          </div>

          <div class="class-time">
            <el-icon><Clock /></el-icon>
            加入于 {{ formatTime(cls.joinedAt) }}
          </div>

          <el-button 
            type="primary" 
            class="view-push-btn"
            @click="viewPushes(cls)"
          >
            <el-icon><Bell /></el-icon>
            查看教师推送
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-else
      description="还没有加入任何班级，点击右上角加入班级吧！"
      :image-size="200"
    >
      <el-button type="primary" @click="joinDialog = true">
        立即加入班级
      </el-button>
    </el-empty>

    <!-- 加入班级对话框 -->
    <el-dialog
      v-model="joinDialog"
      title="加入班级"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef" label-width="80px">
        <el-form-item label="邀请码" prop="inviteCode">
          <el-input 
            v-model="joinForm.inviteCode" 
            placeholder="请输入6位邀请码"
            maxlength="6"
            clearable
            style="text-transform: uppercase;"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">
            💡 邀请码由教师创建班级时生成，请向教师索取
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="joinDialog = false">取消</el-button>
        <el-button type="primary" @click="handleJoin" :loading="joining">
          加入班级
        </el-button>
      </template>
    </el-dialog>

    <!-- 教师推送列表对话框 -->
    <el-dialog
      v-model="pushDialog"
      :title="`${selectedClass?.className} - 教师推送`"
      width="800px"
    >
      <el-timeline v-if="pushes.length > 0">
        <el-timeline-item
          v-for="push in pushes"
          :key="push.id"
          :timestamp="formatTime(push.createdAt)"
          placement="top"
        >
          <el-card>
            <template #header>
              <div class="push-header">
                <el-tag 
                  :type="getResourceTypeTag(push.resourceType)" 
                  size="small"
                >
                  {{ getResourceTypeName(push.resourceType) }}
                </el-tag>
                <h4>{{ push.title }}</h4>
              </div>
            </template>
            <div class="push-content">
              {{ push.content }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <el-empty 
        v-else
        description="还没有收到教师推送"
        :image-size="150"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  School, Plus, User, Document, Clock, Key, Bell, Monitor
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'

const classes = ref<any[]>([])
const pushes = ref<any[]>([])
const selectedClass = ref<any>(null)

const joinDialog = ref(false)
const pushDialog = ref(false)

const joining = ref(false)
const loadingPushes = ref(false)

const joinFormRef = ref()

const joinForm = ref({
  inviteCode: ''
})

const joinRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    { min: 6, max: 6, message: '邀请码为6位', trigger: 'blur' }
  ]
}

// 加载班级列表
const loadClasses = async () => {
  try {
    const res = await api.get('/api/classroom/student/classes')
    if (res.data.success) {
      classes.value = res.data.data
    }
  } catch (error) {
    console.error('加载班级列表失败', error)
    ElMessage.error('加载班级列表失败')
  }
}

// 加入班级
const handleJoin = async () => {
  if (!joinFormRef.value) return
  
  try {
    await joinFormRef.value.validate()
    joining.value = true
    
    const res = await api.post('/api/classroom/join', {
      inviteCode: joinForm.value.inviteCode.toUpperCase()
    })
    
    if (res.data.success) {
      ElMessage.success('加入班级成功！')
      joinDialog.value = false
      joinForm.value = { inviteCode: '' }
      await loadClasses()
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('加入班级失败')
    }
  } finally {
    joining.value = false
  }
}

// 退出班级
const handleLeave = async (cls: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出 ${cls.className} 吗？退出后将无法接收教师推送。`,
      '确认退出',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await api.delete(`/api/classroom/class/${cls.classId}/leave`)
    
    if (res.data.success) {
      ElMessage.success('已退出班级')
      await loadClasses()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('退出班级失败')
      }
    }
  }
}

// 查看教师推送
const viewPushes = async (cls: any) => {
  selectedClass.value = cls
  pushDialog.value = true
  loadingPushes.value = true
  
  try {
    const res = await api.get(`/api/classroom/class/${cls.classId}/pushes`)
    if (res.data.success) {
      pushes.value = res.data.data
    }
  } catch (error) {
    console.error('加载推送列表失败', error)
    ElMessage.error('加载推送列表失败')
  } finally {
    loadingPushes.value = false
  }
}

// 获取资源类型标签颜色
const getResourceTypeTag = (type: string) => {
  const map: Record<string, string> = {
    NOTICE: 'warning',
    QUESTION: 'primary',
    KNOWLEDGE: 'success'
  }
  return map[type] || 'info'
}

// 获取资源类型名称
const getResourceTypeName = (type: string) => {
  const map: Record<string, string> = {
    NOTICE: '📢 通知',
    QUESTION: '📄 题目',
    KNOWLEDGE: '📚 知识点'
  }
  return map[type] || '📋 资源'
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
.my-classes-container {
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-item {
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

.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.class-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.view-push-btn {
  width: 100%;
  margin-top: 8px;
}

.form-tip {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.push-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.push-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.push-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .classes-grid {
    grid-template-columns: 1fr;
  }

  .class-stats {
    grid-template-columns: 1fr;
  }
}
</style>



