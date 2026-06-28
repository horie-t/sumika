import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { MonthlyTotal } from '../../api/reports'
import { yen } from './format'

interface Props {
  data: MonthlyTotal[]
}

/** 月別の収入/支出の推移（棒グラフ）。 */
export function MonthlyTrendChart({ data }: Props) {
  return (
    <section className="report-section">
      <h2>月別推移</h2>
      <div className="chart-container">
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" />
            <YAxis width={80} tickFormatter={(v) => yen.format(Number(v))} />
            <Tooltip formatter={(value) => yen.format(Number(value))} />
            <Legend />
            <Bar dataKey="income" name="収入" fill="#4fb286" radius={[6, 6, 0, 0]} />
            <Bar dataKey="expense" name="支出" fill="#f0876a" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
