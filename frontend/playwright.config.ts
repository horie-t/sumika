import { defineConfig, devices } from '@playwright/test'
import { defineBddConfig } from 'playwright-bdd'

// 日本語 Gherkin (.feature) から Playwright のテストを生成する。
// 生成物は .features-gen/（gitignore 済み）。`npm run e2e` が bddgen → playwright test を実行。
const bddTestDir = defineBddConfig({
  outputDir: '.features-gen',
  features: 'e2e/features/**/*.feature',
  steps: 'e2e/steps/**/*.ts',
})

const FRONTEND_URL = process.env.E2E_BASE_URL ?? 'http://localhost:5173'
const KEYCLOAK_URL = process.env.E2E_KEYCLOAK_URL ?? 'http://localhost:8081'

export default defineConfig({
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
  projects: [
    // 1) Keycloak にログインして storageState を作る。
    { name: 'setup', testDir: 'e2e', testMatch: /auth\.setup\.ts/ },
    // 2) BDD シナリオ本体。認証済み storageState を使う。
    {
      name: 'chromium',
      testDir: bddTestDir,
      dependencies: ['setup'],
      use: { ...devices['Desktop Chrome'], storageState: 'e2e/.auth/user.json' },
    },
  ],
  // ローカルでは起動中の dev server を再利用。CI では Playwright が Vite を起動する。
  // backend(:8080) / Postgres / Keycloak(:8081) は前提（ローカルは手動 / CI は別ステップで起動）。
  webServer: {
    command: 'npm run dev',
    url: FRONTEND_URL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: {
      VITE_KEYCLOAK_URL: KEYCLOAK_URL,
      VITE_KEYCLOAK_REALM: 'sumika',
      VITE_KEYCLOAK_CLIENT_ID: 'sumika-frontend',
    },
  },
})
