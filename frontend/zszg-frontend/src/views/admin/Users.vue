<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><User /></el-icon>
        用户管理
      </h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 筛选器 -->
    <el-card shadow="never" class="filter-card">
      <el-space wrap>
        <el-select v-model="filters.role" placeholder="角色" clearable>
          <el-option label="学生" value="ROLE_STUDENT" />
          <el-option label="教师" value="ROLE_TEACHER" />
          <el-option label="管理员" value="ROLE_ADMIN" />
        </el-select>
        
        <el-select v-model="filters.status" placeholder="状态" clearable>
          <el-option label="正常" value="ACTIVE" />
          <el-option label="冻结" value="FROZEN" />
        </el-select>

        <el-button type="primary" @click="fetchUsers">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetFilters">重置</el-button>
      </el-space>
    </el-card>

    <!-- 用户列表 -->
    <el-card v-loading="loading">
      <el-table :data="users" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="classId" label="班级" width="100" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" @click="editUser(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 'ACTIVE' ? '冻结' : '激活' }}
              </el-button>
              <el-button size="small" type="danger" @click="deleteUser(row.id)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingUser ? '编辑用户' : '添加用户'"
      width="600px"
    >
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input
            v-model="userForm.username"
            :disabled="!!editingUser"
            placeholder="请输入用户名"
          />
        </el-form-item>

        <el-form-item label="密码" :required="!editingUser">
          <el-input
            v-model="userForm.password"
            type="password"
            :placeholder="editingUser ? '不修改请留空' : '请输入密码'"
          />
        </el-form-item>

        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="角色" required>
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="学生" value="ROLE_STUDENT" />
            <el-option label="教师" value="ROLE_TEACHER" />
            <el-option label="管理员" value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="班级">
          <el-input v-model="userForm.classId" placeholder="请输入班级" />
        </el-form-item>

        <el-form-item label="年级">
          <el-input v-model="userForm.grade" placeholder="请输入年级" />
        </el-form-item>

        <el-form-item label="电话">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item v-if="editingUser" label="状态">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="冻结" value="FROZEN" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Plus,
  Search
} from '@element-plus/icons-vue'
import api from '@/lib/api'

const loading = ref(false)
const showCreateDialog = ref(false)
const users = ref<any[]>([])
const editingUser = ref<any>(null)

const filters = ref({
  role: '',
  status: ''
})

const userForm = ref({
  username: '',
  password: '',
  realName: '',
  role: '',
  classId: '',
  grade: '',
  phone: '',
  email: '',
  status: 'ACTIVE'
})

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'ROLE_STUDENT': '学生',
    'ROLE_TEACHER': '教师',
    'ROLE_ADMIN': '管理员'
  }
  return map[role] || role
}

const getRoleType = (role: string) => {
  const map: Record<string, any> = {
    'ROLE_STUDENT': 'primary',
    'ROLE_TEACHER': 'success',
    'ROLE_ADMIN': 'danger'
  }
  return map[role] || 'info'
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.role) params.role = filters.value.role
    if (filters.value.status) params.status = filters.value.status
    
    const res = await api.get('/api/admin/users', { params })
    users.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { role: '', status: '' }
  fetchUsers()
}

const editUser = (user: any) => {
  editingUser.value = user
  userForm.value = {
    username: user.username,
    password: '',
    realName: user.realName || '',
    role: user.role,
    classId: user.classId || '',
    grade: user.grade || '',
    phone: user.phone || '',
    email: user.email || '',
    status: user.status
  }
  showCreateDialog.value = true
}

const submitUser = async () => {
  if (!userForm.value.username || !userForm.value.role) {
    ElMessage.warning('请填写必填项')
    return
  }

  if (!editingUser.value && !userForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  try {
    if (editingUser.value) {
      await api.put(`/api/admin/users/${editingUser.value.id}`, userForm.value)
      ElMessage.success('更新成功')
    } else {
      await api.post('/api/admin/users', userForm.value)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    fetchUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleStatus = async (user: any) => {
  try {
    await api.post(`/api/admin/users/${user.id}/toggle-status`)
    ElMessage.success('状态已更新')
    fetchUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/api/admin/users/${id}`)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  userForm.value = {
    username: '',
    password: '',
    realName: '',
    role: '',
    classId: '',
    grade: '',
    phone: '',
    email: '',
    status: 'ACTIVE'
  }
  editingUser.value = null
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-card {
  margin-bottom: 20px;
}
</style>
