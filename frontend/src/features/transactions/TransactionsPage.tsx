import { useMemo, useState } from 'react'
import {
  useCategories,
  useCreateTransaction,
  useDeleteTransaction,
  useTransactions,
  useUpdateTransaction,
} from '../../api/hooks'
import { apiErrorMessage } from '../../api/errors'
import { useToast } from '../../components/toast-context'
import type { TransactionFilter } from '../../api/transactions'
import type { Transaction, TransactionInput } from '../../api/types'
import { TransactionFilters, type FilterValues } from './TransactionFilters'
import { TransactionForm } from './TransactionForm'

const yen = new Intl.NumberFormat('ja-JP', { style: 'currency', currency: 'JPY' })

export default function TransactionsPage() {
  const [filterValues, setFilterValues] = useState<FilterValues>({
    from: '',
    to: '',
    categoryId: '',
  })
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Transaction | null>(null)

  const filter = useMemo<TransactionFilter>(() => {
    const next: TransactionFilter = {}
    if (filterValues.from) next.from = filterValues.from
    if (filterValues.to) next.to = filterValues.to
    if (filterValues.categoryId) next.categoryId = Number(filterValues.categoryId)
    return next
  }, [filterValues])

  const { showToast } = useToast()
  const categoriesQuery = useCategories()
  const transactionsQuery = useTransactions(filter)
  const createTransaction = useCreateTransaction()
  const updateTransaction = useUpdateTransaction()
  const deleteTransaction = useDeleteTransaction()

  const categoryName = useMemo(() => {
    const byId = new Map((categoriesQuery.data ?? []).map((c) => [c.id, c.name]))
    return (id?: number) => byId.get(id) ?? '-'
  }, [categoriesQuery.data])

  function closeForm() {
    setFormOpen(false)
    setEditing(null)
    createTransaction.reset()
    updateTransaction.reset()
  }

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(transaction: Transaction) {
    setEditing(transaction)
    setFormOpen(true)
  }

  function handleSubmit(input: TransactionInput) {
    if (editing?.id != null) {
      updateTransaction.mutate(
        { id: editing.id, input },
        {
          onSuccess: () => {
            showToast('収支を更新しました')
            closeForm()
          },
          onError: (error) => showToast(apiErrorMessage(error), 'error'),
        },
      )
    } else {
      createTransaction.mutate(input, {
        onSuccess: () => {
          showToast('収支を登録しました')
          closeForm()
        },
        onError: (error) => showToast(apiErrorMessage(error), 'error'),
      })
    }
  }

  function handleDelete(transaction: Transaction) {
    if (transaction.id == null) return
    if (window.confirm('この収支記録を削除しますか？')) {
      deleteTransaction.mutate(transaction.id, {
        onSuccess: () => showToast('収支を削除しました'),
        onError: (error) => showToast(apiErrorMessage(error, '削除に失敗しました'), 'error'),
      })
    }
  }

  return (
    <main>
      <h1>
        <span className="deco" aria-hidden="true">
          📒
        </span>
        収支一覧
      </h1>

      {formOpen ? (
        <TransactionForm
          categories={categoriesQuery.data ?? []}
          initial={editing ?? undefined}
          submitting={createTransaction.isPending || updateTransaction.isPending}
          onSubmit={handleSubmit}
          onCancel={closeForm}
        />
      ) : (
        <button type="button" className="primary" onClick={openCreate}>
          新規登録
        </button>
      )}

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
        <div className="table-card">
          <table className="data-table">
            <thead>
              <tr>
                <th>日付</th>
                <th>カテゴリ</th>
                <th>種別</th>
                <th className="amount">金額</th>
                <th>メモ</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {transactionsQuery.data.map((t) => (
                <tr key={t.id}>
                  <td>{t.occurredOn}</td>
                  <td>{categoryName(t.categoryId)}</td>
                  <td>
                    <span className={`badge ${t.type === 'INCOME' ? 'income' : 'expense'}`}>
                      {t.type === 'INCOME' ? '収入' : '支出'}
                    </span>
                  </td>
                  <td className={`amount ${t.type === 'INCOME' ? 'pos' : 'neg'}`}>
                    {yen.format(t.amount ?? 0)}
                  </td>
                  <td>{t.memo ?? ''}</td>
                  <td>
                    <span className="row-actions">
                      <button type="button" onClick={() => openEdit(t)}>
                        編集
                      </button>
                      <button type="button" className="danger" onClick={() => handleDelete(t)}>
                        削除
                      </button>
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  )
}
