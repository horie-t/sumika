/** 円表示の共有フォーマッタ。 */
export const yen = new Intl.NumberFormat('ja-JP', { style: 'currency', currency: 'JPY' })

/** 当月（YYYY-MM）。 */
export function currentMonth(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

/** YYYY-MM を delta か月ずらす。 */
export function shiftMonth(month: string, delta: number): string {
  const [y, m] = month.split('-').map(Number)
  const d = new Date(y, m - 1 + delta, 1)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
