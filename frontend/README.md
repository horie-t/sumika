# frontend

React + Vite + TypeScript で実装する Web フロントエンド。

- ルーティング: React Router
- サーバー状態: TanStack Query
- HTTP: axios（`src/api/client.ts` の共有インスタンス）
- 型: OpenAPI から自動生成（`src/api/schema.d.ts`）
- Lint: oxlint / Format: Prettier / Test: Vitest + React Testing Library

## コマンド

```bash
npm install          # 依存インストール（または npm ci）
npm run dev          # 開発サーバー (http://localhost:5173)
npm run build        # 型チェック + 本番ビルド
npm run lint         # oxlint
npm run format       # Prettier で整形
npm run format:check # 整形差分チェック（CI 用）
npm test             # Vitest（vitest run）
npm run gen:api      # openapi.json から型を再生成（API 変更時）
```

## backend への接続

開発時は Vite の dev proxy で `/api` を backend(`http://localhost:8080`) に転送する（同一オリジン扱いで CORS 不要）。
axios の baseURL は既定で相対 `/api`。別オリジン配信時は `VITE_API_BASE_URL` で上書きする（`.env.example` 参照）。

API の型は backend の `/v3/api-docs` を `openapi.json` に保存し、`npm run gen:api` で生成する。

## ディレクトリ方針

```
src/
  api/         # axios クライアント・型(schema.d.ts/types.ts)・TanStack Query フック
  features/    # 機能単位のモジュール（transactions, categories）。一覧/フォーム等の画面を含む
  components/  # 機能横断の共有 UI（ToastProvider, ErrorBoundary, GlobalLoadingIndicator）
  test/        # テストセットアップ（setup.ts）
  main.tsx     # エントリ。ErrorBoundary / QueryClientProvider / ToastProvider / BrowserRouter を配線
  App.tsx      # ルーティング定義（ナビゲーション + 各画面）
```
