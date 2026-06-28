import { defineConfig, devices } from '@playwright/test'
import { defineBddConfig } from 'playwright-bdd'

// 日本語 Gherkin (.feature) から Playwright のテストを生成する。
// 生成物は .features-gen/（gitignore 済み）。`npm run e2e` が bddgen → playwright test を実行。
const testDir = defineBddConfig({
  outputDir: '.features-gen',
  features: 'e2e/features/**/*.feature',
  steps: 'e2e/steps/**/*.ts',
})

const FRONTEND_URL = process.env.E2E_BASE_URL ?? 'http://localhost:5173'

export default defineConfig({
  testDir,
  // フルスタック E2E は DB を共有するため、シナリオ間の競合を避けて直列実行する。
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: FRONTEND_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  // ローカルでは起動中の dev server を再利用。CI では Playwright が Vite を起動する。
  // backend(:8080) と Postgres は前提（ローカルは手動 / CI は別ステップで起動）。
  webServer: {
    command: 'npm run dev',
    url: FRONTEND_URL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
})
