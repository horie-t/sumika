import { useMemo, useState } from 'react'
import { useCategories, useTransactions } from '../../api/hooks'
import type { TransactionFilter } from '../../api/transactions'
import { TransactionFilters, type FilterValues } from './TransactionFilters'

const yen = new Intl.NumberFormat('ja-JP', { style: 'currency', currency: 'JPY' })

export default function TransactionsPage() {
  const [filterValues, setFilterValues] = useState<FilterValues>({
    from: '',
    to: '',
    categoryId: '',
  })

  const filter = useMemo<TransactionFilter>(() => {
    const next: TransactionFilter = {}
    if (filterValues.from) next.from = filterValues.from
    if (filterValues.to) next.to = filterValues.to
    if (filterValues.categoryId) next.categoryId = Number(filterValues.categoryId)
    return next
  }, [filterValues])

  const categoriesQuery = useCategories()
  const transactionsQuery = useTransactions(filter)

  const categoryName = useMemo(() => {
    const byId = new Map((categoriesQuery.data ?? []).map((c) => [c.id, c.name]))
    return (id?: number) => byId.get(id) ?? '-'
  }, [categoriesQuery.data])

  return (
    <main>
      <h1>収支一覧</h1>

      <TransactionFilters
        values={filterValues}
        categories={categoriesQuery.data ?? []}
        onChange={setFilterValues}
      />

      {transactionsQuery.isPending ? (
        <p>読み込み中...</p>
      ) : transactionsQuery.isError ? (
        <p role="alert">読み込みに失敗しました</p>
      ) : transactionsQuery.data.length === 0 ? (
        <p>データがありません</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>日付</th>
              <th>カテゴリ</th>
              <th>種別</th>
              <th className="amount">金額</th>
              <th>メモ</th>
            </tr>
          </thead>
          <tbody>
            {transactionsQuery.data.map((t) => (
              <tr key={t.id}>
                <td>{t.occurredOn}</td>
                <td>{categoryName(t.categoryId)}</td>
                <td>{t.type === 'INCOME' ? '収入' : '支出'}</td>
                <td className="amount">{yen.format(t.amount ?? 0)}</td>
                <td>{t.memo ?? ''}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}
