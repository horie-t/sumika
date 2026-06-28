import { expect } from '@playwright/test'
import { When, Then } from './fixtures'

// ===== ナビゲーション =====

When(/^収支一覧を開く$/, async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: '収支一覧' })).toBeVisible()
})

When(/^カテゴリ管理を開く$/, async ({ page }) => {
  await page.goto('/categories')
  await expect(page.getByRole('heading', { name: 'カテゴリ管理' })).toBeVisible()
})

// ===== ボタン操作 =====

// 単一ボタン（新規登録/新規追加/登録/更新/追加/キャンセル 等）。
// exact:true で "登録" が "新規登録" に誤マッチしないようにする。
When(/^"(.+)" を押す$/, async ({ page }, name: string) => {
  await page.getByRole('button', { name, exact: true }).first().click()
})

// 一覧の最初の行に対する操作（編集/削除）。
When(/^一覧の最初の行の "(.+)" を押す$/, async ({ page }, name: string) => {
  await page
    .locator('.data-table tbody tr')
    .first()
    .getByRole('button', { name, exact: true })
    .click()
})

// 次に出る window.confirm を承諾する（削除の確認ダイアログ用）。
When(/^確認ダイアログを承諾する$/, async ({ page }) => {
  page.once('dialog', (dialog) => dialog.accept())
})

// ===== フォーム入力（.transaction-form にスコープして filters の同名ラベルと区別） =====

function form(pageLocator: import('@playwright/test').Page) {
  return pageLocator.locator('.transaction-form')
}

When(/^種別に "(.+)" を選ぶ$/, async ({ page }, label: string) => {
  await form(page).getByLabel('種別').selectOption({ label })
})

When(/^金額に "(.+)" を入力する$/, async ({ page }, value: string) => {
  await form(page).getByLabel('金額').fill(value)
})

When(/^カテゴリに "(.+)" を選ぶ$/, async ({ page }, label: string) => {
  await form(page).getByLabel('カテゴリ').selectOption({ label })
})

When(/^日付に "(.+)" を入力する$/, async ({ page }, value: string) => {
  await form(page).getByLabel('日付').fill(value)
})

When(/^メモに "(.+)" を入力する$/, async ({ page }, value: string) => {
  await form(page).getByLabel('メモ').fill(value)
})

When(/^名前に "(.+)" を入力する$/, async ({ page }, value: string) => {
  await form(page).getByLabel('名前').fill(value)
})

// ===== フィルタ =====

When(/^フィルタのカテゴリに "(.+)" を選ぶ$/, async ({ page }, label: string) => {
  await page.locator('.filters').getByLabel('カテゴリ').selectOption({ label })
})

// ===== 検証 =====

// トースト通知（role=status, aria-live=polite）。
Then(/^"(.+)" というメッセージが表示される$/, async ({ page }, message: string) => {
  await expect(page.getByText(message, { exact: true })).toBeVisible()
})

// フォームのバリデーションエラー（.field-error）。
Then(/^"(.+)" というエラーが表示される$/, async ({ page }, message: string) => {
  await expect(page.getByText(message, { exact: true })).toBeVisible()
})

// 金額は ¥1,200 のように桁区切りで描画される。通貨記号に依存せず桁区切り表記で照合する。
function grouped(amount: string): string {
  return Number(amount).toLocaleString('ja-JP')
}

Then(
  /^一覧にカテゴリ "(.+)" 金額 "(\d+)" の行が表示される$/,
  async ({ page }, category: string, amount: string) => {
    const row = page.locator('.data-table tbody tr', { hasText: category })
    await expect(row).toContainText(grouped(amount))
  },
)

Then(/^一覧にカテゴリ "(.+)" の行は表示されない$/, async ({ page }, category: string) => {
  await expect(page.locator('.data-table tbody tr', { hasText: category })).toHaveCount(0)
})

Then(/^カテゴリ一覧に "(.+)" の行が表示される$/, async ({ page }, name: string) => {
  await expect(page.locator('.data-table tbody tr', { hasText: name })).toBeVisible()
})

Then(/^一覧に "(.+)" と表示される$/, async ({ page }, text: string) => {
  await expect(page.getByText(text, { exact: true })).toBeVisible()
})
