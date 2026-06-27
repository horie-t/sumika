import type { components } from './schema'

/** OpenAPI スキーマ（`schema.d.ts`）から導出した利用しやすい型エイリアス。 */

export type EntryType = 'INCOME' | 'EXPENSE'

export type Category = components['schemas']['CategoryResponse']
export type CategoryInput = components['schemas']['CategoryRequest']

export type Transaction = components['schemas']['TransactionResponse']
export type TransactionInput = components['schemas']['TransactionRequest']
