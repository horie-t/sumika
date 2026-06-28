import '@testing-library/jest-dom/vitest'

// recharts の ResponsiveContainer は ResizeObserver を使うが jsdom には無いため最小実装を入れる。
class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
globalThis.ResizeObserver = globalThis.ResizeObserver ?? (ResizeObserverStub as never)
