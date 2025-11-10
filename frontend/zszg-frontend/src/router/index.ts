import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Login from '@/views/auth/Login.vue'
import Register from '@/views/auth/Register.vue'
import MainLayout from '@/layouts/MainLayout.vue'

export type UserRole = 'ROLE_STUDENT' | 'ROLE_TEACHER' | 'ROLE_ADMIN'

function getToken() { return localStorage.getItem('token') }
function getRole(): UserRole | null { return localStorage.getItem('role') as UserRole | null }

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: Login, meta: { public: true } },
  { path: '/register', name: 'register', component: Register, meta: { public: true } },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue') },
      // student
      { path: 'student/errorbook', name: 'errorbook', component: () => import('@/views/student/ErrorBook.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/trace', name: 'trace', component: () => import('@/views/student/KnowledgeTrace.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/share', name: 'share', component: () => import('@/views/student/SharePool.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/recommend', name: 'recommend', component: () => import('@/views/student/Recommendations.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/my-classes', name: 's-my-classes', component: () => import('@/views/student/MyClasses.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/tasks', name: 's-tasks', component: () => import('@/views/student/Tasks.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'student/profile', name: 'profile', component: () => import('@/views/student/Profile.vue'), meta: { roles: ['ROLE_STUDENT'] } },
      // teacher
      { path: 'teacher/dashboard', name: 't-dashboard', component: () => import('@/views/teacher/Dashboard.vue'), meta: { roles: ['ROLE_TEACHER','ROLE_ADMIN'] } },
      { path: 'teacher/class-manage', name: 't-class-manage', component: () => import('@/views/teacher/ClassManage.vue'), meta: { roles: ['ROLE_TEACHER','ROLE_ADMIN'] } },
      { path: 'teacher/resources', name: 't-resources', component: () => import('@/views/teacher/Resources.vue'), meta: { roles: ['ROLE_TEACHER','ROLE_ADMIN'] } },
      { path: 'teacher/feedback', name: 't-feedback', component: () => import('@/views/teacher/Feedback.vue'), meta: { roles: ['ROLE_TEACHER','ROLE_ADMIN'] } },
      { path: 'teacher/task-publish', name: 't-task-publish', component: () => import('@/views/teacher/TaskPublish.vue'), meta: { roles: ['ROLE_TEACHER','ROLE_ADMIN'] } },
      // admin
      { path: 'admin/users', name: 'a-users', component: () => import('@/views/admin/Users.vue'), meta: { roles: ['ROLE_ADMIN'] } },
      { path: 'admin/questions', name: 'a-questions', component: () => import('@/views/admin/QuestionBank.vue'), meta: { roles: ['ROLE_ADMIN'] } },
      { path: 'admin/audit', name: 'a-audit', component: () => import('@/views/admin/Audit.vue'), meta: { roles: ['ROLE_ADMIN'] } },
      { path: 'admin/settings', name: 'a-settings', component: () => import('@/views/admin/Settings.vue'), meta: { roles: ['ROLE_ADMIN'] } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.meta.public) return next()
  const token = getToken()
  const role = getRole()
  if (!token || !role) return next({ name: 'login', query: { redirect: to.fullPath } })
  const allowRoles = to.meta.roles as UserRole[] | undefined
  if (!allowRoles) return next()
  if (allowRoles.includes(role)) return next()
  return next({ name: 'home' })
})

export default router



