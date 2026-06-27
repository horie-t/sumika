import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { TransactionForm } from './TransactionForm'
import type { Category } from '../../api/types'

const categories: Category[] = [{ id: 1, name: '食費', type: 'EXPENSE' }]

describe('TransactionForm', () => {
  it('入力値を onSubmit に渡す', () => {
    const onSubmit = vi.fn()
    render(<TransactionForm categories={categories} onSubmit={onSubmit} onCancel={() => {}} />)

    fireEvent.change(screen.getByLabelText('金額'), { target: { value: '1500' } })
    fireEvent.change(screen.getByLabelText('カテゴリ'), { target: { value: '1' } })
    fireEvent.change(screen.getByLabelText('日付'), { target: { value: '2026-06-28' } })
    fireEvent.change(screen.getByLabelText('メモ'), { target: { value: 'ランチ' } })
    fireEvent.click(screen.getByRole('button', { name: '登録' }))

    expect(onSubmit).toHaveBeenCalledWith({
      type: 'EXPENSE',
      amount: 1500,
      categoryId: 1,
      occurredOn: '2026-06-28',
      memo: 'ランチ',
    })
  })

  it('不正な入力ではエラーを表示し送信しない', () => {
    const onSubmit = vi.fn()
    render(<TransactionForm categories={categories} onSubmit={onSubmit} onCancel={() => {}} />)

    fireEvent.click(screen.getByRole('button', { name: '登録' }))

    expect(screen.getByText('金額は正の整数で入力してください')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })
})
