import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as categoriesApi from './categories'
import * as transactionsApi from './transactions'
import type { TransactionFilter } from './transactions'
import type { CategoryInput, TransactionInput } from './types'

export const queryKeys = {
  categories: ['categories'] as const,
  transactions: ['transactions'] as const,
  transactionList: (filter: TransactionFilter) => ['transactions', filter] as const,
}

// ---- categories ----

export function useCategories() {
  return useQuery({
    queryKey: queryKeys.categories,
    queryFn: categoriesApi.fetchCategories,
  })
}

export function useCreateCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CategoryInput) => categoriesApi.createCategory(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.categories }),
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: CategoryInput }) =>
      categoriesApi.updateCategory(id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.categories }),
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => categoriesApi.deleteCategory(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.categories }),
  })
}

// ---- transactions ----

export function useTransactions(filter: TransactionFilter = {}) {
  return useQuery({
    queryKey: queryKeys.transactionList(filter),
    queryFn: () => transactionsApi.fetchTransactions(filter),
  })
}

export function useCreateTransaction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: TransactionInput) => transactionsApi.createTransaction(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.transactions }),
  })
}

export function useUpdateTransaction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: TransactionInput }) =>
      transactionsApi.updateTransaction(id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.transactions }),
  })
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => transactionsApi.deleteTransaction(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.transactions }),
  })
}
