import { useIsFetching, useIsMutating } from '@tanstack/react-query'

/** 取得・更新の進行中にグローバルなインジケータを表示する。 */
export function GlobalLoadingIndicator() {
  const active = useIsFetching() + useIsMutating()
  if (active === 0) return null
  return (
    <div className="global-loading" role="status" aria-live="polite">
      通信中...
    </div>
  )
}
