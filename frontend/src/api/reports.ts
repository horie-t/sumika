import { apiClient } from './client'
import type { EntryType } from './types'

// backend の OpenAPI（components.schemas）に対応する集計レスポンス。
// 生成型は全フィールド optional のため、実際の形に合わせた明示的な型を用いる。

export interface CategorySummaryLine {
  categoryId: number
  categoryName: string
  type: EntryType
  total: number
}

export interface MonthlySummary {
  month: string
  totalIncome: number
  totalExpense: number
  net: number
  categories: CategorySummaryLine[]
}

export interface MonthlyTotal {
  month: string
  income: number
  expense: number
}

/** 選択月（YYYY-MM）の収支サマリ＋カテゴリ別内訳。 */
export async function fetchMonthlySummary(month: string): Promise<MonthlySummary> {
  const { data } = await apiClient.get<MonthlySummary>('/reports/monthly-summary', {
    params: { month },
  })
  return data
}

/** from〜to（各 YYYY-MM）の月別推移。 */
export async function fetchMonthlyTrend(from: string, to: string): Promise<MonthlyTotal[]> {
  const { data } = await apiClient.get<MonthlyTotal[]>('/reports/monthly-trend', {
    params: { from, to },
  })
  return data
}
