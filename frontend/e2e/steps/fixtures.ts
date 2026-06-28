import { test as base, createBdd } from 'playwright-bdd'
import { Client } from 'pg'

/**
 * BDD のステップ・フックを生成する起点。
 * フルスタック E2E（実ブラウザ × 実バックエンド × Postgres）用。
 */
export const test = base

export const { Given, When, Then, Before, After } = createBdd(test)

/** バックエンド API の基点（`前提` のデータ投入を UI ではなく API で行う）。 */
export const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080'

/** 種別ラベル（日本語）→ API の EntryType。 */
export function toEntryType(label: string): 'INCOME' | 'EXPENSE' {
  return label === '収入' ? 'INCOME' : 'EXPENSE'
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
