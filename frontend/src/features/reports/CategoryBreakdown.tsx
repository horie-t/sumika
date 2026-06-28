import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { CategorySummaryLine } from '../../api/reports'
import { yen } from './format'

interface Props {
  categories: CategorySummaryLine[]
}

// 支出カテゴリ用の配色（Warm Kakeibo パレット、必要数を循環）。
const COLORS = ['#ff8a7a', '#4fb286', '#ffc466', '#7cc6e8', '#c8a8e0', '#f6a5b8', '#9ad0a5']

/** カテゴリ別内訳の表＋支出の円グラフ。 */
export function CategoryBreakdown({ categories }: Props) {
  const expenses = categories.filter((c) => c.type === 'EXPENSE')

  return (
    <section className="report-section">
      <h2>カテゴリ別内訳</h2>

      {categories.length === 0 ? (
        <p>データがありません</p>
      ) : (
        <>
          {expenses.length > 0 && (
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={expenses} dataKey="total" nameKey="categoryName" label>
                    {expenses.map((c, i) => (
                      <Cell key={c.categoryId} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => yen.format(Number(value))} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}

          <table className="data-table">
            <thead>
              <tr>
                <th>カテゴリ</th>
                <th>種別</th>
                <th className="amount">金額</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.categoryId}>
                  <td>{c.categoryName}</td>
                  <td>
                    <span className={`badge ${c.type === 'INCOME' ? 'income' : 'expense'}`}>
                      {c.type === 'INCOME' ? '収入' : '支出'}
                    </span>
                  </td>
                  <td className={`amount ${c.type === 'INCOME' ? 'pos' : 'neg'}`}>
                    {yen.format(c.total)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </section>
  )
}
