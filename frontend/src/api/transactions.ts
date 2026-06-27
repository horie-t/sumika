import { apiClient } from './client'
import type { Transaction, TransactionInput } from './types'

/** 収支記録の検索条件。未指定の項目は送信されない（絞り込みなし）。 */
export interface TransactionFilter {
  from?: string
  to?: string
  categoryId?: number
}

export async function fetchTransactions(filter: TransactionFilter = {}): Promise<Transaction[]> {
  const { data } = await apiClient.get<Transaction[]>('/transactions', { params: filter })
  return data
}

export async function createTransaction(input: TransactionInput): Promise<Transaction> {
  const { data } = await apiClient.post<Transaction>('/transactions', input)
  return data
}

export async function updateTransaction(id: number, input: TransactionInput): Promise<Transaction> {
  const { data } = await apiClient.put<Transaction>(`/transactions/${id}`, input)
  return data
}

export async function deleteTransaction(id: number): Promise<void> {
  await apiClient.delete(`/transactions/${id}`)
}
