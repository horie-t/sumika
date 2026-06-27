import { Routes, Route, NavLink } from 'react-router-dom'
import TransactionsPage from './features/transactions/TransactionsPage'
import CategoriesPage from './features/categories/CategoriesPage'

function App() {
  return (
    <>
      <nav className="nav">
        <NavLink to="/">収支一覧</NavLink>
        <NavLink to="/categories">カテゴリ管理</NavLink>
      </nav>
      <Routes>
        <Route path="/" element={<TransactionsPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
      </Routes>
    </>
  )
}

export default App
