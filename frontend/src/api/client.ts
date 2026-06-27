import axios from 'axios'

/**
 * backend API への共有 axios インスタンス。
 * ベース URL は環境変数 `VITE_API_BASE_URL`（未設定時はローカル backend）。
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})
