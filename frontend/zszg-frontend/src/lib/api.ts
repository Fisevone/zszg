import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000 // 30秒超时
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    
    // 为所有非登录注册的请求添加Token
    const isAuthEndpoint = config.url?.includes('/api/auth/login') || config.url?.includes('/api/auth/register')
    
    if (token && !isAuthEndpoint) {
      config.headers.Authorization = `Bearer ${token}`
      // 减少日志输出，避免控制台刷屏
      // console.log('🔑 添加Token到请求头 - URL:', config.url)
    }
    
    return config
  },
  (error) => {
    console.error('❌ 请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 成功响应直接返回
    return response
  },
  (error) => {
    const url = error.config?.url || ''
    const status = error.response?.status
    
    console.error('❌ API请求失败:', {
      url: url,
      status: status,
      message: error.response?.data?.message || error.message
    })
    
    // 处理401/403错误 - 不自动退出登录
    if (status === 401 || status === 403) {
      console.warn('⚠️ 认证错误 - 状态码:', status, 'URL:', url)
      
      // 检查是否是登录相关的接口
      const isLoginEndpoint = url.includes('/api/auth/login') || url.includes('/api/auth/register')
      
      if (isLoginEndpoint) {
        // 登录接口的401/403是正常的（密码错误等），不做任何处理
        console.log('💡 登录接口错误（用户名或密码错误），这是正常的')
      } else {
        // 其他接口的401/403：只记录日志，不退出登录
        console.warn('⚠️ 接口返回403，但不会自动退出登录')
        console.warn('💡 提示：如果持续出现此错误，请检查后端是否已重启')
        
        // 显示友好的错误提示（而不是强制退出）
        const errorMessage = error.response?.data?.message || '权限不足或请求失败'
        console.warn(`📝 错误信息: ${errorMessage}`)
      }
    }
    
    return Promise.reject(error)
  }
)

// 添加Token验证工具函数
export function isTokenValid(): boolean {
  const token = localStorage.getItem('token')
  if (!token) {
    console.warn('⚠️ Token不存在')
    return false
  }
  
  try {
    // 解析JWT Token（不验证签名，只检查结构和过期时间）
    const parts = token.split('.')
    if (parts.length !== 3) {
      console.warn('⚠️ Token格式错误')
      return false
    }
    
    const payload = JSON.parse(atob(parts[1]))
    const exp = payload.exp * 1000 // 转换为毫秒
    const now = Date.now()
    
    if (exp < now) {
      console.warn('⚠️ Token已过期')
      return false
    }
    
    const remainingMs = exp - now
    const remainingDays = Math.floor(remainingMs / (1000 * 60 * 60 * 24))
    console.log(`✅ Token有效 - 剩余 ${remainingDays} 天`)
    return true
  } catch (e) {
    console.error('❌ Token解析失败:', e)
    return false
  }
}

// 清除认证信息
export function clearAuth() {
  console.log('🧹 清除所有认证信息')
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('user')
}

export default api



