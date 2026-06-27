import { useState } from 'react'
import { apiErrorMessage } from '../../api/errors'
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
} from '../../api/hooks'
import type { Category, CategoryInput } from '../../api/types'
import { CategoryForm } from './CategoryForm'

export default function CategoriesPage() {
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Category | null>(null)

  const categoriesQuery = useCategories()
  const createCategory = useCreateCategory()
  const updateCategory = useUpdateCategory()
  const deleteCategory = useDeleteCategory()

  function closeForm() {
    setFormOpen(false)
    setEditing(null)
    createCategory.reset()
    updateCategory.reset()
  }

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(category: Category) {
    setEditing(category)
    setFormOpen(true)
  }

  function handleSubmit(input: CategoryInput) {
    if (editing?.id != null) {
      updateCategory.mutate({ id: editing.id, input }, { onSuccess: closeForm })
    } else {
      createCategory.mutate(input, { onSuccess: closeForm })
    }
  }

  function handleDelete(category: Category) {
    if (category.id == null) return
    if (window.confirm(`カテゴリ「${category.name}」を削除しますか？`)) {
      deleteCategory.mutate(category.id)
    }
  }

  const activeError = editing ? updateCategory.error : createCategory.error

  return (
    <main>
      <h1>カテゴリ管理</h1>

      {formOpen ? (
        <CategoryForm
          initial={editing ?? undefined}
          submitting={createCategory.isPending || updateCategory.isPending}
          errorMessage={activeError ? apiErrorMessage(activeError) : undefined}
          onSubmit={handleSubmit}
          onCancel={closeForm}
        />
      ) : (
        <button type="button" onClick={openCreate}>
          新規追加
        </button>
      )}

      {deleteCategory.isError ? (
        <p role="alert" className="form-error">
          {apiErrorMessage(deleteCategory.error, '削除に失敗しました')}
        </p>
      ) : null}

      {categoriesQuery.isPending ? (
        <p>読み込み中...</p>
      ) : categoriesQuery.isError ? (
        <p role="alert">読み込みに失敗しました</p>
      ) : categoriesQuery.data.length === 0 ? (
        <p>カテゴリがありません</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>名前</th>
              <th>種別</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {categoriesQuery.data.map((category) => (
              <tr key={category.id}>
                <td>{category.name}</td>
                <td>{category.type === 'INCOME' ? '収入' : '支出'}</td>
                <td>
                  <button type="button" onClick={() => openEdit(category)}>
                    編集
                  </button>
                  <button type="button" onClick={() => handleDelete(category)}>
                    削除
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}
