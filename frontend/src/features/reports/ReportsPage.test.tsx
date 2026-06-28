import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ReportsPage from './ReportsPage'

vi.mock('../../api/reports', () => ({
  fetchMonthlySummary: vi.fn().mockResolvedValue({
    month: '2026-06',
    totalIncome: 300000,
    totalExpense: 2000,
    net: 298000,
    categories: [
      { categoryId: 3, categoryName: '給与', type: 'INCOME', total: 300000 },
      { categoryId: 2, categoryName: '食費', type: 'EXPENSE', total: 2000 },
    ],
  }),
  fetchMonthlyTrend: vi.fn().mockResolvedValue([
    { month: '2026-05', income: 0, expense: 0 },
    { month: '2026-06', income: 300000, expense: 2000 },
  ]),
}))

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <ReportsPage />
    </QueryClientProvider>,
  )
}

describe('ReportsPage', () => {
  it('サマリと差引・カテゴリ別内訳を表示する', async () => {
    renderPage()
    expect(screen.getByRole('heading', { name: '集計・レポート' })).toBeInTheDocument()
    // 差引（net）は一意の金額なので存在検証に使う
    expect(await screen.findByText('￥298,000')).toBeInTheDocument()
    // カテゴリ別内訳の行
    expect(screen.getByText('給与')).toBeInTheDocument()
    expect(screen.getByText('食費')).toBeInTheDocument()
  })
})
