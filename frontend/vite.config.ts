import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 開発時は /api を backend(:8080) へプロキシし、ブラウザからは同一オリジン扱いにする
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    // Vitest（単体）は src 配下のみ。Playwright E2E（e2e/ と生成物 .features-gen/）は対象外。
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    exclude: ['node_modules', 'dist', 'e2e', '.features-gen'],
  },
})
