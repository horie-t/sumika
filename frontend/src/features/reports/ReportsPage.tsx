import { useMemo, useState } from 'react'
import { useMonthlySummary, useMonthlyTrend } from '../../api/hooks'
import { CategoryBreakdown } from './CategoryBreakdown'
import { MonthlyTrendChart } from './MonthlyTrendChart'
import { ReportSummaryCards } from './ReportSummaryCards'
import { currentMonth, shiftMonth } from './format'

export default function ReportsPage() {
  const [month, setMonth] = useState<string>(() => currentMonth())

  // 月別推移は選択月までの直近 12 か月。
  const trendRange = useMemo(() => ({ from: shiftMonth(month, -11), to: month }), [month])

  const summaryQuery = useMonthlySummary(month)
  const trendQuery = useMonthlyTrend(trendRange.from, trendRange.to)

  return (
    <main>
      <h1>
        <span className="deco" aria-hidden="true">
          📊
        </span>
        集計・レポート
      </h1>

      <div className="filters">
        <label>
          対象月
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
        </label>
      </div>

      {summaryQuery.isPending ? (
        <p>読み込み中...</p>
      ) : summaryQuery.isError ? (
        <p role="alert">読み込みに失敗しました</p>
      ) : (
        <>
          <ReportSummaryCards
            totalIncome={summaryQuery.data.totalIncome}
            totalExpense={summaryQuery.data.totalExpense}
            net={summaryQuery.data.net}
          />
          <CategoryBreakdown categories={summaryQuery.data.categories} />
        </>
      )}

      {trendQuery.isPending ? (
        <p>読み込み中...</p>
      ) : trendQuery.isError ? (
        <p role="alert">読み込みに失敗しました</p>
      ) : (
        <MonthlyTrendChart data={trendQuery.data} />
      )}
    </main>
  )
}
