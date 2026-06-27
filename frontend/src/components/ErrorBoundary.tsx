import { Component, type ErrorInfo, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
}

/** 想定外のレンダリングエラーを捕捉し、フォールバック UI を表示する。 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error:', error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <main>
          <h1>エラーが発生しました</h1>
          <p>お手数ですが、画面を再読み込みしてください。</p>
        </main>
      )
    }
    return this.props.children
  }
}
