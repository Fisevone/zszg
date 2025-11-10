
<template>
  <el-container style="height: 100vh">
    <el-aside :width="collapsed ? '64px' : '240px'" class="aside">
      <div class="brand" @click="$router.push('/home')">
        <div class="logo">知</div>
        <transition name="fade">
          <div v-if="!collapsed" class="title">知错就改</div>
        </transition>
      </div>
      <el-menu 
        :router="true" 
        :default-active="$route.path" 
        :collapse="collapsed"
        class="sidebar-menu"
      >
        <el-menu-item index="/home" class="menu-item">
          <el-icon><House /></el-icon><span>首页</span>
        </el-menu-item>
        
        <!-- 学生功能菜单 -->
        <template v-if="role==='ROLE_STUDENT'">
          <el-menu-item index="/student/errorbook"><el-icon><Notebook /></el-icon><span>错题本</span></el-menu-item>
          <el-menu-item index="/student/my-classes"><el-icon><School /></el-icon><span>我的班级</span></el-menu-item>
          <el-menu-item index="/student/tasks" class="task-menu-item">
            <el-icon><List /></el-icon>
            <span>教师推送</span>
            <el-badge v-if="pendingTaskCount > 0" :value="pendingTaskCount" :max="99" class="task-badge" />
          </el-menu-item>
          <el-menu-item index="/student/share"><el-icon><Share /></el-icon><span>共享池</span></el-menu-item>
          <el-menu-item index="/student/recommend"><el-icon><Star /></el-icon><span>推荐</span></el-menu-item>
          <el-menu-item index="/student/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
        </template>
        
        <!-- 教师功能菜单 -->
        <template v-if="role==='ROLE_TEACHER' || role==='ROLE_ADMIN'">
          <el-menu-item index="/teacher/class-manage"><el-icon><School /></el-icon><span>班级管理</span></el-menu-item>
          <el-menu-item index="/teacher/dashboard"><el-icon><DataAnalysis /></el-icon><span>教学数据</span></el-menu-item>
          <el-menu-item index="/teacher/task-publish"><el-icon><Promotion /></el-icon><span>任务推送</span></el-menu-item>
          <el-menu-item index="/teacher/resources"><el-icon><Folder /></el-icon><span>资源管理</span></el-menu-item>
          <el-menu-item index="/teacher/feedback"><el-icon><ChatDotRound /></el-icon><span>教学反馈</span></el-menu-item>
        </template>
        
        <!-- 管理员功能菜单 -->
        <template v-if="role==='ROLE_ADMIN'">
          <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
          <el-menu-item index="/admin/questions"><el-icon><List /></el-icon><span>题库管理</span></el-menu-item>
          <el-menu-item index="/admin/audit"><el-icon><CircleCheck /></el-icon><span>审核管理</span></el-menu-item>
          <el-menu-item index="/admin/settings"><el-icon><Tools /></el-icon><span>系统设置</span></el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-inner">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon><Menu /></el-icon>
          </button>
          <div class="spacer" />
          <el-button v-if="!token" type="primary" @click="$router.push('/login')">登录</el-button>
          <el-dropdown v-else class="user-dropdown">
            <span class="el-dropdown-link">
              <div class="user-avatar">{{ displayName?.charAt(0) }}</div>
              <div class="user-info-display">
                <span class="uname">{{ displayName }}</span>
                <span class="user-role-tag">{{ roleLabel }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 全局AI助手 -->
    <AIAssistant />
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import {
  ArrowDown, Menu, House, School, Notebook, Document, Share, Star, User, Monitor,
  DataAnalysis, Folder, ChatDotRound, Setting, UserFilled, List, CircleCheck, Tools, SwitchButton, Promotion
} from '@element-plus/icons-vue'
import AIAssistant from '@/components/AIAssistant.vue'
import api from '@/lib/api'

const token = computed(() => localStorage.getItem('token'))
const role = computed(() => localStorage.getItem('role'))

// 待完成任务数量
const pendingTaskCount = ref(0)

// 获取用户信息对象
const userInfo = computed(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
})

// 显示真实姓名，如果没有则显示用户名
const displayName = computed(() => {
  if (userInfo.value) {
    return userInfo.value.realName || userInfo.value.username
  }
  return localStorage.getItem('username') || '用户'
})

// 获取角色标识（用于区分）
const roleLabel = computed(() => {
  const roleMap: any = {
    'ROLE_STUDENT': '学生',
    'ROLE_TEACHER': '教师',
    'ROLE_ADMIN': '管理员'
  }
  return roleMap[role.value || ''] || ''
})

const collapsed = ref(false)

// 获取待完成任务数量
async function fetchPendingTaskCount() {
  if (role.value !== 'ROLE_STUDENT' || !userInfo.value?.id) return
  
  try {
    const res = await api.get(`/api/tasks/student/${userInfo.value.id}`)
    if (res.data.success && res.data.data) {
      // 计算未完成的任务数量
      pendingTaskCount.value = res.data.data.filter((task: any) => !task.isCompleted).length
    }
  } catch (error) {
    console.error('获取任务数量失败:', error)
  }
}

// 组件挂载时获取任务数量
onMounted(() => {
  if (role.value === 'ROLE_STUDENT') {
    fetchPendingTaskCount()
    // 每30秒刷新一次任务数量
    setInterval(fetchPendingTaskCount, 30000)
  }
})

function goToProfile() {
  if (role.value === 'ROLE_STUDENT') {
    window.location.href = '/student/profile'
  } else if (role.value === 'ROLE_TEACHER' || role.value === 'ROLE_ADMIN') {
    window.location.href = '/teacher/dashboard'
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('user')
  location.href = '/login'
}
</script>

<style scoped>
/* ========== 侧边栏 ========== */
.aside {
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
  border-right: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 2px 0 16px rgba(102, 126, 234, 0.08);
  transition: all 0.3s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.brand:hover {
  background: rgba(102, 126, 234, 0.05);
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 20px;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.title {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ========== 菜单样式 ========== */
.sidebar-menu {
  border: none;
  background: transparent;
  padding: 8px;
}

:deep(.el-menu-item) {
  border-radius: 12px;
  margin: 4px 0;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: rgba(102, 126, 234, 0.08) !important;
  color: var(--neon-blue);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15)) !important;
  color: var(--neon-blue);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

:deep(.el-sub-menu__title) {
  border-radius: 12px;
  margin: 4px 0;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(102, 126, 234, 0.08) !important;
  color: var(--neon-blue);
}

/* ========== 任务徽章 ========== */
.task-menu-item {
  position: relative;
}

.task-badge {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

:deep(.task-badge .el-badge__content) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
  font-weight: 600;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* ========== 顶部栏 ========== */
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 2px 16px rgba(102, 126, 234, 0.08);
  height: 60px;
  padding: 0;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  height: 100%;
}

.collapse-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--neon-blue);
  font-size: 18px;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: scale(1.05);
}

.spacer {
  flex: 1;
}

/* ========== 用户下拉菜单 ========== */
.user-dropdown {
  cursor: pointer;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.08);
  transition: all 0.3s ease;
}

.el-dropdown-link:hover {
  background: rgba(102, 126, 234, 0.15);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  text-transform: uppercase;
  flex-shrink: 0;
}

.user-info-display {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.uname {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.2;
}

.user-role-tag {
  font-size: 11px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
  line-height: 1;
}

.dropdown-icon {
  color: var(--text-secondary);
  font-size: 12px;
}

/* ========== 主内容区 ========== */
.main-content {
  background: #f5f7fa;
  padding: 24px;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
