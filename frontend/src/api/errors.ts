/** API エラー（ProblemDetail）から表示用メッセージを取り出す。 */
export function apiErrorMessage(error: unknown, fallback = '保存に失敗しました'): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { detail?: string } } }).response
    if (response?.data?.detail) return response.data.detail
  }
  return fallback
}
