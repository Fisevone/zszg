<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Setting /></el-icon>
        系统设置
      </h1>
    </div>

    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview-section">
      <el-col :span="6">
        <div class="overview-card gradient-bg-blue">
          <div class="overview-icon"><el-icon><UserFilled /></el-icon></div>
          <div class="overview-content">
            <div class="overview-value">{{ systemStats.totalUsers || 0 }}</div>
            <div class="overview-label">用户总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="overview-card gradient-bg-green">
          <div class="overview-icon"><el-icon><Postcard /></el-icon></div>
          <div class="overview-content">
            <div class="overview-value">{{ systemStats.studentCount || 0 }}</div>
            <div class="overview-label">学生用户</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="overview-card gradient-bg-orange">
          <div class="overview-icon"><el-icon><Avatar /></el-icon></div>
          <div class="overview-content">
            <div class="overview-value">{{ systemStats.teacherCount || 0 }}</div>
            <div class="overview-label">教师用户</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="overview-card gradient-bg">
          <div class="overview-icon"><el-icon><Document /></el-icon></div>
          <div class="overview-content">
            <div class="overview-value">{{ systemStats.totalErrors || 0 }}</div>
            <div class="overview-label">错题总数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 系统配置 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-title">
          <el-icon><Tools /></el-icon>
          <span>系统配置</span>
        </div>
      </template>

      <el-form label-width="150px">
        <el-divider content-position="left">基础设置</el-divider>
        
        <el-form-item label="系统名称">
          <el-input v-model="settings.systemName" placeholder="知错就改" />
        </el-form-item>

        <el-form-item label="系统描述">
          <el-input
            v-model="settings.systemDescription"
            type="textarea"
            :rows="3"
            placeholder="基于错题共享和知识点溯源的学习平台"
          />
        </el-form-item>

        <el-divider content-position="left">功能设置</el-divider>

        <el-form-item label="允许用户注册">
          <el-switch v-model="settings.allowRegister" />
        </el-form-item>

        <el-form-item label="错题共享需审核">
          <el-switch v-model="settings.shareNeedApprove" />
        </el-form-item>

        <el-form-item label="资源上传需审核">
          <el-switch v-model="settings.resourceNeedApprove" />
        </el-form-item>

        <el-form-item label="最大文件大小">
          <el-input v-model="settings.maxFileSize" type="number">
            <template #append>MB</template>
          </el-input>
        </el-form-item>

        <el-divider content-position="left">学科管理</el-divider>

        <el-form-item label="支持的学科">
          <el-checkbox-group v-model="settings.subjects">
            <el-checkbox label="数学" />
            <el-checkbox label="语文" />
            <el-checkbox label="英语" />
            <el-checkbox label="物理" />
            <el-checkbox label="化学" />
            <el-checkbox label="生物" />
            <el-checkbox label="政治" />
            <el-checkbox label="历史" />
            <el-checkbox label="地理" />
          </el-checkbox-group>
        </el-form-item>

        <el-divider content-position="left">其他设置</el-divider>

        <el-form-item label="每页显示数量">
          <el-input-number v-model="settings.pageSize" :min="10" :max="100" :step="10" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveSettings">
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
          <el-button @click="resetSettings">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 系统维护 -->
    <el-card class="maintenance-card">
      <template #header>
        <div class="card-title">
          <el-icon><Operation /></el-icon>
          <span>系统维护</span>
        </div>
      </template>

      <el-space direction="vertical" :size="16" style="width: 100%">
        <el-alert
          title="数据备份"
          type="info"
          description="建议定期备份系统数据，以防数据丢失"
          :closable="false"
        >
          <template #default>
            <el-button type="primary" size="small" @click="backupData">
              <el-icon><Download /></el-icon>
              立即备份
            </el-button>
          </template>
        </el-alert>

        <el-alert
          title="清理缓存"
          type="warning"
          description="清理系统缓存可以释放存储空间，提升系统性能"
          :closable="false"
        >
          <template #default>
            <el-button type="warning" size="small" @click="clearCache">
              <el-icon><Delete /></el-icon>
              清理缓存
            </el-button>
          </template>
        </el-alert>

        <el-alert
          title="系统日志"
          type="success"
          description="查看系统运行日志，便于排查问题"
          :closable="false"
        >
          <template #default>
            <el-button type="success" size="small" @click="viewLogs">
              <el-icon><View /></el-icon>
              查看日志
            </el-button>
          </template>
        </el-alert>
      </el-space>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting,
  UserFilled,
  Postcard,
  Avatar,
  Document,
  Tools,
  Check,
  Operation,
  Download,
  Delete,
  View
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const systemStats = ref<any>({})

const settings = ref({
  systemName: '知错就改',
  systemDescription: '基于错题共享和知识点溯源的学习平台',
  allowRegister: true,
  shareNeedApprove: true,
  resourceNeedApprove: true,
  maxFileSize: 50,
  subjects: ['数学', '语文', '英语', '物理', '化学', '生物'],
  pageSize: 20
})

const fetchSystemStats = async () => {
  try {
    const res = await api.get('/api/stats/system')
    systemStats.value = res.data.data || {}
  } catch (error) {
    ElMessage.error('加载系统统计失败')
  }
}

const saveSettings = () => {
  ElMessage.success('设置已保存')
  console.log('Settings:', settings.value)
}

const resetSettings = () => {
  ElMessage.info('设置已重置')
}

const backupData = () => {
  ElMessage.success('数据备份功能开发中...')
}

const clearCache = () => {
  ElMessage.success('缓存清理功能开发中...')
}

const viewLogs = () => {
  ElMessage.info('日志查看功能开发中...')
}

onMounted(() => {
  fetchSystemStats()
})
</script>

<style scoped>
.overview-section {
  margin-bottom: 20px;
}

.overview-card {
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

.overview-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.overview-icon {
  font-size: 48px;
  opacity: 0.9;
}

.overview-content {
  flex: 1;
}

.overview-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  opacity: 0.9;
}

.settings-card,
.maintenance-card {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
