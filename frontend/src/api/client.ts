import axios from 'axios'
import { getAccessToken } from '../auth/token'

/**
 * backend API への共有 axios インスタンス。
 * ベース URL は既定で相対 `/api`（dev では Vite proxy 経由で backend へ）。
 * 別オリジン配信時は環境変数 `VITE_API_BASE_URL` で上書きする。
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
})

// 認証済みなら Keycloak のアクセストークンを Bearer で付与する。
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
