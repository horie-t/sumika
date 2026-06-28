import { Routes, Route, NavLink } from 'react-router-dom'
import { useAuth } from './auth/AuthProvider'
import TransactionsPage from './features/transactions/TransactionsPage'
import CategoriesPage from './features/categories/CategoriesPage'
import ReportsPage from './features/reports/ReportsPage'

function App() {
  const { username, logout } = useAuth()
  return (
    <>
      <nav className="nav">
        <span className="brand">
          <span className="brand-logo" aria-hidden="true">
            🏠
          </span>
          sumika
        </span>
        <NavLink to="/">
          <span className="deco" aria-hidden="true">
            📒
          </span>
          収支一覧
        </NavLink>
        <NavLink to="/categories">
          <span className="deco" aria-hidden="true">
            🏷️
          </span>
          カテゴリ管理
        </NavLink>
        <NavLink to="/reports">
          <span className="deco" aria-hidden="true">
            📊
          </span>
          集計・レポート
        </NavLink>
        <span className="nav-user">
          <span className="avatar" aria-hidden="true">
            🐥
          </span>
          {username}
          <button type="button" onClick={logout}>
            ログアウト
          </button>
        </span>
      </nav>
      <Routes>
        <Route path="/" element={<TransactionsPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/reports" element={<ReportsPage />} />
      </Routes>
    </>
  )
}

export default App
