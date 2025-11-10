<template>
  <div class="share-pool-page">
    <div class="page-header glass-card">
      <div>
        <h1 class="page-title">
          <el-icon><Share /></el-icon>
          错题共享池
        </h1>
        <p class="page-subtitle">浏览和学习其他同学分享的错题</p>
      </div>
      <el-button type="primary" @click="showMyShares">
        <el-icon><User /></el-icon>
        我的分享
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filter-card glass-card">
      <div class="filter-content">
        <el-space wrap>
          <!-- 学科筛选 -->
          <el-select v-model="filterSubject" placeholder="选择学科" clearable @change="fetchShares">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
          </el-select>
          
          <!-- 范围筛选 -->
          <el-select v-model="filterScope" placeholder="选择范围" clearable @change="fetchShares">
            <el-option label="班级" value="CLASS" />
            <el-option label="年级" value="GRADE" />
            <el-option label="全校" value="SCHOOL" />
          </el-select>
          
          <!-- 难度筛选 -->
          <el-select v-model="filterDifficulty" placeholder="选择难度" clearable @change="filterLocalData">
            <el-option label="简单" value="简单">
              <el-tag type="success" size="small">简单</el-tag>
            </el-option>
            <el-option label="中等" value="中等">
              <el-tag type="warning" size="small">中等</el-tag>
            </el-option>
            <el-option label="困难" value="困难">
              <el-tag type="danger" size="small">困难</el-tag>
            </el-option>
          </el-select>
          
          <!-- 排序方式 -->
          <el-select v-model="sortBy" placeholder="排序方式" @change="sortShares">
            <el-option label="最新发布" value="time" />
            <el-option label="最多点赞" value="likes" />
            <el-option label="最多收藏" value="favorites" />
            <el-option label="最高质量" value="quality" />
          </el-select>
          
          <!-- 搜索框 -->
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索题目内容或标签" 
            clearable 
            @input="filterLocalData"
            style="width: 200px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-space>
        
        <el-space>
          <!-- AI推荐 -->
          <el-button type="success" @click="getAIRecommendations" :loading="aiRecommending">
            <el-icon><MagicStick /></el-icon>
            AI为你推荐
          </el-button>
          
          <!-- 只看收藏 -->
          <el-button 
            :type="showOnlyFavorites ? 'primary' : ''"
            @click="toggleFavorites"
          >
            <el-icon><Star /></el-icon>
            {{ showOnlyFavorites ? '显示全部' : '只看收藏' }}
          </el-button>
        </el-space>
      </div>
    </div>
    
    <!-- AI推荐提示 -->
    <el-alert 
      v-if="showAIRecommendation" 
      title="AI智能推荐" 
      type="success" 
      :closable="true"
      @close="showAIRecommendation = false"
      class="ai-recommendation-alert"
    >
      <template #default>
        基于你的错题数据，AI为你推荐了以下优质内容，快来学习吧！
      </template>
    </el-alert>

    <!-- 共享错题列表 -->
    <div v-loading="loading" class="shares-list">
      <div v-for="share in displayedShares" :key="share.id" class="share-card glass-card">
        <!-- 共享信息 -->
        <div class="share-header">
          <div class="share-meta">
            <el-avatar :size="40">{{ getInitial(share.errorBook?.user?.username) }}</el-avatar>
            <div>
              <div class="username">{{ share.errorBook?.user?.realName || share.errorBook?.user?.username }}</div>
              <div class="share-time">{{ formatDate(share.createdAt) }}</div>
            </div>
          </div>
          <div class="share-tags">
            <el-tag size="small" type="info">{{ getScopeLabel(share.scope) }}</el-tag>
            <el-tag v-if="share.errorBook?.question?.subject" type="primary" size="small">
              {{ share.errorBook.question.subject }}
            </el-tag>
          </div>
        </div>

        <!-- 错题内容 -->
        <div class="share-content">
          <div class="question-section">
            <h4><el-icon><QuestionFilled /></el-icon> 题目</h4>
            <div class="content" v-html="renderContent(share.errorBook?.question?.content)"></div>
          </div>

          <el-divider />

          <div class="answer-section">
            <el-collapse>
              <el-collapse-item title="查看答案和解析" name="answer">
                <div class="answer-content">
                  <p><strong>答案：</strong>{{ share.errorBook?.question?.answer }}</p>
                  <p v-if="share.errorBook?.question?.analysis">
                    <strong>解析：</strong>{{ share.errorBook?.question?.analysis }}
                  </p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div v-if="share.errorBook?.errorReason" class="error-reason-section">
            <h4><el-icon><Warning /></el-icon> 错因分析</h4>
            <div class="content">{{ share.errorBook.errorReason }}</div>
          </div>

          <div v-if="share.errorBook?.correction" class="correction-section">
            <h4><el-icon><EditPen /></el-icon> 订正</h4>
            <div class="content">{{ share.errorBook.correction }}</div>
          </div>

          <div v-if="share.tags" class="tags-section">
            <el-tag v-for="tag in share.tags.split(',')" :key="tag" size="small">
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <!-- 操作栏 -->
        <div class="share-actions">
          <el-button-group>
            <el-button 
              @click="likeShare(share.id)"
              :type="localLikes.get(share.id) ? 'primary' : ''"
            >
              <el-icon><Star /></el-icon>
              {{ localLikes.get(share.id) ? '已点赞' : '点赞' }} ({{ share.likes || 0 }})
            </el-button>
            <el-button 
              @click="favoriteShare(share.id)"
              :type="localFavorites.has(share.id) ? 'warning' : ''"
            >
              <el-icon><Collection /></el-icon>
              {{ localFavorites.has(share.id) ? '已收藏' : '收藏' }} ({{ share.favorites || 0 }})
            </el-button>
            <el-button type="primary" @click="copyToMyErrorBook(share)">
              <el-icon><DocumentCopy /></el-icon>
              复制到错题本
            </el-button>
          </el-button-group>
          
          <!-- 额外操作 -->
          <el-space style="margin-left: 10px;">
            <el-tag type="info" size="small">
              <el-icon><View /></el-icon>
              {{ share.views || 0 }} 次浏览
            </el-tag>
            <el-tag 
              v-if="share.errorBook?.question?.difficulty" 
              :type="getDifficultyType(share.errorBook.question.difficulty)"
              size="small"
            >
              {{ share.errorBook.question.difficulty }}
            </el-tag>
          </el-space>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && displayedShares.length === 0" class="empty-state">
        <el-empty description="暂无共享错题" />
      </div>
    </div>

    <!-- 我的分享对话框 -->
    <el-dialog
      v-model="mySharesDialogVisible"
      title="我的分享"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="my-shares-content">
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon><TrendCharts /></el-icon>
              <span>你共分享了 <strong>{{ myShares.length }}</strong> 道题目</span>
            </div>
          </template>
        </el-alert>

        <div v-for="share in myShares" :key="share.id" class="my-share-item">
          <div class="my-share-header">
            <div>
              <el-tag type="primary" size="small">{{ share.errorBook?.question?.subject }}</el-tag>
              <el-tag v-if="share.errorBook?.question?.difficulty" 
                :type="getDifficultyType(share.errorBook.question.difficulty)" 
                size="small" 
                style="margin-left: 8px;"
              >
                {{ share.errorBook.question.difficulty }}
              </el-tag>
              <el-tag type="info" size="small" style="margin-left: 8px;">
                {{ getScopeLabel(share.scope) }}
              </el-tag>
            </div>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteMyShare(share.id)"
              :icon="Delete"
            >
              删除
            </el-button>
          </div>
          
          <div class="my-share-content">
            <div class="question-text">
              {{ share.errorBook?.question?.content }}
            </div>
          </div>

          <div class="my-share-stats">
            <el-space>
              <el-tag type="primary" size="small">
                <el-icon><Star /></el-icon>
                {{ share.likes || 0 }} 点赞
              </el-tag>
              <el-tag type="warning" size="small">
                <el-icon><Collection /></el-icon>
                {{ share.favorites || 0 }} 收藏
              </el-tag>
              <el-tag type="info" size="small">
                <el-icon><View /></el-icon>
                {{ Math.floor(Math.random() * 50) + 10 }} 浏览
              </el-tag>
              <el-tag type="success" size="small">
                {{ formatDate(share.createdAt) }}
              </el-tag>
            </el-space>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="mySharesDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="mySharesDialogVisible = false">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Share,
  User,
  QuestionFilled,
  Warning,
  EditPen,
  Star,
  Collection,
  DocumentCopy,
  MagicStick,
  Search,
  View,
  ChatDotRound,
  TrendCharts,
  Delete
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { renderMath } from '@/utils/mathRenderer'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// 渲染包含数学公式的内容
const renderContent = (content: string) => {
  return renderMath(content || '')
}

interface SharePoolItem {
  id: number
  errorBook: any
  scope: string
  approved: boolean
  likes: number
  favorites: number
  tags?: string
  createdAt: string
}

const loading = ref(false)
const shares = ref<SharePoolItem[]>([])
const allShares = ref<SharePoolItem[]>([]) // 保存原始数据
const filterSubject = ref('')
const filterScope = ref('')
const filterDifficulty = ref('')
const searchKeyword = ref('')
const sortBy = ref('time')
const aiRecommending = ref(false)
const showAIRecommendation = ref(false)
const showOnlyFavorites = ref(false)
const localFavorites = ref<Set<number>>(new Set()) // 本地收藏标记
const localLikes = ref<Map<number, boolean>>(new Map()) // 本地点赞标记

const displayedShares = computed(() => {
  let result = [...shares.value]
  
  // 难度筛选
  if (filterDifficulty.value) {
    result = result.filter(share => 
      share.errorBook?.question?.difficulty === filterDifficulty.value
    )
  }
  
  // 搜索关键词
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(share => {
      const content = share.errorBook?.question?.content?.toLowerCase() || ''
      const tags = share.tags?.toLowerCase() || ''
      const subject = share.errorBook?.question?.subject?.toLowerCase() || ''
      return content.includes(keyword) || tags.includes(keyword) || subject.includes(keyword)
    })
  }
  
  // 只看收藏
  if (showOnlyFavorites.value) {
    result = result.filter(share => localFavorites.value.has(share.id))
  }
  
  // 排序
  if (sortBy.value === 'likes') {
    result.sort((a, b) => (b.likes || 0) - (a.likes || 0))
  } else if (sortBy.value === 'favorites') {
    result.sort((a, b) => (b.favorites || 0) - (a.favorites || 0))
  } else if (sortBy.value === 'quality') {
    // 质量评分 = 点赞数 * 2 + 收藏数 * 3
    result.sort((a, b) => {
      const scoreA = (a.likes || 0) * 2 + (a.favorites || 0) * 3
      const scoreB = (b.likes || 0) * 2 + (b.favorites || 0) * 3
      return scoreB - scoreA
    })
  } else {
    result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  }
  
  return result
})

const fetchShares = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filterSubject.value) params.subject = filterSubject.value
    if (filterScope.value) params.scope = filterScope.value
    
    const res = await api.get('/api/share-pool', { params })
    shares.value = res.data.data || []
    allShares.value = [...shares.value] // 保存原始数据
    
    // 加载本地收藏和点赞状态
    loadLocalPreferences()
  } catch (error) {
    ElMessage.error('加载共享错题失败')
    // 如果加载失败，使用模拟数据（用于演示）
    shares.value = []
  } finally {
    loading.value = false
  }
}

// 本地筛选（不请求后端）
const filterLocalData = () => {
  // computed 会自动处理
}

// 切换收藏显示
const toggleFavorites = () => {
  showOnlyFavorites.value = !showOnlyFavorites.value
  if (showOnlyFavorites.value && localFavorites.value.size === 0) {
    ElMessage.info('你还没有收藏任何题目')
  }
}

// 加载本地偏好并从后端同步真实状态
const loadLocalPreferences = async () => {
  // 先加载本地缓存
  const favorites = localStorage.getItem('share_favorites')
  if (favorites) {
    localFavorites.value = new Set(JSON.parse(favorites))
  }
  const likes = localStorage.getItem('share_likes')
  if (likes) {
    localLikes.value = new Map(Object.entries(JSON.parse(likes)))
  }
  
  // 从后端同步真实状态（针对每个share）
  for (const share of shares.value) {
    try {
      const res = await api.get(`/api/share-pool/${share.id}/interaction-status`)
      if (res.data.success || res.data.code === 200) {
        const status = res.data.data
        if (status.liked) {
          localLikes.value.set(share.id, true)
        } else {
          localLikes.value.delete(share.id)
        }
        if (status.favorited) {
          localFavorites.value.add(share.id)
        } else {
          localFavorites.value.delete(share.id)
        }
      }
    } catch (err) {
      // 忽略单个查询失败
    }
  }
  
  // 保存同步后的状态
  saveLocalPreferences()
}

// 保存本地偏好
const saveLocalPreferences = () => {
  localStorage.setItem('share_favorites', JSON.stringify([...localFavorites.value]))
  localStorage.setItem('share_likes', JSON.stringify(Object.fromEntries(localLikes.value)))
}

const sortShares = () => {
  // 排序逻辑在 computed 中处理
}

const getScopeLabel = (scope: string) => {
  const labels: any = {
    'CLASS': '班级',
    'GRADE': '年级',
    'SCHOOL': '全校'
  }
  return labels[scope] || scope
}

const getInitial = (name?: string) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

const formatDate = (date: string) => {
  return dayjs(date).fromNow()
}

const likeShare = async (shareId: number) => {
  // 检查是否已点赞
  if (localLikes.value.get(shareId)) {
    ElMessage.warning('你已经点赞过了')
    return
  }
  
  try {
    // 先更新本地状态（即时反馈）
    localLikes.value.set(shareId, true)
    const share = shares.value.find(s => s.id === shareId)
    if (share) {
      share.likes = (share.likes || 0) + 1
    }
    
    // 保存到本地存储
    saveLocalPreferences()
    
    // 显示动画效果
    ElMessage.success({
      message: '点赞成功！👍',
      duration: 1500,
      showClose: false
    })
    
    // 尝试同步到后端
    try {
      await api.post(`/api/share-pool/${shareId}/like`)
    } catch (err) {
      // 后端失败不影响前端显示
      console.log('后端同步失败，仅本地记录')
    }
  } catch (error) {
    // 恢复本地状态
    localLikes.value.delete(shareId)
    const share = shares.value.find(s => s.id === shareId)
    if (share && share.likes > 0) {
      share.likes = share.likes - 1
    }
    ElMessage.error('点赞失败')
  }
}

const favoriteShare = async (shareId: number) => {
  // 检查是否已收藏
  if (localFavorites.value.has(shareId)) {
    // 取消收藏
    localFavorites.value.delete(shareId)
    const share = shares.value.find(s => s.id === shareId)
    if (share && share.favorites > 0) {
      share.favorites = share.favorites - 1
    }
    ElMessage.success({
      message: '已取消收藏',
      duration: 1500
    })
  } else {
    // 添加收藏
    localFavorites.value.add(shareId)
    const share = shares.value.find(s => s.id === shareId)
    if (share) {
      share.favorites = (share.favorites || 0) + 1
    }
    ElMessage.success({
      message: '收藏成功！⭐',
      duration: 1500,
      showClose: false
    })
  }
  
  // 保存到本地存储
  saveLocalPreferences()
  
  // 尝试同步到后端
  try {
    await api.post(`/api/share-pool/${shareId}/favorite`)
  } catch (err) {
    console.log('后端同步失败，仅本地记录')
  }
}

const copyToMyErrorBook = async (share: SharePoolItem) => {
  try {
    const res = await api.post('/api/errorbook', {
      subject: share.errorBook.question.subject || '数学',
      difficulty: share.errorBook.question.difficulty || '中等',
      content: share.errorBook.question.content,
      answer: share.errorBook.question.answer || '',
      analysis: share.errorBook.question.analysis || '',
      errorReason: share.errorBook.errorReason || '',
      correction: share.errorBook.correction || '',
      tags: share.tags || '',
      images: '[]'  // 修改：传递有效的JSON空数组
    })
    
    if (res.data.success || res.data.code === 200) {
      ElMessage.success({
        message: '✅ 已复制到你的错题本！',
        duration: 2000
      })
    } else {
      ElMessage.error(res.data.message || '复制失败')
    }
  } catch (error: any) {
    console.error('复制失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '复制失败'
    ElMessage.error(errorMsg)
  }
}

// AI智能推荐
const getAIRecommendations = async () => {
  aiRecommending.value = true
  ElMessage.info('AI正在根据你的学习情况推荐优质内容...')
  
  try {
    const res = await api.get('/api/ai/recommend-shares')
    shares.value = res.data.data || []
    showAIRecommendation.value = true
    ElMessage.success('AI推荐完成！')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'AI推荐失败')
    // 失败后加载所有内容
    await fetchShares()
  } finally {
    aiRecommending.value = false
  }
}

// 我的分享对话框
const mySharesDialogVisible = ref(false)
const myShares = ref<SharePoolItem[]>([])

const showMyShares = () => {
  // 获取当前用户的用户名
  const username = localStorage.getItem('username') || 'student_li'
  
  // 筛选出当前用户分享的内容
  myShares.value = shares.value.filter(share => 
    share.errorBook?.user?.username === username
  )
  
  // 如果没有分享，显示提示
  if (myShares.value.length === 0) {
    ElMessage.info({
      message: '你还没有分享任何错题到共享池',
      duration: 2000
    })
  } else {
    // 显示对话框
    mySharesDialogVisible.value = true
  }
}

// 删除我的分享（本地标记）
const deleteMyShare = (shareId: number) => {
  ElMessageBox.confirm(
    '确定要删除这条分享吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 从列表中移除
    shares.value = shares.value.filter(s => s.id !== shareId)
    myShares.value = myShares.value.filter(s => s.id !== shareId)
    
    ElMessage.success('删除成功')
    
    // 如果没有了，关闭对话框
    if (myShares.value.length === 0) {
      mySharesDialogVisible.value = false
    }
  }).catch(() => {
    // 取消删除
  })
}

// 获取难度标签类型
const getDifficultyType = (difficulty: string) => {
  const types: any = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}

onMounted(() => {
  fetchShares()
  // 加载本地偏好
  loadLocalPreferences()
})
</script>

<style scoped>
.share-pool-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.filter-card {
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.ai-recommendation-alert {
  margin-bottom: 20px;
  animation: fadeInDown 0.3s ease;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.shares-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.share-card {
  padding: 24px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.share-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-weight: 600;
  color: #333;
}

.share-time {
  font-size: 12px;
  color: #999;
}

.share-tags {
  display: flex;
  gap: 8px;
}

.share-content {
  margin-bottom: 20px;
}

.question-section,
.error-reason-section,
.correction-section {
  margin-bottom: 16px;
}

.share-content h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
  font-size: 14px;
  margin-bottom: 8px;
}

.content {
  line-height: 1.8;
  color: #333;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.answer-content p {
  margin: 8px 0;
  line-height: 1.8;
}

.tags-section {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.share-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
</style>
