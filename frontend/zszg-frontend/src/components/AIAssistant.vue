<template>
  <!-- AI助手悬浮按钮 -->
  <div class="ai-fab" @click="showDialog = true" v-if="!showDialog">
    <div class="fab-icon">
      <el-icon :size="32"><MagicStick /></el-icon>
    </div>
    <div class="fab-pulse"></div>
  </div>

  <!-- AI助手对话框 -->
  <el-dialog
    v-model="showDialog"
    title="🤖 AI智能助手"
    width="700px"
    class="ai-assistant-dialog"
    :close-on-click-modal="false"
  >
    <div class="assistant-container">
      <!-- 快捷功能按钮 -->
      <div class="quick-actions">
        <el-button size="small" @click="setQuickQuestion('这道题怎么做？')">
          <el-icon><QuestionFilled /></el-icon>
          解题帮助
        </el-button>
        <el-button size="small" @click="setQuickQuestion('能帮我分析一下错因吗？')">
          <el-icon><Warning /></el-icon>
          错因分析
        </el-button>
        <el-button size="small" @click="setQuickQuestion('有没有相似的练习题？')">
          <el-icon><Connection /></el-icon>
          相似题
        </el-button>
        <el-button size="small" @click="setQuickQuestion('这个知识点怎么学？')">
          <el-icon><Reading /></el-icon>
          学习建议
        </el-button>
      </div>

      <!-- 对话消息列表 -->
      <div class="messages-container" ref="messagesRef">
        <div v-if="messages.length === 0" class="welcome-message">
          <el-icon class="welcome-icon"><MagicStick /></el-icon>
          <h3>你好！我是AI学习助手 🎓</h3>
          <p>我可以帮你：</p>
          <ul>
            <li>📝 解答题目疑问</li>
            <li>🔍 分析错误原因</li>
            <li>📚 推荐学习资源</li>
            <li>🎯 制定学习计划</li>
          </ul>
          <p class="welcome-tip">快来问我吧！👇</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
          <div class="message-avatar">
            <span v-if="msg.role === 'user'">👤</span>
            <span v-else>🤖</span>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="loading" class="message-item ai">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="question"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题，按Enter发送..."
          @keydown.enter.exact.prevent="sendQuestion"
          @keydown.enter.shift.exact="question += '\n'"
          :disabled="loading"
        />
        <div class="input-actions">
          <span class="input-tip">Shift+Enter 换行，Enter 发送</span>
          <el-button type="primary" @click="sendQuestion" :loading="loading" :disabled="!question.trim()">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick, QuestionFilled, Warning, Connection, Reading, Promotion
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const showDialog = ref(false)
const question = ref('')
const messages = ref<any[]>([])
const loading = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

const formatMessage = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/【(.*?)】/g, '<strong style="color: #667eea;">【$1】</strong>')
    .replace(/(\d+\.)\s/g, '<br><strong>$1</strong> ')
    .replace(/•/g, '<br>•')
}

const setQuickQuestion = (q: string) => {
  question.value = q
}

const sendQuestion = async () => {
  if (!question.value.trim() || loading.value) return

  const userQuestion = question.value.trim()
  const currentTime = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userQuestion,
    time: currentTime
  })

  question.value = ''
  loading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    const res = await api.post('/api/ai/ask', {
      subject: '通用',
      question: userQuestion,
      context: null
    })

    // 添加AI回复
    messages.value.push({
      role: 'ai',
      content: res.data.data,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('AI回答失败，请稍后重试')
    // 移除用户消息
    messages.value.pop()
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
/* 悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  animation: bounce 2s infinite;
}

.ai-fab:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 40px rgba(102, 126, 234, 0.6);
}

.fab-icon {
  position: relative;
  z-index: 2;
}

.fab-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

/* 对话框样式 */
:deep(.ai-assistant-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 16px 16px 0 0;
}

:deep(.ai-assistant-dialog .el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 700;
}

:deep(.ai-assistant-dialog .el-dialog__body) {
  padding: 0;
}

.assistant-container {
  display: flex;
  flex-direction: column;
  height: 600px;
}

/* 快捷功能 */
.quick-actions {
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  border-radius: 20px;
  border-color: #667eea;
  color: #667eea;
}

.quick-actions .el-button:hover {
  background: #667eea;
  color: white;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #495057;
}

.welcome-icon {
  font-size: 64px;
  color: #667eea;
  margin-bottom: 16px;
}

.welcome-message h3 {
  font-size: 24px;
  margin-bottom: 16px;
  color: #2c3e50;
}

.welcome-message p {
  font-size: 16px;
  margin: 12px 0;
}

.welcome-message ul {
  text-align: left;
  display: inline-block;
  margin: 20px 0;
}

.welcome-message li {
  padding: 8px 0;
  font-size: 15px;
}

.welcome-tip {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
  margin-top: 20px;
}

/* 消息列表 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: white;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-text {
  padding: 14px 18px;
  border-radius: 16px;
  background: #f8f9fa;
  color: #495057;
  line-height: 1.6;
  font-size: 15px;
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #adb5bd;
  padding: 0 8px;
}

.message-item.user .message-time {
  text-align: right;
}

/* 输入中动画 */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 14px 18px;
  background: #f8f9fa;
  border-radius: 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* 输入区域 */
.input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #e9ecef;
}

:deep(.input-area .el-textarea__inner) {
  border-radius: 12px;
  padding: 12px;
  font-size: 15px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-tip {
  font-size: 12px;
  color: #adb5bd;
}
</style>


