import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { keycloak } from './keycloak'
import { setAccessToken } from './token'

interface AuthContextValue {
  username: string
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth は AuthProvider の内側で使用してください')
  return ctx
}

// React StrictMode の二重実行で keycloak.init() が二度呼ばれるのを防ぐ。
let initStarted = false

/**
 * Keycloak で未認証ならログイン画面へリダイレクトし、認証後にトークンを保持する。
 * 初期化が終わるまでは子を描画しない（全ルート保護）。
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false)
  const [username, setUsername] = useState('')

  useEffect(() => {
    if (initStarted) {
      setReady(true)
      return
    }
    initStarted = true

    keycloak.onTokenExpired = () => {
      void keycloak.updateToken(30).then(() => setAccessToken(keycloak.token))
    }

    keycloak
      .init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false })
      .then((authenticated) => {
        if (authenticated) {
          setAccessToken(keycloak.token)
          setUsername(keycloak.tokenParsed?.preferred_username ?? '')
        }
        setReady(true)
      })
      .catch(() => setReady(true))
  }, [])

  if (!ready) {
    return <div className="auth-loading">認証を確認中...</div>
  }

  return (
    <AuthContext.Provider value={{ username, logout: () => void keycloak.logout() }}>
      {children}
    </AuthContext.Provider>
  )
}
