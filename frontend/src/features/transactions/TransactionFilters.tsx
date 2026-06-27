import type { Category } from '../../api/types'

export interface FilterValues {
  from: string
  to: string
  categoryId: string
}

interface Props {
  values: FilterValues
  categories: Category[]
  onChange: (values: FilterValues) => void
}

export function TransactionFilters({ values, categories, onChange }: Props) {
  return (
    <div className="filters">
      <label>
        開始日
        <input
          type="date"
          value={values.from}
          onChange={(e) => onChange({ ...values, from: e.target.value })}
        />
      </label>
      <label>
        終了日
        <input
          type="date"
          value={values.to}
          onChange={(e) => onChange({ ...values, to: e.target.value })}
        />
      </label>
      <label>
        カテゴリ
        <select
          value={values.categoryId}
          onChange={(e) => onChange({ ...values, categoryId: e.target.value })}
        >
          <option value="">すべて</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
