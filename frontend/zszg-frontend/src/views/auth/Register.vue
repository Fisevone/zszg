<template>
  <div class="register-container">
    <!-- 静态背景 -->
    <div class="animated-bg"></div>

    <!-- 注册卡片 -->
    <div class="register-card glass-card">
      <div class="card-header">
        <h2 class="card-title">
          <span class="title-icon">✨</span>
          <span class="title-text">创建账号</span>
        </h2>
        <p class="card-subtitle">加入我们，开启高效学习之旅</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="username">
          <div class="input-wrapper">
            <el-input
              v-model="form.username"
              placeholder="用户名（4-20个字符）"
              size="large"
              class="premium-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item prop="password">
          <div class="input-wrapper">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（至少6个字符）"
              size="large"
              show-password
              class="premium-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <div class="input-wrapper">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              class="premium-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item prop="realName">
          <div class="input-wrapper">
            <el-input
              v-model="form.realName"
              placeholder="真实姓名"
              size="large"
              class="premium-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item prop="email">
          <div class="input-wrapper">
            <el-input
              v-model="form.email"
              type="email"
              placeholder="邮箱（选填）"
              size="large"
              class="premium-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><Message /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item prop="role">
          <div class="role-selector">
            <div class="role-label">
              <el-icon><UserFilled /></el-icon>
              <span>选择角色</span>
            </div>
            <div class="role-options">
              <div 
                class="role-option" 
                :class="{ active: form.role === 'ROLE_STUDENT' }"
                @click="form.role = 'ROLE_STUDENT'"
              >
                <div class="role-icon student-icon">🎓</div>
                <div class="role-info">
                  <div class="role-name">学生</div>
                  <div class="role-desc">学习、练习、共享</div>
                </div>
                <div class="role-check" v-if="form.role === 'ROLE_STUDENT'">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
              
              <div 
                class="role-option" 
                :class="{ active: form.role === 'ROLE_TEACHER' }"
                @click="form.role = 'ROLE_TEACHER'"
              >
                <div class="role-icon teacher-icon">👨‍🏫</div>
                <div class="role-info">
                  <div class="role-name">教师</div>
                  <div class="role-desc">教学、管理、反馈</div>
                </div>
                <div class="role-check" v-if="form.role === 'ROLE_TEACHER'">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <button
            class="premium-submit-btn"
            :class="{ loading: loading }"
            @click.prevent="handleRegister"
            :disabled="loading"
          >
            <span class="btn-bg"></span>
            <span class="btn-content">
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </span>
            <span class="btn-glow"></span>
          </button>
        </el-form-item>

        <div class="form-footer">
          <span class="footer-text">已有账号？</span>
          <a class="footer-link" @click="goToLogin">立即登录</a>
        </div>
      </el-form>

      <!-- 装饰元素 -->
      <div class="card-decoration tl"></div>
      <div class="card-decoration tr"></div>
      <div class="card-decoration bl"></div>
      <div class="card-decoration br"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Message,
  UserFilled,
  Check
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  role: 'ROLE_STUDENT' // 默认学生角色
})

const validatePassword = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码至少6个字符'))
  } else {
    if (form.value.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度4-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    loading.value = true
    const res = await api.post('/api/auth/register', {
      username: form.value.username,
      password: form.value.password,
      realName: form.value.realName,
      email: form.value.email,
      role: form.value.role
    })

    if (res.data.success) {
      ElMessage.success('注册成功！请登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.data.message || '注册失败')
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* ========== 容器布局 ========== */
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f0ff 0%, #e8eeff 50%, #f0f4ff 100%);
}

/* ========== 静态背景 ========== */
.animated-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* ========== 注册卡片 ========== */
.register-card {
  width: 100%;
  max-width: 480px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
  animation: scaleIn 0.6s ease;
}

/* ========== 卡片头部 ========== */
.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-title {
  font-size: 36px;
  font-weight: 900;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  animation: pulse 2s ease-in-out infinite;
}

.title-text {
  background: linear-gradient(135deg, #00f3ff 0%, #bf00ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

/* ========== 表单样式 ========== */
.register-form {
  margin-top: 32px;
}

.input-wrapper {
  position: relative;
  margin-bottom: 4px;
}

:deep(.premium-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
  padding: 14px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.premium-input .el-input__wrapper:hover),
:deep(.premium-input .el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--neon-blue);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.3);
}

:deep(.premium-input .el-input__inner) {
  color: var(--text-primary);
  font-size: 15px;
}

:deep(.premium-input .el-input__inner::placeholder) {
  color: var(--text-tertiary);
}

.input-icon {
  color: var(--neon-blue);
  font-size: 18px;
}

/* ========== 提交按钮 ========== */
.premium-submit-btn {
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.4s ease;
}

.premium-submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.premium-submit-btn .btn-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  transition: all 0.4s ease;
}

.premium-submit-btn .btn-content {
  position: relative;
  z-index: 1;
  color: white;
  display: block;
}

.premium-submit-btn .btn-glow {
  position: absolute;
  inset: -2px;
  border-radius: 12px;
  opacity: 0;
  background: linear-gradient(135deg, var(--neon-cyan), var(--neon-purple));
  filter: blur(15px);
  transition: opacity 0.4s ease;
  z-index: 0;
}

.premium-submit-btn:not(:disabled):hover {
  transform: translateY(-2px);
}

.premium-submit-btn:not(:disabled):hover .btn-glow {
  opacity: 0.8;
}

.premium-submit-btn:not(:disabled):active {
  transform: translateY(0);
}

.premium-submit-btn.loading .btn-bg {
  animation: shimmer 1.5s infinite;
}

/* ========== 角色选择器 ========== */
.role-selector {
  margin-bottom: 8px;
}

.role-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.role-label .el-icon {
  color: var(--neon-blue);
  font-size: 18px;
}

.role-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.role-option {
  position: relative;
  padding: 20px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-option:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.role-option.active {
  background: rgba(102, 126, 234, 0.1);
  border-color: var(--neon-blue);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.3);
}

.role-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border-radius: 10px;
  flex-shrink: 0;
}

.student-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.teacher-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.role-info {
  flex: 1;
}

.role-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.role-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.role-check {
  width: 24px;
  height: 24px;
  background: var(--neon-blue);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
  animation: scaleIn 0.3s ease;
}

/* ========== 表单底部 ========== */
.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.footer-text {
  margin-right: 8px;
}

.footer-link {
  color: var(--neon-blue);
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--neon-blue);
  transition: width 0.3s ease;
}

.footer-link:hover {
  color: var(--neon-cyan);
}

.footer-link:hover::after {
  width: 100%;
}

/* ========== 卡片装饰 ========== */
.card-decoration {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 2px solid rgba(102, 126, 234, 0.3);
  opacity: 0.5;
  transition: all 0.3s ease;
}

.card-decoration.tl {
  top: 20px;
  left: 20px;
  border-right: none;
  border-bottom: none;
}

.card-decoration.tr {
  top: 20px;
  right: 20px;
  border-left: none;
  border-bottom: none;
}

.card-decoration.bl {
  bottom: 20px;
  left: 20px;
  border-right: none;
  border-top: none;
}

.card-decoration.br {
  bottom: 20px;
  right: 20px;
  border-left: none;
  border-top: none;
}

.register-card:hover .card-decoration {
  border-color: var(--neon-blue);
  opacity: 1;
}

/* ========== 响应式设计 ========== */
@media (max-width: 640px) {
  .register-card {
    padding: 36px 24px;
  }

  .card-title {
    font-size: 28px;
  }

  .card-subtitle {
    font-size: 14px;
  }

  .role-options {
    grid-template-columns: 1fr;
  }

  .role-option {
    padding: 16px 12px;
  }

  .role-icon {
    width: 40px;
    height: 40px;
    font-size: 24px;
  }

  .role-name {
    font-size: 15px;
  }

  .role-desc {
    font-size: 12px;
  }
}
</style>
