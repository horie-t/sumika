import { useState, type FormEvent } from 'react'
import type { Category, EntryType, Transaction, TransactionInput } from '../../api/types'

interface Props {
  categories: Category[]
  initial?: Transaction
  submitting?: boolean
  errorMessage?: string
  onSubmit: (input: TransactionInput) => void
  onCancel: () => void
}

interface FormState {
  type: EntryType
  amount: string
  categoryId: string
  occurredOn: string
  memo: string
}

function toFormState(initial?: Transaction): FormState {
  return {
    type: initial?.type ?? 'EXPENSE',
    amount: initial?.amount != null ? String(initial.amount) : '',
    categoryId: initial?.categoryId != null ? String(initial.categoryId) : '',
    occurredOn: initial?.occurredOn ?? '',
    memo: initial?.memo ?? '',
  }
}

export function TransactionForm({
  categories,
  initial,
  submitting,
  errorMessage,
  onSubmit,
  onCancel,
}: Props) {
  const [form, setForm] = useState<FormState>(() => toFormState(initial))
  const [errors, setErrors] = useState<Record<string, string>>({})

  // 収支種別はカテゴリ種別と一致する必要があるため、選択中の種別でカテゴリを絞る
  const categoryOptions = categories.filter((c) => c.type === form.type)

  function update<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => {
      const next = { ...prev, [key]: value }
      if (key === 'type') {
        const stillValid = categories.some(
          (c) => String(c.id) === prev.categoryId && c.type === value,
        )
        if (!stillValid) next.categoryId = ''
      }
      return next
    })
  }

  function validate(): boolean {
    const nextErrors: Record<string, string> = {}
    const amount = Number(form.amount)
    if (!form.amount || !Number.isInteger(amount) || amount <= 0) {
      nextErrors.amount = '金額は正の整数で入力してください'
    }
    if (!form.categoryId) {
      nextErrors.categoryId = 'カテゴリを選択してください'
    }
    if (!form.occurredOn) {
      nextErrors.occurredOn = '日付を入力してください'
    }
    if (form.memo.length > 255) {
      nextErrors.memo = 'メモは255文字以内で入力してください'
    }
    setErrors(nextErrors)
    return Object.keys(nextErrors).length === 0
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!validate()) return
    const memo = form.memo.trim()
    onSubmit({
      type: form.type,
      amount: Number(form.amount),
      categoryId: Number(form.categoryId),
      occurredOn: form.occurredOn,
      memo: memo === '' ? undefined : memo,
    })
  }

  return (
    <form className="transaction-form" onSubmit={handleSubmit}>
      <h2>{initial ? '収支を編集' : '収支を登録'}</h2>
      {errorMessage ? (
        <p role="alert" className="form-error">
          {errorMessage}
        </p>
      ) : null}

      <div className="field">
        <label>
          種別
          <select value={form.type} onChange={(e) => update('type', e.target.value as EntryType)}>
            <option value="EXPENSE">支出</option>
            <option value="INCOME">収入</option>
          </select>
        </label>
      </div>

      <div className="field">
        <label>
          金額
          <input
            type="number"
            min="1"
            value={form.amount}
            onChange={(e) => update('amount', e.target.value)}
          />
        </label>
        {errors.amount ? <span className="field-error">{errors.amount}</span> : null}
      </div>

      <div className="field">
        <label>
          カテゴリ
          <select value={form.categoryId} onChange={(e) => update('categoryId', e.target.value)}>
            <option value="">選択してください</option>
            {categoryOptions.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        {errors.categoryId ? <span className="field-error">{errors.categoryId}</span> : null}
      </div>

      <div className="field">
        <label>
          日付
          <input
            type="date"
            value={form.occurredOn}
            onChange={(e) => update('occurredOn', e.target.value)}
          />
        </label>
        {errors.occurredOn ? <span className="field-error">{errors.occurredOn}</span> : null}
      </div>

      <div className="field">
        <label>
          メモ
          <input
            type="text"
            value={form.memo}
            maxLength={255}
            onChange={(e) => update('memo', e.target.value)}
          />
        </label>
        {errors.memo ? <span className="field-error">{errors.memo}</span> : null}
      </div>

      <div className="form-actions">
        <button type="submit" disabled={submitting}>
          {initial ? '更新' : '登録'}
        </button>
        <button type="button" onClick={onCancel} disabled={submitting}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
