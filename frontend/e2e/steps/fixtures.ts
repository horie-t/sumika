import { test as base, createBdd } from 'playwright-bdd'
import { Client } from 'pg'

/**
 * BDD のステップ・フックを生成する起点。
 * フルスタック E2E（実ブラウザ × 実バックエンド × Postgres × Keycloak）用。
 */
export const test = base

export const { Given, When, Then, Before, After } = createBdd(test)

/** バックエンド API の基点（`前提` のデータ投入を UI ではなく API で行う）。 */
export const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080'

const KEYCLOAK_URL = process.env.E2E_KEYCLOAK_URL ?? 'http://localhost:8081'

/** 種別ラベル（日本語）→ API の EntryType。 */
export function toEntryType(label: string): 'INCOME' | 'EXPENSE' {
  return label === '収入' ? 'INCOME' : 'EXPENSE'
}

let cachedToken: string | undefined

/** demo ユーザーの password grant でアクセストークンを取得（JVM 内でキャッシュ）。 */
async function accessToken(): Promise<string> {
  if (!cachedToken) {
    const res = await fetch(`${KEYCLOAK_URL}/realms/sumika/protocol/openid-connect/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        client_id: 'sumika-frontend',
        grant_type: 'password',
        username: 'demo',
        password: 'demo',
      }),
    })
    if (!res.ok) throw new Error(`Keycloak token request failed: ${res.status}`)
    cachedToken = ((await res.json()) as { access_token: string }).access_token
  }
  return cachedToken
}

/** API シード用の認証ヘッダ（UI ログインと同一の demo ユーザー＝同一 sub）。 */
export async function authHeaders(): Promise<Record<string, string>> {
  return { Authorization: `Bearer ${await accessToken()}` }
}

const dbConfig = {
  host: process.env.E2E_DB_HOST ?? 'localhost',
  port: Number(process.env.E2E_DB_PORT ?? 5432),
  user: process.env.E2E_DB_USER ?? 'sumika',
  password: process.env.E2E_DB_PASSWORD ?? 'sumika',
  database: process.env.E2E_DB_NAME ?? 'sumika',
}

/**
 * 各シナリオ前に DB を空にして分離する。
 * 実ポート E2E はトランザクションロールバックが効かないため、バックエンドの
 * clean.sql と同じく TRUNCATE ... RESTART IDENTITY で id 採番ごとリセットする。
 */
Before(async () => {
  const client = new Client(dbConfig)
  await client.connect()
  try {
    await client.query('TRUNCATE transactions, categories RESTART IDENTITY CASCADE')
  } finally {
    await client.end()
  }
})
