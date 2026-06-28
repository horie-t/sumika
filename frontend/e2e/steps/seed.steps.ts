import type { APIRequestContext } from '@playwright/test'
import { expect } from '@playwright/test'
import { Given, BACKEND_URL, authHeaders, toEntryType } from './fixtures'

/** カテゴリ名から id を引く（事前に作成済みであること）。 */
async function categoryIdByName(request: APIRequestContext, name: string): Promise<number> {
  const res = await request.get(`${BACKEND_URL}/api/categories`, { headers: await authHeaders() })
  expect(res.ok()).toBeTruthy()
  const categories = (await res.json()) as Array<{ id: number; name: string }>
  const found = categories.find((c) => c.name === name)
  if (!found) throw new Error(`カテゴリが見つかりません: ${name}`)
  return found.id
}

// 前提データは UI ではなく API で投入し、シナリオを検証対象の挙動に集中させる。
// UI ログインと同一の demo ユーザーのトークンで投入するため、登録データは UI から見える。

Given(
  /^カテゴリ "(.+)"\((支出|収入)\) が存在する$/,
  async ({ request }, name: string, typeLabel: string) => {
    const res = await request.post(`${BACKEND_URL}/api/categories`, {
      headers: await authHeaders(),
      data: { name, type: toEntryType(typeLabel) },
    })
    expect(res.status()).toBe(201)
  },
)

Given(
  /^"(.+)"\((支出|収入)\) の収支 金額 "(\d+)" 日付 "([\d-]+)" が登録済みである$/,
  async ({ request }, name: string, typeLabel: string, amount: string, occurredOn: string) => {
    const categoryId = await categoryIdByName(request, name)
    const res = await request.post(`${BACKEND_URL}/api/transactions`, {
      headers: await authHeaders(),
      data: { type: toEntryType(typeLabel), amount: Number(amount), categoryId, occurredOn },
    })
    expect(res.status()).toBe(201)
  },
)
