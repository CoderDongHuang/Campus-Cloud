import axios from 'axios'
import type { ApiResult } from '../types/api'

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// 请求拦截器：自动带 Token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：401 自动刷新 Token
let isRefreshing = false
let refreshQueue: Array<(token: string) => void> = []

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    const { config, response } = error
    if (response?.status === 401 && !config._retry) {
      config._retry = true
      if (!isRefreshing) {
        isRefreshing = true
        try {
          const refreshToken = localStorage.getItem('refreshToken')
          const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
          const newToken = res.data.data.accessToken
          localStorage.setItem('accessToken', newToken)
          refreshQueue.forEach((cb) => cb(newToken))
          refreshQueue = []
          config.headers.Authorization = `Bearer ${newToken}`
          return http(config)
        } catch {
          localStorage.clear()
          window.location.href = '/login'
        } finally {
          isRefreshing = false
        }
      }
      return new Promise((resolve) => {
        refreshQueue.push((token: string) => {
          config.headers.Authorization = `Bearer ${token}`
          resolve(http(config))
        })
      })
    }
    return Promise.reject(error)
  },
)

export default http
