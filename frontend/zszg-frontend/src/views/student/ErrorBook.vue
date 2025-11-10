<template>
  <div class="error-book-container">
    <!-- 顶部标题栏 -->
    <div class="top-bar">
      <div class="title-section">
        <div class="icon-wrapper">
          <el-icon class="title-icon"><Notebook /></el-icon>
        </div>
        <div>
          <h1 class="main-title">错题本</h1>
          <p class="subtitle">记录每一次进步的足迹</p>
        </div>
      </div>
      <el-button type="primary" size="large" class="add-button" @click="showDialog = true">
          <el-icon><Plus /></el-icon>
        添加错题
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ errorBooks.length }}</div>
          <div class="stat-label">总错题数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon corrected">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ correctedCount }}</div>
          <div class="stat-label">已订正</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon shared">
          <el-icon><Share /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ sharedCount }}</div>
          <div class="stat-label">已共享</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rate">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ correctionRate }}%</div>
          <div class="stat-label">订正率</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterSubject" placeholder="学科筛选" clearable @change="fetchErrorBooks" class="filter-select">
          <el-option label="数学" value="数学" />
          <el-option label="语文" value="语文" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
          <el-option label="生物" value="生物" />
        </el-select>
      <el-input v-model="searchKeyword" placeholder="搜索题目内容..." class="search-input" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 错题列表 -->
    <div v-loading="loading" class="error-list">
      <TransitionGroup name="list">
        <div v-for="error in filteredErrorBooks" :key="error.id" class="error-item">
          <div class="error-item-header">
            <div class="header-left">
              <span class="subject-badge" :class="getSubjectClass(error.question.subject)">
                  {{ error.question.subject }}
              </span>
              <span v-if="error.question.difficulty" class="difficulty-badge" :class="getDifficultyClass(error.question.difficulty)">
                  {{ error.question.difficulty }}
              </span>
              <span v-if="error.status === 'SHARED'" class="status-badge shared">
                <el-icon><Share /></el-icon>
                已共享
              </span>
              </div>
            <div class="header-actions">
              <el-button 
                v-if="error.status !== 'SHARED'"
                link 
                type="success" 
                @click="shareToPool(error)" 
                class="share-btn"
              >
                <el-icon><Share /></el-icon>
                分享到共享池
              </el-button>
              <el-button link type="info" @click="extractKnowledgePoints(error)" class="ai-btn">
                <el-icon><Collection /></el-icon>
                知识图谱
              </el-button>
              <el-button link type="primary" @click="showAIAnalysis(error)" class="ai-btn">
                <el-icon><MagicStick /></el-icon>
                AI分析
              </el-button>
              <el-button link type="warning" @click="getSimilarQuestions(error)" class="ai-btn">
                <el-icon><Connection /></el-icon>
                推荐相似题
              </el-button>
              <el-button link type="success" @click="askAIQuestion(error)" class="ai-btn">
                <el-icon><ChatDotRound /></el-icon>
                向AI提问
              </el-button>
              <el-button link @click="viewTeacherFeedback(error.id)" class="teacher-feedback-btn">
                <el-icon><ChatDotRound /></el-icon>
                教师反馈
              </el-button>
              <el-button link type="primary" @click="editError(error)">
                  <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" @click="deleteError(error.id)">
                  <el-icon><Delete /></el-icon>
                删除
              </el-button>
              </div>
            </div>

          <div class="error-item-body">
            <div class="question-block">
              <div class="block-title">
                <el-icon class="block-icon"><QuestionFilled /></el-icon>
                题目
                </div>
              <!-- 如果有原图，显示图文对照 -->
              <div v-if="error.imageUrl" class="question-with-image">
                <div class="question-text" v-html="renderContent(error.question.content)"></div>
                <div class="question-image">
                  <div class="image-label">📷 原题图片</div>
                  <img :src="error.imageUrl" alt="题目原图" @click="previewImage(error.imageUrl)" />
                </div>
              </div>
              <div v-else class="block-content" v-html="renderContent(error.question.content)"></div>
              </div>

            <div v-if="error.question.answer" class="answer-block">
              <div class="block-title">
                <el-icon class="block-icon"><Check /></el-icon>
                正确答案
                </div>
              <div class="block-content">{{ error.question.answer }}</div>
              </div>

            <div v-if="error.errorReason" class="reason-block">
              <div class="block-title">
                <el-icon class="block-icon"><Warning /></el-icon>
                错因分析
                </div>
              <div class="block-content reason-text">{{ error.errorReason }}</div>
              </div>

            <div v-if="error.correction" class="correction-block">
              <div class="block-title">
                <el-icon class="block-icon"><EditPen /></el-icon>
                我的订正
                </div>
              <div class="block-content">{{ error.correction }}</div>
              </div>

            <div v-if="error.tags" class="tags-row">
              <el-tag v-for="tag in error.tags.split(',')" :key="tag" size="small" class="tag-item">
                  {{ tag }}
                </el-tag>
              </div>

            <!-- AI分析结果 -->
            <div v-if="error.aiAnalysis" class="ai-analysis-section">
              <div class="ai-header">
                <el-icon class="ai-icon"><MagicStick /></el-icon>
                <span class="ai-title">AI智能分析</span>
                <el-tag type="success" size="small">智能助手</el-tag>
              </div>
              <div class="ai-content" v-html="error.aiAnalysis"></div>
              <div class="ai-actions">
                <el-button size="small" @click="askAIQuestion(error)">
                  <el-icon><ChatDotRound /></el-icon>
                  向AI提问
                </el-button>
                <el-button size="small" @click="getSimilarQuestions(error)">
                  <el-icon><Connection /></el-icon>
                  推荐相似题
                </el-button>
              </div>
              </div>
            </div>

          <div class="error-item-footer">
            <span class="time-text">
              <el-icon><Clock /></el-icon>
              {{ formatDate(error.createdAt) }}
            </span>
        </div>
      </div>
      </TransitionGroup>

    <!-- 空状态 -->
      <div v-if="!loading && errorBooks.length === 0" class="empty-container">
        <div class="empty-illustration">
        <el-icon><DocumentDelete /></el-icon>
      </div>
        <h3 class="empty-title">暂无错题记录</h3>
        <p class="empty-desc">记录你的错题，让每次错误都成为进步的阶梯</p>
        <el-button type="primary" size="large" @click="showDialog = true">
          <el-icon><Plus /></el-icon>
          添加第一道错题
        </el-button>
    </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEditing ? '编辑错题' : '添加错题'"
      width="900px"
      :close-on-click-modal="false"
      class="error-dialog"
    >
      <!-- 录入模式切换 -->
      <div v-if="!isEditing" class="input-mode-tabs">
        <div 
          :class="['mode-tab', { active: inputMode === 'manual' }]"
          @click="switchInputMode('manual')"
        >
          <el-icon><Edit /></el-icon>
          <span>手动输入</span>
        </div>
        <div 
          :class="['mode-tab', { active: inputMode === 'photo' }]"
          @click="switchInputMode('photo')"
        >
          <el-icon><Camera /></el-icon>
          <span>📸 拍照识别</span>
        </div>
      </div>

      <!-- 拍照识别模式 -->
      <div v-if="!isEditing && inputMode === 'photo'" class="photo-recognition-section">
        <!-- 上传区域 -->
        <div v-if="!recognizedImage" class="upload-area" @click="triggerFileInput">
          <input 
            ref="fileInput" 
            type="file" 
            accept="image/*" 
            @change="handleImageUpload"
            style="display: none"
          />
          <div class="upload-icon">
            <el-icon><Camera /></el-icon>
          </div>
          <p class="upload-title">点击上传题目图片</p>
          <p class="upload-desc">支持 JPG、PNG、JPEG 格式</p>
          <p class="upload-hint">📸 AI将自动识别题目内容、公式、答案</p>
        </div>

        <!-- 图片预览 + 识别中 -->
        <div v-else class="image-preview-section">
          <div class="preview-header">
            <span>📷 已上传图片</span>
            <el-button link type="danger" @click="clearImage">
              <el-icon><Delete /></el-icon>
              重新上传
            </el-button>
          </div>
          <div class="image-preview">
            <img :src="recognizedImage" alt="题目图片" />
          </div>
          
          <!-- 识别状态 -->
          <div v-if="recognizing" class="recognizing-status">
            <el-icon class="rotating"><Loading /></el-icon>
            <span>AI正在识别中，请稍候...</span>
          </div>
          
          <!-- 识别成功提示 -->
          <div v-else-if="recognitionSuccess" class="recognition-success">
            <el-icon><CircleCheck /></el-icon>
            <span>✅ 识别完成！请检查并确认下方内容</span>
          </div>
        </div>
      </div>

      <el-form :model="form" label-position="top" class="error-form">
        <div class="form-row">
          <el-form-item label="学科" required class="form-col-2">
            <el-select v-model="form.subject" placeholder="选择学科" size="large">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
          </el-select>
        </el-form-item>
          <el-form-item label="难度" class="form-col-2">
            <el-select v-model="form.difficulty" placeholder="选择难度" size="large">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        </div>

        <el-form-item label="题目内容" required>
          <!-- 智能识别提示 - 实时版 -->
          <div v-if="inputMode === 'manual' && form.content.trim().length > 0" class="smart-parse-hint">
            <div class="auto-parse-status">
              <el-icon v-if="parsing" class="rotating"><Loading /></el-icon>
              <el-icon v-else-if="autoParseSuccess" class="success-icon"><CircleCheck /></el-icon>
              <el-icon v-else><MagicStick /></el-icon>
              <span v-if="parsing" class="status-text">🧠 AI正在智能识别中...</span>
              <span v-else-if="autoParseSuccess" class="status-text success">✅ 已自动识别并填充</span>
              <span v-else class="status-text">💡 实时智能识别：输入停止2秒后自动分析</span>
            </div>
            <el-button 
              v-if="!autoParseSuccess"
              link
              type="primary" 
              size="small" 
              :loading="parsing"
              @click="parseQuestionContent"
            >
              或点击立即识别
            </el-button>
          </div>

          <!-- 如果有识别的图片，显示在旁边 -->
          <div v-if="recognizedImage" class="content-with-image">
            <div class="content-input-wrapper">
          <el-input
                v-model="form.content"
                type="textarea"
                :rows="8"
                placeholder="请输入题目内容..."
                size="large"
              />
            </div>
            <div class="original-image-preview">
              <div class="preview-label">📷 原图参考</div>
              <img :src="recognizedImage" alt="题目原图" />
              <el-button link type="danger" size="small" @click="clearImage">
                <el-icon><Delete /></el-icon>
                移除原图
              </el-button>
            </div>
          </div>
          <el-input
            v-else
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请输入题目内容..."
            size="large"
          />
        </el-form-item>

        <el-form-item label="正确答案">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="3"
            placeholder="请输入正确答案..."
            size="large"
          />
        </el-form-item>

        <el-form-item label="题目解析">
          <el-input
            v-model="form.analysis"
            type="textarea"
            :rows="3"
            placeholder="请输入题目解析..."
            size="large"
          />
        </el-form-item>

        <el-form-item label="错因分析">
          <el-input
            v-model="form.errorReason"
            type="textarea"
            :rows="3"
            placeholder="分析一下为什么会做错..."
            size="large"
          />
        </el-form-item>

        <el-form-item label="订正内容">
          <el-input
            v-model="form.correction"
            type="textarea"
            :rows="3"
            placeholder="写下你的订正过程和思路..."
            size="large"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="输入标签，多个标签用逗号分隔，例如：重点,易错,计算"
            size="large"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="closeDialog">取消</el-button>
          <el-button type="primary" size="large" @click="submitForm" :loading="submitting">
            {{ isEditing ? '保存修改' : '添加错题' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 相似题练习对话框 -->
    <el-dialog 
      v-model="similarQuestionsDialog" 
      title="📝 相似题练习" 
      width="800px" 
      :close-on-click-modal="false"
      class="similar-questions-dialog"
    >
      <div v-if="similarQuestions.length > 0" class="practice-container">
        <!-- 进度指示器 -->
        <div class="progress-bar">
          <div 
            v-for="(q, index) in similarQuestions" 
            :key="index"
            :class="['progress-dot', {
              'active': index === currentQuestionIndex,
              'correct': questionResults[index]?.isCorrect === true,
              'wrong': questionResults[index]?.isCorrect === false
            }]"
            @click="currentQuestionIndex = index"
          >
            {{ index + 1 }}
          </div>
        </div>

        <!-- 当前题目 -->
        <div class="question-card">
          <div class="question-header">
            <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
            <el-tag :type="similarQuestions[currentQuestionIndex].type === '选择题' ? 'primary' : 'success'">
              {{ similarQuestions[currentQuestionIndex].type || '练习题' }}
            </el-tag>
          </div>
          
          <div class="question-content">
            {{ similarQuestions[currentQuestionIndex].question }}
          </div>

          <!-- 答题区域 -->
          <div class="answer-section">
            <div class="answer-label">你的答案：</div>
            
            <!-- 选择题：显示选项 -->
            <div v-if="similarQuestions[currentQuestionIndex].options && similarQuestions[currentQuestionIndex].options.length > 0" class="options-container">
              <div 
                v-for="(option, idx) in similarQuestions[currentQuestionIndex].options" 
                :key="idx"
                :class="['option-item', {
                  'selected': userAnswers[currentQuestionIndex] === option.label,
                  'correct': questionResults[currentQuestionIndex] && option.label === questionResults[currentQuestionIndex].correctAnswer,
                  'wrong': questionResults[currentQuestionIndex] && userAnswers[currentQuestionIndex] === option.label && !questionResults[currentQuestionIndex].isCorrect,
                  'disabled': questionResults[currentQuestionIndex] !== null
                }]"
                @click="questionResults[currentQuestionIndex] === null && (userAnswers[currentQuestionIndex] = option.label)"
              >
                <div class="option-label">{{ option.label }}</div>
                <div class="option-content">{{ option.text }}</div>
                <div v-if="questionResults[currentQuestionIndex] && option.label === questionResults[currentQuestionIndex].correctAnswer" class="correct-mark">✓</div>
                <div v-if="questionResults[currentQuestionIndex] && userAnswers[currentQuestionIndex] === option.label && !questionResults[currentQuestionIndex].isCorrect" class="wrong-mark">✗</div>
              </div>
            </div>
            
            <!-- 填空题/计算题：显示输入框 -->
            <el-input
              v-else
              v-model="userAnswers[currentQuestionIndex]"
              placeholder="请输入答案..."
              size="large"
              :disabled="questionResults[currentQuestionIndex] !== null"
            />
            
            <el-button 
              v-if="questionResults[currentQuestionIndex] === null"
              type="primary" 
              @click="submitCurrentAnswer"
              class="submit-btn"
              :disabled="!userAnswers[currentQuestionIndex]"
            >
              提交答案
            </el-button>
          </div>

          <!-- 结果显示 -->
          <div v-if="questionResults[currentQuestionIndex]" class="result-section">
            <div :class="['result-badge', questionResults[currentQuestionIndex].isCorrect ? 'correct' : 'wrong']">
              {{ questionResults[currentQuestionIndex].isCorrect ? '✅ 回答正确' : '❌ 回答错误' }}
            </div>
            
            <div class="answer-compare">
              <div class="answer-item">
                <span class="label">你的答案：</span>
                <span :class="questionResults[currentQuestionIndex].isCorrect ? 'correct-text' : 'wrong-text'">
                  {{ questionResults[currentQuestionIndex].userAnswer }}
                </span>
              </div>
              <div class="answer-item">
                <span class="label">正确答案：</span>
                <span class="correct-text">{{ questionResults[currentQuestionIndex].correctAnswer }}</span>
              </div>
            </div>

            <div class="analysis-section">
              <div class="analysis-title">📖 题目解析</div>
              <div class="analysis-content" v-html="formatAIResponse(questionResults[currentQuestionIndex].analysis)"></div>
            </div>
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="navigation-buttons">
          <el-button 
            @click="prevQuestion" 
            :disabled="currentQuestionIndex === 0"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一题
          </el-button>
          
          <el-button 
            v-if="currentQuestionIndex < similarQuestions.length - 1"
            type="primary"
            @click="nextQuestion"
          >
            下一题
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          
          <el-button 
            v-else
            type="success"
            @click="showPracticeSummary"
          >
            <el-icon><CircleCheck /></el-icon>
            完成练习
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="closeSimilarDialog">关闭</el-button>
      </template>
    </el-dialog>

    <!-- AI问答对话框 -->
    <el-dialog v-model="aiQuestionDialog" title="🤖 AI学习助手" width="650px" class="ai-chat-dialog">
      <!-- 题目信息卡片 -->
      <div v-if="currentError" class="question-context-card">
        <div class="context-header">
          <el-icon><QuestionFilled /></el-icon>
          <span>关于这道题，你可以问我：</span>
        </div>
        <div class="quick-questions">
          <el-tag @click="quickAsk('这道题怎么做？')" class="quick-tag">这道题怎么做？</el-tag>
          <el-tag @click="quickAsk('为什么我的答案是错的？')" class="quick-tag">为什么我的答案是错的？</el-tag>
          <el-tag @click="quickAsk('有没有更简单的方法？')" class="quick-tag">有没有更简单的方法？</el-tag>
          <el-tag @click="quickAsk('有类似的题目吗？')" class="quick-tag">有类似的题目吗？</el-tag>
        </div>
      </div>

      <!-- 对话消息区域 -->
      <div class="ai-chat-messages" ref="chatMessagesRef">
        <div v-if="aiMessages.length === 0" class="empty-chat">
          <el-icon class="empty-icon"><ChatDotRound /></el-icon>
          <p>点击上方快捷问题，或在下方输入你的问题</p>
        </div>
        <div v-for="(msg, idx) in aiMessages" :key="idx" :class="['ai-message', msg.role]">
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-bubble">
            <div v-html="formatAIResponse(msg.content)"></div>
          </div>
        </div>
        <div v-if="asking" class="ai-message ai">
          <div class="message-avatar">🤖</div>
          <div class="message-bubble typing">
            <span></span><span></span><span></span>
      </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <template #footer>
        <div class="chat-input-wrapper">
        <el-input
          v-model="aiQuestion"
          placeholder="有什么问题问我吧..."
          @keyup.enter="sendAIQuestion"
          :disabled="asking"
            type="textarea"
            :rows="2"
            resize="none"
          />
          <el-button 
            type="primary" 
            @click="sendAIQuestion" 
            :loading="asking"
            :disabled="!aiQuestion.trim()"
            class="send-btn"
          >
            <el-icon v-if="!asking"><Promotion /></el-icon>
            {{ asking ? '思考中...' : '发送' }}
            </el-button>
        </div>
          </template>
    </el-dialog>

    <!-- 知识图谱对话框 -->
    <el-dialog 
      v-model="knowledgeMapDialog" 
      title="📚 知识图谱" 
      width="1200px" 
      class="knowledge-map-dialog"
      :close-on-click-modal="false"
    >
      <div v-if="loadingKnowledge" class="loading-container">
        <el-icon class="is-loading" :size="50"><Loading /></el-icon>
        <p>AI正在分析知识点...</p>
      </div>

      <div v-else-if="knowledgeMap" class="knowledge-map-container">
        <!-- 题目信息 -->
        <el-card class="question-info-card" shadow="never">
          <div class="info-title">
            <el-icon><QuestionFilled /></el-icon>
            <span>题目内容</span>
          </div>
          <div class="info-content">{{ currentError?.question?.content }}</div>
        </el-card>

        <!-- 使用说明 -->
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #title>
            <div style="font-size: 13px;">
              💡 <strong>提示：</strong>点击任意知识点节点，可以查看相关练习题
            </div>
          </template>
        </el-alert>

        <!-- 知识图谱树状图 -->
        <div class="knowledge-tree-chart">
          <div 
            ref="knowledgeChartDom"
            style="height: 800px; width: 100%;"
          ></div>
        </div>

        <!-- 旧的卡片展示（备用） -->
        <div class="knowledge-graph" style="display: none;">
          <!-- 前置知识点 -->
          <div v-if="knowledgeMap.prerequisites && knowledgeMap.prerequisites.length > 0" class="knowledge-section">
            <div class="section-header prerequisite">
              <el-icon><Top /></el-icon>
              <h3>前置知识点</h3>
              <span class="section-desc">需要先掌握的基础知识</span>
            </div>
            <div class="knowledge-cards">
              <div 
                v-for="(point, index) in knowledgeMap.prerequisites" 
                :key="'pre-' + index"
                class="knowledge-card prerequisite"
              >
                <div class="card-number">{{ index + 1 }}</div>
                <div class="card-content">
                  <div class="card-title">{{ point.name }}</div>
                  <div class="card-desc">{{ point.description }}</div>
                </div>
              </div>
            </div>
            <div class="flow-arrow">↓</div>
          </div>

          <!-- 当前知识点 -->
          <div class="knowledge-section">
            <div class="section-header current">
              <el-icon><Collection /></el-icon>
              <h3>当前考察知识点</h3>
              <span class="section-desc">这道题主要考察的内容</span>
            </div>
            <div class="knowledge-cards">
              <div 
                v-for="(point, index) in knowledgeMap.current" 
                :key="'cur-' + index"
                class="knowledge-card current"
              >
                <div class="card-number">⭐</div>
                <div class="card-content">
                  <div class="card-title">{{ point.name }}</div>
                  <div class="card-desc">{{ point.description }}</div>
                  <div v-if="point.difficulty" class="card-difficulty">
                    <el-tag :type="getDifficultyTagType(point.difficulty)" size="small">
                      {{ point.difficulty }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
            <div class="flow-arrow">↓</div>
          </div>

          <!-- 后置知识点 -->
          <div v-if="knowledgeMap.advanced && knowledgeMap.advanced.length > 0" class="knowledge-section">
            <div class="section-header advanced">
              <el-icon><Bottom /></el-icon>
              <h3>后续拓展知识点</h3>
              <span class="section-desc">掌握后可以学习的进阶内容</span>
            </div>
            <div class="knowledge-cards">
              <div 
                v-for="(point, index) in knowledgeMap.advanced" 
                :key="'adv-' + index"
                class="knowledge-card advanced"
              >
                <div class="card-number">{{ index + 1 }}</div>
                <div class="card-content">
                  <div class="card-title">{{ point.name }}</div>
                  <div class="card-desc">{{ point.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 学习建议 -->
        <el-card v-if="knowledgeMap.suggestions" class="suggestions-card" shadow="never">
          <div class="suggestions-header">
            <el-icon><MagicStick /></el-icon>
            <h4>AI学习建议</h4>
          </div>
          <div class="suggestions-content">{{ knowledgeMap.suggestions }}</div>
        </el-card>
      </div>

      <template #footer>
        <el-button @click="knowledgeMapDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 推荐题目对话框 - 交互式练习 -->
    <el-dialog
      v-model="recommendedQuestionsDialog"
      :title="`📚 ${selectedKnowledgePoint} - 相关练习题`"
      width="900px"
      class="recommended-questions-dialog"
      :close-on-click-modal="false"
    >
      <div v-if="loadingRecommend" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>AI正在推荐题目...</p>
  </div>

      <div v-else-if="recommendedQuestions.length > 0" class="practice-container">
        <!-- 进度条 -->
        <div class="progress-bar">
          <div class="progress-info">
            <span class="progress-text">练习进度</span>
            <span class="progress-count">{{ currentRecommendIndex + 1 }} / {{ recommendedQuestions.length }}</span>
          </div>
          <el-progress
            :percentage="((currentRecommendIndex + 1) / recommendedQuestions.length) * 100"
            :color="'#667eea'"
            :stroke-width="8"
          />
        </div>

        <!-- 当前题目 -->
        <div class="current-question-card">
          <div class="question-header">
            <div class="question-number">第 {{ currentRecommendIndex + 1 }} 题</div>
            <div class="question-tags">
              <el-tag :type="getDifficultyTagType(recommendedQuestions[currentRecommendIndex].difficulty)" size="small">
                {{ recommendedQuestions[currentRecommendIndex].difficulty || '中等' }}
              </el-tag>
              <el-tag type="info" size="small" style="margin-left: 8px;">
                {{ recommendedQuestions[currentRecommendIndex].type || '填空题' }}
              </el-tag>
            </div>
          </div>

          <div class="question-content">
            {{ recommendedQuestions[currentRecommendIndex].question }}
          </div>

          <!-- 答题区域 -->
          <div class="answer-section">
            <div class="answer-label">你的答案：</div>
            
            <!-- 选择题：显示选项 -->
            <div v-if="recommendedQuestions[currentRecommendIndex].options && recommendedQuestions[currentRecommendIndex].options.length > 0" class="options-container">
              <div 
                v-for="(option, idx) in recommendedQuestions[currentRecommendIndex].options" 
                :key="idx"
                :class="['option-item', {
                  'selected': recommendAnswers[currentRecommendIndex] === option.label,
                  'correct': recommendResults[currentRecommendIndex] && option.label === recommendResults[currentRecommendIndex].correctAnswer,
                  'wrong': recommendResults[currentRecommendIndex] && recommendAnswers[currentRecommendIndex] === option.label && !recommendResults[currentRecommendIndex].isCorrect,
                  'disabled': recommendResults[currentRecommendIndex] !== null
                }]"
                @click="recommendResults[currentRecommendIndex] === null && (recommendAnswers[currentRecommendIndex] = option.label)"
              >
                <div class="option-label">{{ option.label }}</div>
                <div class="option-content">{{ option.text }}</div>
                <div v-if="recommendResults[currentRecommendIndex] && option.label === recommendResults[currentRecommendIndex].correctAnswer" class="correct-mark">✓</div>
                <div v-if="recommendResults[currentRecommendIndex] && recommendAnswers[currentRecommendIndex] === option.label && !recommendResults[currentRecommendIndex].isCorrect" class="wrong-mark">✗</div>
              </div>
            </div>
            
            <!-- 填空题/计算题：显示输入框 -->
            <el-input
              v-else
              v-model="recommendAnswers[currentRecommendIndex]"
              placeholder="请输入答案..."
              size="large"
              :disabled="recommendResults[currentRecommendIndex] !== null"
            />
            
            <el-button 
              v-if="recommendResults[currentRecommendIndex] === null"
              type="primary" 
              @click="submitRecommendAnswer"
              class="submit-btn"
              :disabled="!recommendAnswers[currentRecommendIndex]"
            >
              提交答案
            </el-button>
          </div>

          <!-- 结果显示 -->
          <div v-if="recommendResults[currentRecommendIndex]" class="result-section">
            <div :class="['result-badge', recommendResults[currentRecommendIndex].isCorrect ? 'correct' : 'wrong']">
              {{ recommendResults[currentRecommendIndex].isCorrect ? '✅ 回答正确' : '❌ 回答错误' }}
            </div>
            
            <div class="answer-comparison">
              <div class="answer-row">
                <span class="label">你的答案：</span>
                <span :class="['value', recommendResults[currentRecommendIndex].isCorrect ? 'correct-text' : 'wrong-text']">
                  {{ recommendResults[currentRecommendIndex].userAnswer }}
                </span>
              </div>
              <div class="answer-row">
                <span class="label">正确答案：</span>
                <span class="value correct-text">
                  {{ recommendResults[currentRecommendIndex].correctAnswer }}
                </span>
              </div>
            </div>

            <div class="analysis-section">
              <div class="analysis-header">
                <el-icon><Document /></el-icon>
                <span>详细解析</span>
              </div>
              <div class="analysis-content">
                {{ recommendResults[currentRecommendIndex].analysis }}
              </div>
            </div>
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="navigation-buttons">
          <el-button 
            @click="prevRecommendQuestion" 
            :disabled="currentRecommendIndex === 0"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一题
          </el-button>
          
          <el-button 
            v-if="currentRecommendIndex < recommendedQuestions.length - 1"
            type="primary"
            @click="nextRecommendQuestion"
          >
            下一题
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          
          <el-button 
            v-else
            type="success"
            @click="showRecommendSummary"
          >
            <el-icon><CircleCheck /></el-icon>
            完成练习
          </el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeRecommendDialog">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 分享到共享池对话框 -->
    <el-dialog
      v-model="shareDialog"
      title="🌍 分享到共享池"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="share-form">
        <div class="share-preview">
          <div class="preview-label">📝 分享的错题</div>
          <div class="preview-content">
            <div class="preview-subject">{{ currentShareError?.question?.subject }}</div>
            <div class="preview-question">{{ currentShareError?.question?.content?.substring(0, 100) }}...</div>
          </div>
        </div>

        <el-form :model="shareForm" label-position="top" class="share-form-content">
          <el-form-item label="分享范围" required>
            <el-radio-group v-model="shareForm.scope">
              <el-radio value="班级">仅限本班级</el-radio>
              <el-radio value="年级">年级范围</el-radio>
              <el-radio value="全校">全校范围</el-radio>
            </el-radio-group>
            <div class="form-hint">💡 选择分享范围，让更多同学看到你的错题和收获</div>
          </el-form-item>

          <el-form-item label="分享标签（可选）">
            <el-input
              v-model="shareForm.tags"
              placeholder="如：易错题、重点题、经典题等，用逗号分隔"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <div class="share-notice">
            <el-icon><InfoFilled /></el-icon>
            <div>
              <div class="notice-title">📢 温馨提示</div>
              <ul>
                <li>分享后需要教师审核才会显示在共享池</li>
                <li>分享的错题将帮助其他同学学习</li>
                <li>你会获得学习积分和贡献值</li>
              </ul>
            </div>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shareDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmShare" :loading="sharing">
            <el-icon v-if="!sharing"><Share /></el-icon>
            {{ sharing ? '分享中...' : '确认分享' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 教师反馈对话框 -->
    <el-dialog
      v-model="teacherFeedbackDialog"
      title="👨‍🏫 教师反馈"
      width="700px"
    >
      <div v-if="loadingFeedback" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="teacherFeedbacks.length > 0" class="feedback-list">
        <div 
          v-for="(feedback, index) in teacherFeedbacks" 
          :key="index"
          class="feedback-item"
        >
          <div class="feedback-header">
            <div class="teacher-avatar">👨‍🏫</div>
            <div class="feedback-meta">
              <div class="teacher-name">{{ feedback.teacherName }}</div>
              <div class="feedback-time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(feedback.createdAt) }}
              </div>
            </div>
            <div v-if="feedback.rating" class="feedback-rating">
              <el-rate v-model="feedback.rating" disabled show-score />
            </div>
          </div>
          
          <div class="feedback-content">
            {{ feedback.feedback }}
          </div>
        </div>
      </div>

      <el-empty 
        v-else 
        description="暂无教师反馈"
        :image-size="150"
      />

      <template #footer>
        <el-button @click="teacherFeedbackDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { 
  Notebook, Plus, Document, CircleCheck, Share, TrendCharts, Search,
  QuestionFilled, Check, Warning, EditPen, Clock, Edit, Delete,
  DocumentDelete, MagicStick, ChatDotRound, Connection, Promotion,
  Camera, Loading, ArrowLeft, ArrowRight, Collection, Top, Bottom,
  InfoFilled
} from '@element-plus/icons-vue'
import api from '@/lib/api'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { TreeChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { renderMath } from '@/utils/mathRenderer'

// 注册 ECharts 组件
echarts.use([TreeChart, CanvasRenderer, TitleComponent, TooltipComponent, LegendComponent])

// 渲染包含数学公式的内容
const renderContent = (content: string) => {
  return renderMath(content || '')
}

// 状态
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const isEditing = ref(false)
const currentError = ref<any>(null)
const errorBooks = ref<any[]>([])
const filterSubject = ref('')
const searchKeyword = ref('')

// AI相关状态
const aiQuestionDialog = ref(false)
const aiQuestion = ref('')
const aiMessages = ref<any[]>([])
const asking = ref(false)
const chatMessagesRef = ref<HTMLElement | null>(null)

// 相似题推荐状态
const similarQuestionsDialog = ref(false)
const similarQuestions = ref<any[]>([])
const currentQuestionIndex = ref(0)
const userAnswers = ref<string[]>([])
const questionResults = ref<any[]>([])
const loadingSimilar = ref(false)

// 知识图谱状态
const knowledgeMapDialog = ref(false)
const knowledgeMap = ref<any>(null)
const loadingKnowledge = ref(false)
const knowledgeChartDom = ref<HTMLElement>()
let knowledgeChartInstance: any = null

// 推荐题目对话框
const recommendedQuestionsDialog = ref(false)
const recommendedQuestions = ref<any[]>([])
const selectedKnowledgePoint = ref('')
const loadingRecommend = ref(false)
const currentRecommendIndex = ref(0)
const recommendAnswers = ref<string[]>([])
const recommendResults = ref<any[]>([])

// 教师反馈相关状态
const teacherFeedbackDialog = ref(false)
const teacherFeedbacks = ref<any[]>([])
const loadingFeedback = ref(false)

// 分享到共享池相关状态
const shareDialog = ref(false)
const currentShareError = ref<any>(null)
const sharing = ref(false)
const shareForm = ref({
  scope: '班级',
  tags: ''
})

// 拍照识别相关状态
const inputMode = ref('manual') // 'manual' 或 'photo'
const fileInput = ref<HTMLInputElement | null>(null)
const recognizedImage = ref('')
const recognizedImageFile = ref<File | null>(null) // 保存原始文件
const recognizing = ref(false)
const recognitionSuccess = ref(false)

// 智能解析相关状态
const parsing = ref(false)
const autoParseSuccess = ref(false)
let autoParseTimer: ReturnType<typeof setTimeout> | null = null

// 表单
const form = ref({
  subject: '',
  difficulty: '',
  content: '',
  answer: '',
  analysis: '',
  errorReason: '',
  correction: '',
  tags: ''
})

// 监听题目内容变化，实现自动识别
watch(() => form.value.content, (newContent, oldContent) => {
  // 清除之前的定时器
  if (autoParseTimer) {
    clearTimeout(autoParseTimer)
    autoParseTimer = null
  }
  
  // 重置成功状态
  autoParseSuccess.value = false
  
  // 如果内容为空或太短，不触发
  if (!newContent || newContent.trim().length < 10) {
    return
  }
  
  // 如果是手动输入模式且内容发生变化，设置2秒后自动识别
  if (inputMode.value === 'manual' && newContent !== oldContent) {
    console.log('💡 检测到内容变化，2秒后自动识别...')
    autoParseTimer = setTimeout(() => {
      console.log('🚀 自动触发智能识别')
      parseQuestionContent(true) // true表示是自动触发
    }, 2000) // 2秒延迟
  }
})

// 计算属性
const correctedCount = computed(() => {
  return errorBooks.value.filter(e => e.correction).length
})

const sharedCount = computed(() => {
  return errorBooks.value.filter(e => e.status === 'SHARED').length
})

const correctionRate = computed(() => {
  if (errorBooks.value.length === 0) return 0
  return Math.round((correctedCount.value / errorBooks.value.length) * 100)
})

const filteredErrorBooks = computed(() => {
  let result = errorBooks.value
  if (filterSubject.value) {
    result = result.filter(e => e.question.subject === filterSubject.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(e =>
      e.question.content.toLowerCase().includes(keyword) ||
      (e.errorReason && e.errorReason.toLowerCase().includes(keyword))
    )
  }
  return result
})

// 方法
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getSubjectClass = (subject: string) => {
  const map: any = {
    '数学': 'math',
    '语文': 'chinese',
    '英语': 'english',
    '物理': 'physics',
    '化学': 'chemistry',
    '生物': 'biology'
  }
  return map[subject] || 'default'
}

const getDifficultyClass = (difficulty: string) => {
  const map: any = {
    '简单': 'easy',
    '中等': 'medium',
    '困难': 'hard'
  }
  return map[difficulty] || 'medium'
}

const fetchErrorBooks = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filterSubject.value) params.subject = filterSubject.value
    const res = await api.get('/api/errorbook', { params })
    errorBooks.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载错题失败')
  } finally {
    loading.value = false
  }
}

const editError = (error: any) => {
  isEditing.value = true
  currentError.value = error
  form.value = {
    subject: error.question.subject,
    difficulty: error.question.difficulty || '',
    content: error.question.content,
    answer: error.question.answer || '',
    analysis: error.question.analysis || '',
    errorReason: error.errorReason || '',
    correction: error.correction || '',
    tags: error.tags || ''
  }
  showDialog.value = true
}

const deleteError = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这道错题吗？', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.delete(`/api/errorbook/${id}`)
    ElMessage.success('删除成功')
    fetchErrorBooks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const submitForm = async () => {
  if (!form.value.subject || !form.value.content) {
    ElMessage.warning('请填写学科和题目内容')
    return
  }

  submitting.value = true
  try {
    if (isEditing.value && currentError.value) {
      await api.put(`/api/errorbook/${currentError.value.id}`, {
        errorReason: form.value.errorReason,
        correction: form.value.correction,
        tags: form.value.tags
      })
      ElMessage.success('更新成功')
      
      // 关闭对话框
      closeDialog()
      
      // 刷新列表
      console.log('🔄 更新后重新加载错题列表...')
      await fetchErrorBooks()
      console.log('✅ 错题列表已刷新')
    } else {
      // 如果有识别的图片，先上传图片
      let imageUrl = ''
      if (recognizedImageFile.value) {
        try {
          console.log('📤 上传题目原图...')
          const imageFormData = new FormData()
          imageFormData.append('file', recognizedImageFile.value)
          
          const uploadRes = await api.post('/api/file/upload', imageFormData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
          
          if (uploadRes.data.success) {
            imageUrl = uploadRes.data.data
            console.log('✅ 图片上传成功:', imageUrl)
          }
        } catch (uploadError) {
          console.error('图片上传失败:', uploadError)
          // 上传失败不影响提交，继续
        }
      }
      
      // 提交错题数据（包含图片URL）
      const errorData = {
        ...form.value,
        images: imageUrl ? JSON.stringify([imageUrl]) : undefined // 后端期望的是JSON数组格式
      }
      
      console.log('📤 提交错题数据:', errorData)
      
      const response = await api.post('/api/errorbook', errorData)
      console.log('✅ 添加成功，返回数据:', response.data)
      
      ElMessage.success('添加成功' + (imageUrl ? '（含原图）' : ''))
      
      // 关闭对话框
      closeDialog()
      
      // 重新加载错题列表
      console.log('🔄 重新加载错题列表...')
      await fetchErrorBooks()
      console.log('✅ 错题列表已刷新，当前数量:', errorBooks.value.length)
    }
  } catch (error: any) {
    console.error('❌ 添加错题失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    ElMessage.error('操作失败: ' + errorMsg)
  } finally {
    submitting.value = false
  }
}

const closeDialog = () => {
  showDialog.value = false
  isEditing.value = false
  currentError.value = null
  inputMode.value = 'manual'
  recognizedImage.value = ''
  recognizedImageFile.value = null
  recognizing.value = false
  recognitionSuccess.value = false
  parsing.value = false
  autoParseSuccess.value = false
  
  // 清除自动识别定时器
  if (autoParseTimer) {
    clearTimeout(autoParseTimer)
    autoParseTimer = null
  }
  
  form.value = {
    subject: '',
    difficulty: '',
    content: '',
    answer: '',
    analysis: '',
    errorReason: '',
    correction: '',
    tags: ''
  }
}

// ============ 拍照识别功能 ============

// 切换录入模式
const switchInputMode = (mode: 'manual' | 'photo') => {
  inputMode.value = mode
  if (mode === 'photo') {
    // 清空表单，准备接收识别结果
    form.value.content = ''
    form.value.answer = ''
    form.value.analysis = ''
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理图片上传
const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件！')
    return
  }
  
  // 检查文件大小（限制5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB！')
    return
  }
  
  // 保存原始文件
  recognizedImageFile.value = file
  
  // 显示图片预览
  const reader = new FileReader()
  reader.onload = (e) => {
    recognizedImage.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  
  // 开始识别
  await recognizeQuestion(file)
}

// 清除图片
const clearImage = () => {
  recognizedImage.value = ''
  recognizedImageFile.value = null
  recognizing.value = false
  recognitionSuccess.value = false
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// ============ 智能解析功能 ============

// 智能解析题目内容
const parseQuestionContent = async (isAuto = false) => {
  if (!form.value.content || form.value.content.trim().length === 0) {
    if (!isAuto) {
      ElMessage.warning('请先输入题目内容')
    }
    return
  }

  parsing.value = true
  try {
    if (!isAuto) {
      ElMessage.info('🧠 AI正在智能识别题目信息...')
    } else {
      console.log('🤖 自动识别启动（静默模式）')
    }
    
    const res = await api.post('/api/errorbook/parse-question', {
      content: form.value.content
    })
    
    console.log('📡 收到后端响应:', res.data)
    
    // 修复：后端返回的是 {success, message, data}，不是 {code}
    if (res.data.success && res.data.data) {
      const result = res.data.data
      
      console.log('📋 解析结果:', result)
      console.log('📝 当前表单值:', form.value)
      
      if (result.success) {
        // 自动填充识别结果（强制覆盖，不管是否已有值）
        if (result.subject) {
          console.log('✏️ 设置学科:', result.subject)
          form.value.subject = result.subject
        }
        if (result.difficulty) {
          console.log('✏️ 设置难度:', result.difficulty)
          form.value.difficulty = result.difficulty
        }
        if (result.answer) {
          console.log('✏️ 设置答案:', result.answer)
          form.value.answer = result.answer
        }
        if (result.analysis) {
          console.log('✏️ 设置解析:', result.analysis)
          form.value.analysis = result.analysis
        }
        
        console.log('✅ 填充后的表单值:', form.value)
        
        // 合并标签
        if (result.tags && result.tags.length > 0) {
          const existingTags = form.value.tags ? form.value.tags.split(',').map(t => t.trim()) : []
          const newTags = result.tags.filter((tag: string) => !existingTags.includes(tag))
          if (newTags.length > 0) {
            form.value.tags = [...existingTags, ...newTags].filter(t => t).join(', ')
          }
        }
        
        // 显示识别结果摘要
        let summary = `✅ 识别成功！表单已自动填充\n\n`
        summary += `• 学科: ${result.subject || '未识别'}\n`
        summary += `• 难度: ${result.difficulty || '未识别'}\n`
        summary += `• 题型: ${result.questionType || '未识别'}\n`
        if (result.answer) {
          summary += `• 答案: ${result.answer.substring(0, 30)}${result.answer.length > 30 ? '...' : ''}\n`
        }
        if (result.knowledgePoints && result.knowledgePoints.length > 0) {
          summary += `• 知识点: ${result.knowledgePoints.join(', ')}\n`
        }
        if (result.confidence) {
          summary += `• 置信度: ${result.confidence}`
        }
        
        // 标记自动识别成功
        autoParseSuccess.value = true
        
        // 使用更醒目的提示（仅在手动触发时显示消息条）
        if (!isAuto) {
          // 手动触发时显示完整提示
          ElMessage.success({
            message: '🎉 智能识别完成！学科、难度、答案、解析已自动填充',
            duration: 3000,
            showClose: true
          })
        }
        // 自动触发时静默填充，不显示任何弹窗
        
        console.log('📋 智能识别结果:', result)
      } else {
        ElMessage.warning(result.errorMessage || '识别失败')
      }
    } else {
      ElMessage.error(res.data.message || '识别失败')
    }
  } catch (error: any) {
    console.error('智能识别失败:', error)
    ElMessage.error('智能识别失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    parsing.value = false
  }
}

// 调用AI识别题目
const recognizeQuestion = async (file: File) => {
  recognizing.value = true
  recognitionSuccess.value = false
  
  try {
    ElMessage.info('🤖 AI正在识别题目，请稍候...')
    
    // 创建FormData
    const formData = new FormData()
    formData.append('file', file)
    formData.append('subject', '通用')
    
    // 调用GLM-4V视觉识别API
    const res = await api.post('/api/ai/free-photo-search', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (res.data.success) {
      const result = res.data.data
      
      console.log('📦 完整的GLM-4V返回结果:', result)
      console.log('📦 结果类型:', typeof result)
      console.log('📦 结果的keys:', Object.keys(result))
      
      // 第一步：基础解析 - 提取题目文本（尝试多种可能的字段名）
      const questionText = result.questionText || result.text || result.question || result.content || ''
      const solutionText = result.solution || result.answer || result.explanation || ''
      
      console.log('📝 提取的题目文本:', questionText)
      console.log('📝 题目文本长度:', questionText?.length)
      console.log('📝 提取的解答文本:', solutionText)
      console.log('📝 解答文本长度:', solutionText?.length)
      
      // 如果识别内容为空，直接使用基础模式
      if (!questionText || questionText.trim().length < 10) {
        console.error('❌ 识别内容太少或为空!')
        console.log('尝试直接使用整个result对象:', JSON.stringify(result, null, 2))
        
        // 可能整个result就是文本
        if (typeof result === 'string' && result.length > 10) {
          console.log('🔄 result是字符串，直接使用')
          form.value.content = result
          form.value.subject = detectSubject(result) || '数学'
          form.value.difficulty = detectDifficulty(result) || '中等'
          recognitionSuccess.value = true
          ElMessage.success('✅ 识别完成！请检查内容')
          return
        }
        
        ElMessage.error('识别内容为空，请重新拍照或手动输入')
        return
      }
      
      // 第二步：使用NLP智能解析 - 调用GLM提取结构化信息
      ElMessage.info('🧠 AI正在智能解析题目结构...')
      await parseQuestionWithNLP(questionText, solutionText)
      
      recognitionSuccess.value = true
      ElMessage.success('✅ 识别完成！请检查内容')
    } else {
      console.error('❌ API返回失败:', res.data)
      ElMessage.error(res.data.message || '识别失败，请重试')
    }
  } catch (error: any) {
    console.error('识别失败:', error)
    ElMessage.error(error.response?.data?.message || '识别失败，请检查网络或重试')
  } finally {
    recognizing.value = false
  }
}

// 使用NLP智能解析题目
const parseQuestionWithNLP = async (questionText: string, solutionText: string) => {
  try {
    console.log('🧠 开始NLP智能解析...')
    console.log('题目文本:', questionText)
    console.log('解答文本:', solutionText)
    
    // 如果识别内容为空，使用基础解析
    if (!questionText || questionText.trim().length < 5) {
      console.warn('⚠️ 识别内容太少，使用基础解析')
      parseRecognitionResultBasic(questionText, solutionText)
      return
    }
    
    // 调用GLM进行智能分析
    console.log('📡 开始调用GLM进行智能分析...')
    const res = await api.post('/api/ai/ask', {
      subject: '通用',
      question: `请仔细分析以下题目，提取关键信息，并严格按照JSON格式返回（只返回JSON，不要任何其他文字）：

【题目内容】
${questionText}

${solutionText ? `【参考解答】\n${solutionText}` : ''}

请返回纯JSON格式（不要markdown代码块）：
{
  "subject": "数学或语文或英语或物理或化学或生物",
  "difficulty": "简单或中等或困难",
  "questionContent": "${questionText.substring(0, 50)}...",
  "correctAnswer": "从题目或选项中判断的正确答案",
  "analysis": "简要说明这是什么类型的题目和解题思路",
  "tags": "主要知识点1,知识点2,知识点3"
}

注意：
1. 只返回JSON，不要其他任何文字
2. 所有字段都必须填写
3. subject必须是：数学/语文/英语/物理/化学/生物之一
4. difficulty必须是：简单/中等/困难之一`,
      context: ''
    })
    
    console.log('📥 收到GLM响应:', res.data)
    
    if (res.data.success) {
      const aiResponse = res.data.data
      console.log('🤖 GLM分析结果:', aiResponse)
      console.log('响应类型:', typeof aiResponse)
      console.log('响应长度:', aiResponse?.length)
      
      // 尝试解析JSON
      let parsed = null
      try {
        // 移除可能的markdown代码块标记和前后空白
        let jsonStr = aiResponse.trim()
        
        // 移除各种可能的代码块标记
        jsonStr = jsonStr.replace(/^```json\s*/gi, '')
        jsonStr = jsonStr.replace(/^```\s*/gi, '')
        jsonStr = jsonStr.replace(/\s*```$/gi, '')
        
        // 尝试提取JSON对象
        const jsonMatch = jsonStr.match(/\{[\s\S]*\}/)
        if (jsonMatch) {
          jsonStr = jsonMatch[0]
        }
        
        console.log('📝 准备解析的JSON字符串:', jsonStr.substring(0, 200))
        parsed = JSON.parse(jsonStr)
        console.log('✅ JSON解析成功:', parsed)
      } catch (e) {
        console.error('❌ JSON解析失败:', e)
        console.log('原始响应内容:', aiResponse)
        console.warn('⚠️ 尝试正则提取...')
        // 如果JSON解析失败，使用智能提取
        parsed = extractFromText(aiResponse, questionText, solutionText)
        console.log('正则提取结果:', parsed)
      }
      
      // 填充表单
      if (parsed && Object.keys(parsed).length > 0) {
        console.log('🔄 开始填充表单...')
        
        if (parsed.subject) {
          console.log('  - 学科:', parsed.subject)
          form.value.subject = parsed.subject
        }
        if (parsed.difficulty) {
          console.log('  - 难度:', parsed.difficulty)
          form.value.difficulty = parsed.difficulty
        }
        if (parsed.questionContent) {
          console.log('  - 题目内容长度:', parsed.questionContent.length)
          form.value.content = parsed.questionContent
        }
        if (parsed.correctAnswer) {
          console.log('  - 答案:', parsed.correctAnswer)
          form.value.answer = parsed.correctAnswer
        }
        if (parsed.analysis) {
          console.log('  - 解析长度:', parsed.analysis.length)
          form.value.analysis = parsed.analysis
        }
        if (parsed.tags) {
          console.log('  - 标签:', parsed.tags)
          form.value.tags = parsed.tags
        }
        
        console.log('✅ 表单填充完成!')
        console.log('当前表单状态:', JSON.stringify(form.value, null, 2))
        ElMessage.success('✅ 智能解析完成！已自动填充字段')
      } else {
        console.error('❌ 解析结果为空，使用基础解析')
        parseRecognitionResultBasic(questionText, solutionText)
      }
    } else {
      console.error('❌ API调用失败:', res.data)
      parseRecognitionResultBasic(questionText, solutionText)
    }
  } catch (error: any) {
    console.error('❌ NLP解析失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    // 如果NLP解析失败，使用基础解析作为后备
    console.log('🔄 切换到基础解析模式...')
    parseRecognitionResultBasic(questionText, solutionText)
    ElMessage.warning('AI解析失败，使用基础模式填充表单')
  }
}

// 从AI文本响应中智能提取信息（后备方案）
const extractFromText = (aiText: string, questionText: string, solutionText: string): any => {
  const result: any = {}
  
  // 提取学科
  const subjectMatch = aiText.match(/学科[：:]\s*["']?([^"'\n,]+)["']?/i)
  if (subjectMatch) result.subject = subjectMatch[1].trim()
  
  // 提取难度
  const difficultyMatch = aiText.match(/难度[：:]\s*["']?([^"'\n,]+)["']?/i)
  if (difficultyMatch) result.difficulty = difficultyMatch[1].trim()
  
  // 提取答案
  const answerMatch = aiText.match(/(?:正确)?答案[：:]\s*["']?([^"'\n}]+)["']?/i)
  if (answerMatch) result.correctAnswer = answerMatch[1].trim()
  
  // 提取解析
  const analysisMatch = aiText.match(/(?:题目)?解析[：:]\s*["']?([^"'}]+)["']?/i)
  if (analysisMatch) result.analysis = analysisMatch[1].trim()
  
  // 提取标签
  const tagsMatch = aiText.match(/(?:知识点)?标签[：:]\s*["']?([^"'\n}]+)["']?/i)
  if (tagsMatch) result.tags = tagsMatch[1].trim()
  
  // 使用原始内容作为题目
  result.questionContent = questionText
  
  return result
}

// 基础解析（后备方案）
const parseRecognitionResultBasic = (questionText: string, solutionText: string) => {
  console.log('📝 使用基础解析...')
  console.log('题目文本:', questionText)
  console.log('解答文本:', solutionText)
  
  // 自动判断学科
  const subject = detectSubject(questionText)
  if (subject) {
    console.log('  ✅ 学科:', subject)
    form.value.subject = subject
  } else {
    console.log('  ⚠️ 无法判断学科，默认数学')
    form.value.subject = '数学'
  }
  
  // 填充题目内容
  if (questionText && questionText.trim()) {
    console.log('  ✅ 题目内容长度:', questionText.length)
    form.value.content = questionText.trim()
  } else {
    console.warn('  ⚠️ 题目内容为空')
  }
  
  // 尝试从解答中提取答案和解析
  if (solutionText && solutionText.trim()) {
    const parsed = extractAnswerAndAnalysis(solutionText)
    if (parsed.answer) {
      console.log('  ✅ 提取答案:', parsed.answer)
      form.value.answer = parsed.answer
    }
    if (parsed.analysis) {
      console.log('  ✅ 提取解析长度:', parsed.analysis.length)
      form.value.analysis = parsed.analysis
    }
  }
  
  // 自动判断难度
  const difficulty = detectDifficulty(questionText)
  if (difficulty) {
    console.log('  ✅ 难度:', difficulty)
    form.value.difficulty = difficulty
  } else {
    console.log('  ⚠️ 无法判断难度，默认中等')
    form.value.difficulty = '中等'
  }
  
  console.log('✅ 基础解析完成!')
  console.log('当前表单状态:', JSON.stringify(form.value, null, 2))
  ElMessage.success('✅ 基础解析完成！请检查并补充信息')
}

// ⚠️ 已废弃 - 使用 parseQuestionWithNLP 替代
// 保留作为后备方案

// 检测学科
const detectSubject = (text: string): string => {
  const keywords: Record<string, string[]> = {
    '数学': ['函数', '方程', '几何', '微分', '积分', '三角', '圆', '抛物线', '向量', '矩阵', '概率', '统计', '求导', '极限'],
    '物理': ['力', '速度', '加速度', '质量', '动量', '能量', '电流', '电压', '电阻', '磁场', '光', '波'],
    '化学': ['反应', '元素', '化合物', '原子', '分子', '离子', '酸', '碱', '盐', '氧化', '还原'],
    '英语': ['translate', 'grammar', 'vocabulary', 'passage', 'reading', 'writing'],
    '语文': ['文章', '作者', '诗', '词', '句', '段落', '修辞', '中心思想']
  }
  
  for (const [subject, words] of Object.entries(keywords)) {
    if (words.some(word => text.includes(word))) {
      return subject
    }
  }
  
  return ''
}

// 从解答文本中提取答案和解析
const extractAnswerAndAnalysis = (text: string): { answer: string, analysis: string } => {
  let answer = ''
  let analysis = ''
  
  // 尝试提取答案部分
  const answerMatch = text.match(/(?:答案|答|解答)[：:]\s*([^\n]+)/i)
  if (answerMatch) {
    answer = answerMatch[1].trim()
  }
  
  // 尝试提取解析部分
  const analysisMatch = text.match(/(?:解析|分析|解题步骤|思路)[：:][\s\S]+/i)
  if (analysisMatch) {
    analysis = analysisMatch[0].trim()
  } else {
    // 如果没有明确标记，整段文本作为解析
    analysis = text.trim()
  }
  
  return { answer, analysis }
}

// 检测难度
const detectDifficulty = (text: string): string => {
  const length = text.length
  
  // 基于题目长度和复杂度关键词判断
  const hardKeywords = ['证明', '推导', '综合', '探究', '复杂']
  const hasHardKeyword = hardKeywords.some(word => text.includes(word))
  
  if (hasHardKeyword || length > 200) {
    return '困难'
  } else if (length > 100) {
    return '中等'
  } else {
    return '简单'
  }
}

// AI分析功能
const showAIAnalysis = async (error: any) => {
  if (error.aiAnalysis) {
    // 已有分析，清除显示
    error.aiAnalysis = null
    return
  }
  
  ElMessage.info('AI正在分析中，请稍候...')
  try {
    const res = await api.post('/api/ai/analyze-error', {
      subject: error.question.subject,
      questionContent: error.question.content,
      correctAnswer: error.question.answer || '',
      userAnswer: '学生答案',
      difficulty: error.question.difficulty || '中等'
    })
    
    // 将分析结果格式化为HTML
    error.aiAnalysis = formatAIResponse(res.data.data)
    ElMessage.success('AI分析完成！')
  } catch (error) {
    ElMessage.error('AI分析失败，请稍后重试')
  }
}

// 格式化AI响应 - 优化显示效果
const formatAIResponse = (text: string) => {
  if (!text) return ''
  
  // 1. 处理换行
  text = text.replace(/\n/g, '<br>')
  
  // 2. 处理标题【】，使用更醒目的样式
  text = text.replace(/【(.*?)】/g, (match, title) => {
    // 根据标题类型使用不同颜色
    let color = '#667eea'
    if (title.includes('第一步') || title.includes('第二步') || title.includes('第三步')) {
      color = '#409eff'  // 蓝色
    } else if (title.includes('错误') || title.includes('易错')) {
      color = '#f56c6c'  // 红色
    } else if (title.includes('知识点') || title.includes('核心')) {
      color = '#67c23a'  // 绿色
    }
    return `<div class="ai-section-title" style="color: ${color}; font-size: 16px; font-weight: bold; margin: 16px 0 8px 0; padding-left: 8px; border-left: 4px solid ${color};">📌 ${title}</div>`
  })
  
  // 3. 处理列表项（• 或数字.）
  text = text.replace(/(^|<br>)(•|\d+\.)\s/g, '$1<span style="color: #409eff; font-weight: bold; margin-right: 4px;">$2</span> ')
  
  // 4. 处理步骤标记
  text = text.replace(/步骤(\d+)：/g, '<span style="display: inline-block; background: #ecf5ff; color: #409eff; padding: 2px 8px; border-radius: 4px; font-weight: bold; margin: 8px 0;">步骤$1</span>：')
  
  // 5. 处理"说明："、"计算："等关键词
  text = text.replace(/(说明|计算|验证|原因|结果|答案)：/g, '<strong style="color: #909399;">$1：</strong>')
  
  // 6. 处理警告提醒 ⚠️
  text = text.replace(/⚠️/g, '<span style="color: #e6a23c; font-size: 18px;">⚠️</span>')
  
  // 7. 增加段落间距
  text = text.replace(/(<br>){2,}/g, '<br><div style="margin: 12px 0;"></div>')
  
  return `<div style="line-height: 1.8; font-size: 14px;">${text}</div>`
}

const askAIQuestion = (error: any) => {
  currentError.value = error
  aiQuestionDialog.value = true
  aiMessages.value = []
  aiQuestion.value = ''
}

// 快捷提问
const quickAsk = (question: string) => {
  aiQuestion.value = question
  sendAIQuestion()
}

const sendAIQuestion = async () => {
  if (!aiQuestion.value.trim()) return
  
  const userMsg = aiQuestion.value
  aiMessages.value.push({
    role: 'user',
    content: userMsg,
    time: new Date().toLocaleTimeString()
  })
  
  aiQuestion.value = ''
  asking.value = true
  
  // 滚动到底部
  setTimeout(() => scrollChatToBottom(), 100)
  
  try {
    const res = await api.post('/api/ai/ask', {
      subject: currentError.value.question.subject,
      question: userMsg,
      context: `题目：${currentError.value.question.content}\n正确答案：${currentError.value.question.answer || '未知'}`
    })
    
    if (res.data.success) {
    aiMessages.value.push({
      role: 'ai',
        content: res.data.data,
        time: new Date().toLocaleTimeString()
    })
      // 滚动到底部
      setTimeout(() => scrollChatToBottom(), 100)
    } else {
      ElMessage.error(res.data.message || 'AI回答失败')
    }
  } catch (error) {
    console.error('AI问答失败:', error)
    ElMessage.error('AI回答失败，请稍后重试')
  } finally {
    asking.value = false
  }
}

// 滚动聊天到底部
const scrollChatToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 获取相似题目推荐
const getSimilarQuestions = async (error: any) => {
  loadingSimilar.value = true
  ElMessage.info('🤖 AI正在生成相似题目...')
  
  try {
    const res = await api.post('/api/ai/ask', {
      subject: error.question.subject,
      question: `请根据以下题目，生成3道相似的练习题，要求：

【原题】
${error.question.content}

【要求】
1. 题型相似，难度相当
2. 考察相同的知识点
3. 每道题必须包含：题目、正确答案、详细解析
4. 如果是选择题，必须包含选项
5. 返回JSON格式（不要markdown代码块）：

【选择题格式】
[
  {
    "question": "题目内容（不含选项）",
    "type": "选择题",
    "options": [
      {"label": "A", "text": "选项A内容"},
      {"label": "B", "text": "选项B内容"},
      {"label": "C", "text": "选项C内容"},
      {"label": "D", "text": "选项D内容"}
    ],
    "answer": "A",
    "analysis": "详细解析"
  }
]

【填空题/计算题格式】
[
  {
    "question": "题目内容",
    "type": "填空题",
    "answer": "正确答案",
    "analysis": "详细解析"
  }
]`,
      context: ''
    })
    
    if (res.data.success) {
      const aiResponse = res.data.data
      console.log('AI推荐题目响应:', aiResponse)
      
      try {
        // 尝试解析JSON
        let jsonStr = aiResponse.trim()
        jsonStr = jsonStr.replace(/^```json\s*/gi, '')
        jsonStr = jsonStr.replace(/^```\s*/gi, '')
        jsonStr = jsonStr.replace(/\s*```$/gi, '')
        
        const jsonMatch = jsonStr.match(/\[[\s\S]*\]/)
        if (jsonMatch) {
          jsonStr = jsonMatch[0]
        }
        
        const questions = JSON.parse(jsonStr)
        
        if (Array.isArray(questions) && questions.length > 0) {
          similarQuestions.value = questions
          currentQuestionIndex.value = 0
          userAnswers.value = new Array(questions.length).fill('')
          questionResults.value = new Array(questions.length).fill(null)
          similarQuestionsDialog.value = true
          ElMessage.success('✅ 已生成' + questions.length + '道练习题！')
        } else {
          throw new Error('解析结果不是数组')
        }
      } catch (parseError) {
        console.error('JSON解析失败:', parseError)
        console.log('原始响应:', aiResponse)
        
        // 如果JSON解析失败，使用简单文本展示
        ElMessageBox.alert(formatAIResponse(aiResponse), '相似题推荐', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了'
    })
      }
    }
  } catch (error) {
    console.error('推荐失败:', error)
    ElMessage.error('推荐失败，请重试')
  } finally {
    loadingSimilar.value = false
  }
}

// 提取知识点并生成知识图谱
const extractKnowledgePoints = async (error: any) => {
  currentError.value = error
  knowledgeMapDialog.value = true
  loadingKnowledge.value = true
  knowledgeMap.value = null
  
  console.log('📚 开始提取知识点...')
  ElMessage.info('🤖 AI正在分析知识点...')
  
  try {
    const res = await api.post('/api/ai/ask', {
      subject: error.question.subject,
      question: `请分析以下题目的知识点体系，并生成知识图谱：

【题目】
${error.question.content}

【学科】${error.question.subject}
【难度】${error.question.difficulty || '中等'}

请严格按照以下JSON格式返回（不要markdown代码块）：
{
  "prerequisites": [
    {"name": "前置知识点名称", "description": "这个知识点的简要说明"}
  ],
  "current": [
    {"name": "当前知识点名称", "description": "这个知识点的详细说明", "difficulty": "基础/中等/困难"}
  ],
  "advanced": [
    {"name": "后续知识点名称", "description": "这个知识点的说明"}
  ],
  "suggestions": "针对这道题的学习建议"
}

要求：
1. prerequisites: 做这道题需要先掌握的基础知识（2-3个）
2. current: 这道题主要考察的知识点（2-4个）
3. advanced: 掌握这道题后可以学习的进阶知识（2-3个）
4. suggestions: 给出具体的学习路径建议

只返回JSON，不要其他文字！`,
      context: ''
    })
    
    if (res.data.success) {
      const aiResponse = res.data.data
      console.log('📥 收到AI响应:', aiResponse)
      
      try {
        // 尝试解析JSON
        let jsonStr = aiResponse
        
        // 移除可能的markdown代码块标记
        jsonStr = jsonStr.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim()
        
        // 尝试提取JSON对象
        const jsonMatch = jsonStr.match(/\{[\s\S]*\}/)
        if (jsonMatch) {
          jsonStr = jsonMatch[0]
        }
        
        console.log('📝 准备解析的JSON:', jsonStr)
        const parsedData = JSON.parse(jsonStr)
        
        console.log('✅ 解析成功:', parsedData)
        knowledgeMap.value = parsedData
        ElMessage.success('✅ 知识图谱生成成功！')
      } catch (parseError) {
        console.error('❌ JSON解析失败:', parseError)
        console.log('原始响应:', aiResponse)
        
        // 解析失败，提供默认结构
        ElMessage.warning('知识点提取成功，但格式化失败')
        knowledgeMap.value = {
          current: [
            {
              name: error.question.subject + '相关知识点',
              description: aiResponse,
              difficulty: error.question.difficulty || '中等'
            }
          ],
          prerequisites: [],
          advanced: [],
          suggestions: '请查看AI的详细回复'
        }
      }
    } else {
      ElMessage.error('知识点提取失败')
    }
  } catch (error) {
    console.error('❌ 提取知识点失败:', error)
    ElMessage.error('提取失败，请重试')
  } finally {
    loadingKnowledge.value = false
  }
}

// 获取难度标签类型
const getDifficultyTagType = (difficulty: string) => {
  const map: any = {
    '基础': 'success',
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger',
    '较难': 'danger'
  }
  return map[difficulty] || 'info'
}

// 生成知识树数据
const generateTreeData = (knowledgeMap: any) => {
  if (!knowledgeMap) return null
  
  const treeData: any = {
    name: '知识体系',
    children: [],
    itemStyle: {
      color: '#667eea',
      borderColor: '#667eea'
    },
    label: {
      fontSize: 16,
      fontWeight: 'bold',
      color: '#667eea'
    }
  }
  
  // 添加前置知识点
  if (knowledgeMap.prerequisites && knowledgeMap.prerequisites.length > 0) {
    treeData.children.push({
      name: '📘 前置知识',
      collapsed: false,
      itemStyle: {
        color: '#2196f3',
        borderColor: '#2196f3'
      },
      label: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#1976d2'
      },
      children: knowledgeMap.prerequisites.map((point: any) => ({
        name: point.name,
        value: point.description,
        knowledgeType: 'prerequisite',
        knowledgeData: point,
        itemStyle: {
          color: '#64b5f6',
          borderColor: '#2196f3'
        },
        label: {
          fontSize: 13,
          color: '#1976d2'
        }
      }))
    })
  }
  
  // 添加当前知识点
  if (knowledgeMap.current && knowledgeMap.current.length > 0) {
    treeData.children.push({
      name: '⭐ 当前考察',
      collapsed: false,
      itemStyle: {
        color: '#ff9800',
        borderColor: '#ff9800'
      },
      label: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#e65100'
      },
      children: knowledgeMap.current.map((point: any) => ({
        name: point.name,
        value: point.description,
        knowledgeType: 'current',
        knowledgeData: point,
        itemStyle: {
          color: '#ffb74d',
          borderColor: '#ff9800',
          borderWidth: 2
        },
        label: {
          fontSize: 14,
          fontWeight: 'bold',
          color: '#e65100'
        }
      }))
    })
  }
  
  // 添加后置知识点
  if (knowledgeMap.advanced && knowledgeMap.advanced.length > 0) {
    treeData.children.push({
      name: '📗 后续拓展',
      collapsed: false,
      itemStyle: {
        color: '#9c27b0',
        borderColor: '#9c27b0'
      },
      label: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#6a1b9a'
      },
      children: knowledgeMap.advanced.map((point: any) => ({
        name: point.name,
        value: point.description,
        knowledgeType: 'advanced',
        knowledgeData: point,
        itemStyle: {
          color: '#ba68c8',
          borderColor: '#9c27b0'
        },
        label: {
          fontSize: 13,
          color: '#6a1b9a'
        }
      }))
    })
  }
  
  return treeData
}

// 初始化图表
const initChart = () => {
  console.log('🌳 开始初始化知识图谱树状图...')
  console.log('知识图谱数据:', knowledgeMap.value)
  
  if (!knowledgeMap.value) {
    console.warn('知识图谱数据为空')
    return
  }
  
  // 等待 DOM 更新
  nextTick(() => {
    console.log('DOM 已更新，查找图表容器...')
    console.log('图表容器元素:', knowledgeChartDom.value)
    
    if (!knowledgeChartDom.value) {
      console.error('❌ 图表容器未找到！')
      return
    }
    
    // 初始化图表实例
    if (!knowledgeChartInstance) {
      console.log('创建新的图表实例...')
      knowledgeChartInstance = echarts.init(knowledgeChartDom.value)
      
      // 添加点击事件
      knowledgeChartInstance.on('click', (params: any) => {
        handleNodeClick(params)
      })
    } else {
      console.log('使用现有图表实例')
    }
    
    const treeData = generateTreeData(knowledgeMap.value)
    console.log('生成的树数据:', treeData)
    const chartOption = {
      tooltip: {
        trigger: 'item',
        triggerOn: 'mousemove',
        formatter: (params: any) => {
          if (params.data.value) {
            return `<div style="max-width: 300px;">
              <strong>${params.data.name}</strong><br/>
              <span style="color: #666;">${params.data.value}</span><br/>
              <span style="color: #667eea; font-size: 12px;">💡 点击查看相关题目</span>
            </div>`
          }
          return params.data.name
        }
      },
      series: [
        {
          type: 'tree',
          data: [treeData],
          top: '5%',
          left: '8%',
          bottom: '5%',
          right: '8%',
          symbolSize: 10,
          orient: 'vertical',
          expandAndCollapse: true,
          initialTreeDepth: -1,
          layout: 'orthogonal',
          // 增加节点间距
          edgeShape: 'polyline',
          edgeForkPosition: '50%',
          // 固定节点间距
          nodePadding: 60,
          layerPadding: 100,
          // 调整节点间距
          itemStyle: {
            borderWidth: 2
          },
          lineStyle: {
            width: 2,
            curveness: 0.5
          },
          label: {
            show: true,
            position: 'top',
            distance: 15,
            rotate: 0,
            verticalAlign: 'middle',
            align: 'center',
            fontSize: 13,
            fontWeight: 'bold',
            color: '#333',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderRadius: 6,
            padding: [6, 12],
            shadowBlur: 3,
            shadowColor: 'rgba(0, 0, 0, 0.1)',
            shadowOffsetY: 2,
            overflow: 'truncate',
            width: 120
          },
          leaves: {
            label: {
              position: 'bottom',
              distance: 15,
              rotate: 0,
              verticalAlign: 'middle',
              align: 'center',
              fontSize: 12,
              color: '#555',
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              padding: [5, 10],
              borderRadius: 6,
              shadowBlur: 3,
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              shadowOffsetY: 2,
              overflow: 'truncate',
              width: 100
            }
          },
          animationDurationUpdate: 750,
          emphasis: {
            focus: 'descendant',
            lineStyle: {
              width: 3
            },
            label: {
              fontSize: 13,
              fontWeight: 'bold'
            }
          }
        }
      ]
    }
    
    console.log('设置图表配置...')
    knowledgeChartInstance.setOption(chartOption)
    console.log('✅ 知识图谱树状图渲染完成！')
  })
}

// 监听知识图谱数据变化，生成树状图
watch(knowledgeMap, (newVal) => {
  if (newVal) {
    initChart()
  }
}, { deep: true })

// 监听对话框打开，初始化图表
watch(knowledgeMapDialog, (newVal) => {
  if (newVal && knowledgeMap.value) {
    // 对话框打开时，延迟初始化图表
    setTimeout(() => {
      initChart()
    }, 100)
  } else if (!newVal) {
    // 对话框关闭时，销毁图表实例
    if (knowledgeChartInstance) {
      knowledgeChartInstance.dispose()
      knowledgeChartInstance = null
    }
  }
})

// 处理节点点击事件
const handleNodeClick = (params: any) => {
  console.log('点击节点:', params)
  
  if (params.data && params.data.knowledgeType) {
    // 只有知识点节点可以点击
    selectedKnowledgePoint.value = params.data.name
    getRecommendedQuestions(params.data)
  }
}

// 获取推荐题目
const getRecommendedQuestions = async (knowledgePoint: any) => {
  loadingRecommend.value = true
  recommendedQuestionsDialog.value = true
  recommendedQuestions.value = []
  
  ElMessage.info(`🤖 AI正在推荐"${knowledgePoint.name}"相关题目...`)
  
  try {
    const res = await api.post('/api/ai/ask', {
      subject: currentError.value?.question?.subject || '数学',
      question: `请根据以下知识点，推荐3道相关练习题：

【知识点】${knowledgePoint.name}
【描述】${knowledgePoint.value}
【难度】${knowledgePoint.knowledgeData?.difficulty || '中等'}

要求：
1. 题目要紧密围绕这个知识点
2. 难度从易到难递增
3. 每道题包含完整的题目、答案、解析
4. 返回JSON格式（不要markdown代码块）：

[
  {
    "question": "题目内容",
    "type": "选择题/填空题/计算题",
    "options": [{"label": "A", "text": "选项A"}], // 选择题才需要
    "answer": "正确答案",
    "analysis": "详细解析",
    "difficulty": "简单/中等/困难"
  }
]`,
      context: ''
    })
    
    if (res.data.success) {
      const aiResponse = res.data.data
      console.log('📥 收到推荐题目:', aiResponse)
      
      try {
        let jsonStr = aiResponse.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim()
        const jsonMatch = jsonStr.match(/\[[\s\S]*\]/)
        if (jsonMatch) {
          jsonStr = jsonMatch[0]
        }
        
        const questions = JSON.parse(jsonStr)
        if (Array.isArray(questions) && questions.length > 0) {
          recommendedQuestions.value = questions
          // 重置状态
          currentRecommendIndex.value = 0
          recommendAnswers.value = new Array(questions.length).fill('')
          recommendResults.value = new Array(questions.length).fill(null)
          ElMessage.success(`✅ 已推荐 ${questions.length} 道练习题！`)
        }
      } catch (parseError) {
        console.error('JSON解析失败:', parseError)
        ElMessage.error('题目格式解析失败')
      }
    }
  } catch (error) {
    console.error('推荐失败:', error)
    ElMessage.error('推荐失败，请重试')
  } finally {
    loadingRecommend.value = false
  }
}

// 提交推荐题答案
const submitRecommendAnswer = () => {
  const current = recommendedQuestions.value[currentRecommendIndex.value]
  const userAnswer = recommendAnswers.value[currentRecommendIndex.value]
  
  if (!userAnswer || !userAnswer.trim()) {
    ElMessage.warning('请先输入答案')
    return
  }
  
  // 判断对错
  const isCorrect = userAnswer.trim().toLowerCase() === current.answer.trim().toLowerCase()
  
  recommendResults.value[currentRecommendIndex.value] = {
    isCorrect,
    userAnswer,
    correctAnswer: current.answer,
    analysis: current.analysis
  }
  
  if (isCorrect) {
    ElMessage.success('✅ 回答正确！')
  } else {
    ElMessage.error('❌ 回答错误，查看解析吧')
  }
}

// 上一题
const prevRecommendQuestion = () => {
  if (currentRecommendIndex.value > 0) {
    currentRecommendIndex.value--
  }
}

// 下一题
const nextRecommendQuestion = () => {
  if (currentRecommendIndex.value < recommendedQuestions.value.length - 1) {
    currentRecommendIndex.value++
  }
}

// 显示练习总结
const showRecommendSummary = () => {
  const total = recommendedQuestions.value.length
  const answered = recommendResults.value.filter(r => r !== null).length
  const correct = recommendResults.value.filter(r => r && r.isCorrect).length
  const accuracy = answered > 0 ? Math.round((correct / answered) * 100) : 0
  
  ElMessageBox.alert(
    `<div style="padding: 20px; text-align: center;">
      <h3 style="margin-bottom: 20px;">📊 练习总结</h3>
      <p style="font-size: 16px; margin: 10px 0;">总题数：<strong>${total}</strong> 题</p>
      <p style="font-size: 16px; margin: 10px 0;">已答：<strong>${answered}</strong> 题</p>
      <p style="font-size: 16px; margin: 10px 0;">正确：<strong style="color: #52c41a;">${correct}</strong> 题</p>
      <p style="font-size: 16px; margin: 10px 0;">正确率：<strong style="color: #667eea;">${accuracy}%</strong></p>
      ${answered < total ? '<p style="color: #ff9800; margin-top: 20px;">💡 还有题目未完成，继续加油！</p>' : ''}
    </div>`,
    '练习完成',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 关闭推荐对话框
const closeRecommendDialog = () => {
  recommendedQuestionsDialog.value = false
  // 重置状态
  currentRecommendIndex.value = 0
  recommendAnswers.value = []
  recommendResults.value = []
}

// 查看教师反馈
const viewTeacherFeedback = async (errorBookId: number) => {
  teacherFeedbackDialog.value = true
  loadingFeedback.value = true
  teacherFeedbacks.value = []

  try {
    const res = await api.get(`/api/classroom/feedback/errorbook/${errorBookId}`)
    if (res.data.success) {
      teacherFeedbacks.value = res.data.data
      if (teacherFeedbacks.value.length === 0) {
        ElMessage.info('暂无教师反馈')
      }
    }
  } catch (error) {
    console.error('加载教师反馈失败', error)
    ElMessage.error('加载教师反馈失败')
  } finally {
    loadingFeedback.value = false
  }
}

// ============ 分享到共享池功能 ============

// 打开分享对话框
const shareToPool = (error: any) => {
  currentShareError.value = error
  shareForm.value = {
    scope: '班级',
    tags: error.tags || ''
  }
  shareDialog.value = true
}

// 确认分享
const confirmShare = async () => {
  if (!currentShareError.value) return

  sharing.value = true
  try {
    const res = await api.post('/api/share-pool', {
      errorBookId: currentShareError.value.id,
      scope: shareForm.value.scope,
      tags: shareForm.value.tags
    })

    if (res.data.success || res.data.code === 200) {
      ElMessage.success('🎉 分享成功！等待教师审核后将显示在共享池')
      
      // 更新本地错题状态
      currentShareError.value.status = 'SHARED'
      
      // 关闭对话框
      shareDialog.value = false
      
      // 刷新列表
      await fetchErrorBooks()
    } else {
      ElMessage.error(res.data.message || '分享失败')
    }
  } catch (error: any) {
    console.error('分享失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '分享失败'
    ElMessage.error(errorMsg)
  } finally {
    sharing.value = false
  }
}

// 提交当前题目答案
const submitCurrentAnswer = async () => {
  const current = similarQuestions.value[currentQuestionIndex.value]
  const userAnswer = userAnswers.value[currentQuestionIndex.value]
  
  if (!userAnswer || !userAnswer.trim()) {
    ElMessage.warning('请先输入答案')
    return
  }
  
  // 判断对错（简单比较）
  const isCorrect = userAnswer.trim().toLowerCase() === current.answer.trim().toLowerCase()
  
  questionResults.value[currentQuestionIndex.value] = {
    isCorrect,
    userAnswer,
    correctAnswer: current.answer,
    analysis: current.analysis
  }
  
  if (isCorrect) {
    ElMessage.success('✅ 回答正确！')
  } else {
    ElMessage.error('❌ 回答错误，查看解析吧')
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < similarQuestions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 关闭相似题对话框
const closeSimilarDialog = () => {
  similarQuestionsDialog.value = false
  similarQuestions.value = []
  currentQuestionIndex.value = 0
  userAnswers.value = []
  questionResults.value = []
}

// 图片预览
const previewImage = (url: string) => {
  window.open(url, '_blank')
}

// 显示练习总结
const showPracticeSummary = () => {
  const total = similarQuestions.value.length
  const answered = questionResults.value.filter(r => r !== null).length
  const correct = questionResults.value.filter(r => r?.isCorrect === true).length
  
  const accuracy = answered > 0 ? Math.round((correct / answered) * 100) : 0
  
  ElMessageBox.alert(`
    <div style="text-align: center; padding: 20px;">
      <h3 style="color: #667eea; margin-bottom: 20px;">📊 练习总结</h3>
      <div style="font-size: 18px; line-height: 2;">
        <p>📝 总题数：${total} 题</p>
        <p>✍️ 已答题：${answered} 题</p>
        <p style="color: #52c41a;">✅ 答对：${correct} 题</p>
        <p style="color: #f5222d;">❌ 答错：${answered - correct} 题</p>
        <p style="color: #667eea; font-size: 24px; font-weight: bold; margin-top: 20px;">
          正确率：${accuracy}%
        </p>
      </div>
      ${accuracy >= 80 ? '<p style="color: #52c41a; font-size: 16px; margin-top: 20px;">🎉 太棒了！掌握得很好！</p>' : 
        accuracy >= 60 ? '<p style="color: #faad14; font-size: 16px; margin-top: 20px;">💪 继续加油！再练习几道！</p>' : 
        '<p style="color: #f5222d; font-size: 16px; margin-top: 20px;">📚 需要加强哦！建议复习相关知识点！</p>'}
    </div>
  `, '练习完成', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  }).then(() => {
    closeSimilarDialog()
  })
}

onMounted(() => {
  fetchErrorBooks()
})
</script>

<style scoped>
.error-book-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: #f5f7fa;
  min-height: 100vh;
}

/* ========== 顶部标题栏 ========== */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.title-icon {
  font-size: 32px;
}

.main-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: white;
}

.subtitle {
  font-size: 14px;
  margin: 4px 0 0 0;
  opacity: 0.9;
  color: white;
}

.add-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: white;
  color: #667eea;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

/* ========== 统计卡片 ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.total {
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

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

/* ========== 筛选栏 ========== */
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-select {
  width: 200px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

/* ========== 错题列表 ========== */
.error-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.error-item {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.error-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.error-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(90deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #e9ecef;
}

.header-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.subject-badge {
  padding: 6px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: white;
}

.subject-badge.math { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.subject-badge.chinese { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.subject-badge.english { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.subject-badge.physics { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.subject-badge.chemistry { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.subject-badge.biology { background: linear-gradient(135deg, #30cfd0 0%, #330867 100%); }

.difficulty-badge {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.difficulty-badge.easy { background: #d4edda; color: #155724; }
.difficulty-badge.medium { background: #fff3cd; color: #856404; }
.difficulty-badge.hard { background: #f8d7da; color: #721c24; }

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.shared { background: #d1ecf1; color: #0c5460; }

.header-actions {
  display: flex;
  gap: 8px;
}

.error-item-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-block,
.answer-block,
.reason-block,
.correction-block {
  padding: 16px;
  border-radius: 12px;
  background: #f8f9fa;
}

.question-block { border-left: 4px solid #667eea; }
.answer-block { border-left: 4px solid #28a745; }
.reason-block { border-left: 4px solid #dc3545; background: #fff5f5; }
.correction-block { border-left: 4px solid #17a2b8; }

.block-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
}

.block-icon {
  font-size: 18px;
}

.block-content {
  font-size: 15px;
  line-height: 1.8;
  color: #495057;
  white-space: pre-wrap;
}

.reason-text {
  color: #dc3545;
  font-weight: 500;
}

.tags-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  background: #e7f3ff;
  color: #0066cc;
  border: 1px solid #b3d9ff;
}

.error-item-footer {
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.time-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6c757d;
}

/* ========== 空状态 ========== */
.empty-container {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
}

.empty-illustration {
  font-size: 120px;
  color: #dee2e6;
  margin-bottom: 24px;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #495057;
  margin: 0 0 12px 0;
}

.empty-desc {
  font-size: 16px;
  color: #6c757d;
  margin: 0 0 32px 0;
}

/* ========== 对话框 ========== */
:deep(.error-dialog) {
  border-radius: 16px;
}

:deep(.error-dialog .el-dialog__header) {
  padding: 24px 24px 0;
  border-bottom: none;
}

:deep(.error-dialog .el-dialog__body) {
  padding: 24px;
}

.error-form {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-col-2 {
  grid-column: span 1;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ========== 列表动画 ========== */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-select,
  .search-input {
    width: 100%;
    max-width: none;
  }

  .error-item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

/* ========== AI功能样式 ========== */
.ai-btn {
  color: #667eea !important;
  font-weight: 600;
}

.ai-btn:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

.ai-analysis-section {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-left: 4px solid #667eea;
  border-radius: 12px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
}

.ai-icon {
  font-size: 24px;
  color: #667eea;
}

.ai-title {
  font-size: 16px;
  font-weight: 700;
  color: #667eea;
  flex: 1;
}

.ai-content {
  line-height: 1.8;
  color: #495057;
  font-size: 15px;
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.ai-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

/* AI对话框样式 */
:deep(.ai-chat-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 16px 16px 0 0;
}

:deep(.ai-chat-dialog .el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.ai-chat-messages {
  max-height: 400px;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-message {
  display: flex;
  gap: 12px;
  animation: messageIn 0.3s ease;
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.ai-message.user .message-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

/* AI对话框新增样式 */
.question-context-card {
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.context-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 14px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  border: 1px solid #667eea;
  color: #667eea;
}

.quick-tag:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.empty-chat {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  color: #ddd;
  margin-bottom: 16px;
}

.empty-chat p {
  font-size: 14px;
  margin: 0;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 12px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 输入区域 */
.chat-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input-wrapper .el-input {
  flex: 1;
}

.send-btn {
  height: 64px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 600;
}

.send-btn:disabled {
  opacity: 0.5;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8f9fa;
  color: #495057;
  line-height: 1.6;
}

.ai-message.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* ========== 拍照识别功能样式 ========== */

/* 录入模式切换标签 */
.input-mode-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 12px;
}

.mode-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 15px;
  font-weight: 500;
  color: #666;
  background: white;
  border: 2px solid transparent;
}

.mode-tab:hover {
  color: #667eea;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.mode-tab.active {
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.mode-tab .el-icon {
  font-size: 18px;
}

/* 拍照识别区域 */
.photo-recognition-section {
  margin-bottom: 24px;
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #f8f9ff 0%, #fef5ff 100%);
}

.upload-area:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #fae8ff 100%);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
}

.upload-icon {
  margin-bottom: 16px;
}

.upload-icon .el-icon {
  font-size: 64px;
  color: #667eea;
}

.upload-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.upload-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 12px 0;
}

.upload-hint {
  font-size: 14px;
  color: #667eea;
  margin: 0;
  font-weight: 500;
}

/* 图片预览区域 */
.image-preview-section {
  border-radius: 16px;
  overflow: hidden;
  background: white;
  border: 2px solid #e4e7ed;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.image-preview {
  padding: 20px;
  text-align: center;
  background: #f5f7fa;
}

.image-preview img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 识别状态 */
.recognizing-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #fff8e1 0%, #ffe0b2 100%);
  color: #f57c00;
  font-weight: 600;
  font-size: 15px;
}

.recognizing-status .el-icon {
  font-size: 24px;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 识别成功状态 */
.recognition-success {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
  font-weight: 600;
  font-size: 15px;
}

.recognition-success .el-icon {
  font-size: 24px;
}

/* 图文对照 - 录入时 */
.content-with-image {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.content-input-wrapper {
  flex: 1;
}

.original-image-preview {
  width: 300px;
  flex-shrink: 0;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 12px;
  background: #f5f7fa;
}

.preview-label {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 8px;
  text-align: center;
}

.original-image-preview img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.original-image-preview img:hover {
  transform: scale(1.05);
}

/* 图文对照 - 错题本显示 */
.question-with-image {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.question-text {
  flex: 1;
  line-height: 1.8;
}

.question-image {
  width: 250px;
  flex-shrink: 0;
  border: 2px solid #667eea;
  border-radius: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9ff 0%, #fef5ff 100%);
}

.image-label {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 8px;
  text-align: center;
}

.question-image img {
  width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.question-image img:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

/* ========== 相似题练习对话框 ========== */
.practice-container {
  padding: 20px 0;
}

/* 进度条 */
.progress-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 30px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.progress-dot {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  border: 2px solid #dcdfe6;
  color: #909399;
}

.progress-dot:hover {
  transform: scale(1.1);
  border-color: #667eea;
}

.progress-dot.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.progress-dot.correct {
  background: #52c41a;
  color: white;
  border-color: #52c41a;
}

.progress-dot.wrong {
  background: #f5222d;
  color: white;
  border-color: #f5222d;
}

/* 题目卡片 */
.question-card {
  background: white;
  border: 2px solid #e4e7ed;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.question-number {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.question-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #fef5ff 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

/* 答题区域 */
.answer-section {
  margin-bottom: 24px;
}

.answer-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

/* 选择题选项 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  position: relative;
}

.option-item:hover:not(.disabled) {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateX(4px);
}

.option-item.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #fae8ff 100%);
}

.option-item.correct {
  border-color: #52c41a;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.option-item.wrong {
  border-color: #f5222d;
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
}

.option-item.disabled {
  cursor: not-allowed;
}

.option-label {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  background: #f5f7fa;
  color: #606266;
  flex-shrink: 0;
}

.option-item.selected .option-label {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.option-item.correct .option-label {
  background: #52c41a;
  color: white;
}

.option-item.wrong .option-label {
  background: #f5222d;
  color: white;
}

.option-content {
  flex: 1;
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
}

.correct-mark,
.wrong-mark {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
  flex-shrink: 0;
}

.correct-mark {
  background: #52c41a;
  color: white;
}

.wrong-mark {
  background: #f5222d;
  color: white;
}

.submit-btn {
  margin-top: 12px;
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 结果显示 */
.result-section {
  margin-top: 24px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.result-badge {
  text-align: center;
  padding: 12px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.result-badge.correct {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
  border: 2px solid #4caf50;
}

.result-badge.wrong {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  color: #c62828;
  border: 2px solid #f44336;
}

.answer-compare {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.answer-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 8px;
  font-size: 15px;
}

.answer-item .label {
  font-weight: 600;
  color: #606266;
  margin-right: 12px;
  min-width: 80px;
}

.correct-text {
  color: #52c41a;
  font-weight: 600;
}

.wrong-text {
  color: #f5222d;
  font-weight: 600;
  text-decoration: line-through;
}

/* 解析区域 */
.analysis-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 2px solid #667eea;
}

.analysis-title {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.analysis-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

/* 导航按钮 */
.navigation-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.navigation-buttons .el-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
}

/* ==================== 知识图谱样式 ==================== */
.knowledge-map-dialog :deep(.el-dialog__body) {
  max-height: 80vh;
  overflow-y: auto;
  padding: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #667eea;
}

.loading-container p {
  margin-top: 20px;
  font-size: 16px;
  color: #909399;
}

.knowledge-map-container {
  padding: 10px;
}

/* 题目信息卡片 */
.question-info-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #fef5ff 100%);
  border: 1px solid #e0e7ff;
}

.info-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
}

.info-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

/* 知识图谱 */
.knowledge-graph {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.knowledge-section {
  position: relative;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  position: relative;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}

.section-desc {
  font-size: 13px;
  opacity: 0.8;
  font-weight: normal;
}

/* 前置知识点样式 */
.section-header.prerequisite {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-left: 4px solid #2196f3;
}

/* 当前知识点样式 */
.section-header.current {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #e65100;
  border-left: 4px solid #ff9800;
}

/* 后置知识点样式 */
.section-header.advanced {
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
  color: #6a1b9a;
  border-left: 4px solid #9c27b0;
}

/* 知识点卡片 */
.knowledge-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 0 12px;
}

.knowledge-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: white;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.knowledge-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  transition: all 0.3s ease;
}

.knowledge-card.prerequisite::before {
  background: linear-gradient(180deg, #2196f3 0%, #64b5f6 100%);
}

.knowledge-card.current::before {
  background: linear-gradient(180deg, #ff9800 0%, #ffb74d 100%);
}

.knowledge-card.advanced::before {
  background: linear-gradient(180deg, #9c27b0 0%, #ba68c8 100%);
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.card-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  color: white;
}

.knowledge-card.prerequisite .card-number {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
}

.knowledge-card.current .card-number {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  font-size: 20px;
}

.knowledge-card.advanced .card-number {
  background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.card-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.card-difficulty {
  margin-top: 4px;
}

/* 流程箭头 */
.flow-arrow {
  text-align: center;
  font-size: 40px;
  color: #667eea;
  margin: 16px 0;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  animation: bounce-arrow 2s infinite;
}

@keyframes bounce-arrow {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(8px);
  }
}

/* 学习建议卡片 */
.suggestions-card {
  margin-top: 24px;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border: 1px solid #81c784;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #2e7d32;
}

.suggestions-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.suggestions-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  padding: 12px;
  background: white;
  border-radius: 8px;
  white-space: pre-wrap;
}

/* ==================== 知识树图表样式 ==================== */
.knowledge-tree-chart {
  background: linear-gradient(135deg, #f8f9ff 0%, #fff5f8 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e7ff;
}

/* ==================== 推荐题目对话框样式（复用相似题样式）==================== */
/* 推荐题目对话框使用与相似题相同的样式类 */

/* ==================== 教师反馈对话框样式 ==================== */
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
}

.feedback-item {
  background: linear-gradient(135deg, #f5f7ff 0%, #fef5ff 100%);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.feedback-item:hover {
  border-color: rgba(102, 126, 234, 0.3);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.teacher-avatar {
  width: 48px;
  height: 48px;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.feedback-meta {
  flex: 1;
}

.teacher-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.feedback-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.feedback-rating {
  display: flex;
  align-items: center;
}

.feedback-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid var(--neon-blue);
}

.teacher-feedback-btn {
  color: #10b981 !important;
}

.teacher-feedback-btn:hover {
  color: #059669 !important;
  background: rgba(16, 185, 129, 0.1) !important;
}

/* ==================== 智能识别按钮样式 ==================== */
.smart-parse-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px dashed #0ea5e9;
  border-radius: 12px;
  animation: pulse-border 2s ease-in-out infinite;
}

@keyframes pulse-border {
  0%, 100% {
    border-color: #0ea5e9;
    box-shadow: 0 0 0 0 rgba(14, 165, 233, 0.4);
  }
  50% {
    border-color: #38bdf8;
    box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.2);
  }
}

.auto-parse-status {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.auto-parse-status .el-icon {
  font-size: 18px;
}

.auto-parse-status .rotating {
  color: #0ea5e9;
  animation: rotate 1s linear infinite;
}

.auto-parse-status .success-icon {
  color: #52c41a;
  animation: scale-in 0.3s ease-out;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes scale-in {
  from { transform: scale(0); }
  to { transform: scale(1); }
}

.auto-parse-status .status-text {
  font-size: 14px;
  color: #0369a1;
  font-weight: 500;
}

.auto-parse-status .status-text.success {
  color: #059669;
}

.smart-parse-hint .el-button {
  flex-shrink: 0;
  font-size: 13px;
}

/* ==================== 分享到共享池样式 ==================== */
.share-btn {
  color: #10b981 !important;
  font-weight: 600;
}

.share-btn:hover {
  color: #059669 !important;
  background: rgba(16, 185, 129, 0.1) !important;
}

.share-form {
  padding: 10px 0;
}

.share-preview {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #bae6fd;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.preview-label {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 12px;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-subject {
  display: inline-block;
  padding: 4px 12px;
  background: #0ea5e9;
  color: white;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
}

.preview-question {
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
}

.share-form-content .el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.share-form-content .el-radio {
  margin-right: 0;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.3s;
}

.share-form-content .el-radio:hover {
  border-color: #10b981;
  background: #f0fdf4;
}

.share-form-content .el-radio.is-checked {
  border-color: #10b981;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.form-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

.share-notice {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fffbeb;
  border: 2px solid #fde68a;
  border-radius: 12px;
  margin-top: 16px;
}

.share-notice .el-icon {
  font-size: 20px;
  color: #f59e0b;
  flex-shrink: 0;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 8px;
}

.share-notice ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #78350f;
  line-height: 1.8;
}

.share-notice li {
  margin: 4px 0;
}
</style>
