import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ToastProvider } from '../../components/ToastProvider'
import TransactionsPage from './TransactionsPage'

vi.mock('../../api/transactions', () => ({
  fetchTransactions: vi.fn().mockResolvedValue([
    {
      id: 1,
      type: 'EXPENSE',
      amount: 1200,
      categoryId: 1,
      occurredOn: '2026-06-27',
      memo: 'ランチ',
    },
  ]),
  createTransaction: vi.fn(),
  updateTransaction: vi.fn(),
  deleteTransaction: vi.fn(),
}))

vi.mock('../../api/categories', () => ({
  fetchCategories: vi.fn().mockResolvedValue([{ id: 1, name: '食費', type: 'EXPENSE' }]),
  createCategory: vi.fn(),
  updateCategory: vi.fn(),
  deleteCategory: vi.fn(),
}))

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <TransactionsPage />
      </ToastProvider>
    </QueryClientProvider>,
  )
}

describe('TransactionsPage', () => {
  it('取得した収支一覧を表示する', async () => {
    renderPage()

    expect(await screen.findByText('ランチ')).toBeInTheDocument()
    expect(screen.getByText('￥1,200')).toBeInTheDocument()
  })
})
