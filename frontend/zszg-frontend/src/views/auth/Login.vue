<template>
  <div class="login-container">
    <!-- 静态背景 -->
    <div class="animated-bg"></div>

    <!-- 登录卡片 -->
    <div class="login-card glass-card">
      <div class="card-header">
        <div class="logo-wrapper">
          <div class="logo-circle">
            <span class="logo-text">知</span>
          </div>
        </div>
        <h2 class="card-title">欢迎回来</h2>
        <p class="card-subtitle">登录到知错就改学习平台</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <div class="input-wrapper">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              class="premium-input"
              @keyup.enter="handleLogin"
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
              placeholder="请输入密码"
              size="large"
              show-password
              class="premium-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item>
          <button
            class="premium-submit-btn"
            :class="{ loading: loading }"
            @click.prevent="handleLogin"
            :disabled="loading"
          >
            <span class="btn-bg"></span>
            <span class="btn-content">
              <span v-if="!loading">立即登录</span>
              <span v-else>登录中...</span>
            </span>
            <span class="btn-glow"></span>
          </button>
        </el-form-item>

        <div class="form-footer">
          <span class="footer-text">还没有账号？</span>
          <a class="footer-link" @click="goToRegister">立即注册</a>
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
import { User, Lock } from '@element-plus/icons-vue'
import api from '@/lib/api'

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    loading.value = true
    const res = await api.post('/api/auth/login', {
      username: form.value.username,
      password: form.value.password
    })

    if (res.data.success) {
      const { token, username, role, userId, realName, email } = res.data.data
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('role', role)
      // 保存完整的用户信息
      localStorage.setItem('user', JSON.stringify({
        id: userId,
        username: username,
        realName: realName,
        email: email,
        role: role
      }))
      
      ElMessage.success('登录成功！')
      
      // 根据角色跳转
      if (role === 'ROLE_STUDENT') {
        router.push('/student/errorbook')
      } else if (role === 'ROLE_TEACHER') {
        router.push('/teacher/dashboard')
      } else if (role === 'ROLE_ADMIN') {
        router.push('/admin/users')
      }
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* ========== 容器布局 ========== */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 50%, #f5f0ff 100%);
}

/* ========== 静态背景 ========== */
.animated-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* ========== 登录卡片 ========== */
.login-card {
  width: 100%;
  max-width: 440px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
  animation: scaleIn 0.6s ease;
}

/* ========== 卡片头部 ========== */
.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  margin-bottom: 24px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
  animation: pulse 3s ease-in-out infinite;
  position: relative;
}

.logo-circle::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  filter: blur(10px);
  opacity: 0.6;
  z-index: -1;
}

.logo-text {
  font-size: 36px;
  font-weight: 900;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.card-title {
  font-size: 32px;
  font-weight: 900;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #ffffff 0%, #b8c5d6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* ========== 表单样式 ========== */
.login-form {
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
  margin-top: 8px;
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

.login-card:hover .card-decoration {
  border-color: var(--neon-blue);
  opacity: 1;
}

/* ========== 响应式设计 ========== */
@media (max-width: 640px) {
  .login-card {
    padding: 36px 24px;
  }

  .card-title {
    font-size: 26px;
  }

  .logo-circle {
    width: 70px;
    height: 70px;
  }

  .logo-text {
    font-size: 32px;
  }
}
</style>
