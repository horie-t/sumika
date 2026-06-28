import { yen } from './format'

interface Props {
  totalIncome: number
  totalExpense: number
  net: number
}

/** 収入計・支出計・差引の 3 カード。 */
export function ReportSummaryCards({ totalIncome, totalExpense, net }: Props) {
  return (
    <div className="report-summary">
      <div className="summary-card income">
        <span className="summary-label">収入</span>
        <span className="summary-value">{yen.format(totalIncome)}</span>
      </div>
      <div className="summary-card expense">
        <span className="summary-label">支出</span>
        <span className="summary-value">{yen.format(totalExpense)}</span>
      </div>
      <div className="summary-card net">
        <span className="summary-label">差引</span>
        <span className="summary-value">{yen.format(net)}</span>
      </div>
    </div>
  )
}
