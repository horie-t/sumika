import { test as setup, expect } from '@playwright/test'

const authFile = 'e2e/.auth/user.json'

/**
 * Keycloak のログイン画面で demo ユーザーとしてログインし、認証済みの storageState を保存する。
 * 本体のシナリオはこの storageState を使い、keycloak-js が SSO クッキーで無音再認証する。
 */
setup('Keycloak で demo としてログインする', async ({ page }) => {
  await page.goto('/')
  // 未認証なので Keycloak のログイン画面へリダイレクトされる。
  await page.locator('#username').fill('demo')
  await page.locator('#password').fill('demo')
  await page.locator('#kc-login').click()
  // アプリへ戻り、認証済み画面が表示される。
  await expect(page.getByRole('heading', { name: '収支一覧' })).toBeVisible()
  await page.context().storageState({ path: authFile })
})
