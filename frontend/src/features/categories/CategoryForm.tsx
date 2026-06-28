import { useState, type FormEvent } from 'react'
import type { Category, CategoryInput, EntryType } from '../../api/types'

interface Props {
  initial?: Category
  submitting?: boolean
  onSubmit: (input: CategoryInput) => void
  onCancel: () => void
}

export function CategoryForm({ initial, submitting, onSubmit, onCancel }: Props) {
  const [name, setName] = useState(initial?.name ?? '')
  const [type, setType] = useState<EntryType>(initial?.type ?? 'EXPENSE')
  const [error, setError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const trimmed = name.trim()
    if (!trimmed) {
      setError('カテゴリ名を入力してください')
      return
    }
    if (trimmed.length > 50) {
      setError('カテゴリ名は50文字以内で入力してください')
      return
    }
    setError(null)
    onSubmit({ name: trimmed, type })
  }

  return (
    <form className="transaction-form" onSubmit={handleSubmit}>
      <h2>{initial ? 'カテゴリを編集' : 'カテゴリを追加'}</h2>

      <div className="field">
        <label>
          名前
          <input
            type="text"
            value={name}
            maxLength={50}
            onChange={(e) => setName(e.target.value)}
          />
        </label>
        {error ? <span className="field-error">{error}</span> : null}
      </div>

      <div className="field">
        <label>
          種別
          <select value={type} onChange={(e) => setType(e.target.value as EntryType)}>
            <option value="EXPENSE">支出</option>
            <option value="INCOME">収入</option>
          </select>
        </label>
      </div>

      <div className="form-actions">
        <button type="submit" className="primary" disabled={submitting}>
          {initial ? '更新' : '追加'}
        </button>
        <button type="button" onClick={onCancel} disabled={submitting}>
          キャンセル
        </button>
      </div>
    </form>
  )
}
