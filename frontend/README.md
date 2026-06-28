# frontend

React + Vite + TypeScript で実装する Web フロントエンド。

- ルーティング: React Router
- サーバー状態: TanStack Query
- HTTP: axios（`src/api/client.ts` の共有インスタンス）
- 型: OpenAPI から自動生成（`src/api/schema.d.ts`）
- Lint: oxlint / Format: Prettier
- Test: Vitest + React Testing Library（単体）/ Playwright + playwright-bdd（ブラウザ E2E）

## コマンド

```bash
npm install          # 依存インストール（または npm ci）
npm run dev          # 開発サーバー (http://localhost:5173)
npm run build        # 型チェック + 本番ビルド
npm run lint         # oxlint
npm run format       # Prettier で整形
npm run format:check # 整形差分チェック（CI 用）
npm test             # Vitest（vitest run）— 単体テスト
npm run gen:api      # openapi.json から型を再生成（API 変更時）
npm run e2e          # ブラウザ E2E（bddgen → playwright test）
npm run e2e:ui       # E2E を Playwright UI モードで実行（デバッグ向け）
npm run e2e:report   # 直近の E2E 実行の HTML レポートを表示
```

## ブラウザ E2E テスト（Playwright + playwright-bdd）

実ブラウザから画面を操作し、**実バックエンド + Postgres** に対して主要フローを検証する
フルスタック E2E。テスト仕様は **日本語 Gherkin**（`# language: ja`）で記述し、ステップ実装は
TypeScript（`e2e/` 配下）。

```
e2e/
  features/     # 日本語 Gherkin のテスト仕様（PO も読める）: transactions.feature, categories.feature
  steps/        # ステップ実装（TypeScript）
    fixtures.ts       # createBdd・DB クリーンアップ(Before フック)・API シード基点
    common.steps.ts   # ナビ/ボタン/フォーム入力/トースト・エラー検証
    seed.steps.ts     # 前提データ（カテゴリ/収支）を API で投入する Given
```

### 手動で実行する手順

フルスタック（DB + backend + frontend）を起動して実行する。**frontend の dev サーバーは
Playwright が自動起動する**ため手動起動は不要。

```bash
# 1. リポジトリルートで DB を起動
docker compose up -d db

# 2. backend を起動（:8080）。別ターミナルで:
cd backend && ./gradlew bootRun

# 3. frontend で E2E を実行。別ターミナルで:
cd frontend
npm install
npx playwright install chromium   # 初回のみ（ブラウザを取得）
npm run e2e                       # 全シナリオを実行
```

実行後の確認・デバッグ:

```bash
npm run e2e:report   # HTML レポート（失敗時はトレース/スクショ付き）
npm run e2e:ui       # UI モードでシナリオを 1 つずつ実行
```

### 注意・カスタマイズ

- **各シナリオの前に DB を `TRUNCATE` する**（テスト分離のため）。ローカル DB の収支・カテゴリ
  データは消えるので、開発用 DB に対してのみ実行すること。
- 接続先は環境変数で上書きできる（既定は docker compose と同じ）:
  - `E2E_BASE_URL`（既定 `http://localhost:5173`）— フロントの URL
  - `E2E_BACKEND_URL`（既定 `http://localhost:8080`）— 前提データ投入の API
  - `E2E_DB_HOST` / `E2E_DB_PORT` / `E2E_DB_USER` / `E2E_DB_PASSWORD` / `E2E_DB_NAME`（既定すべて `sumika`、ポート `5432`）
- CI では専用の `e2e` ジョブが Postgres・backend を起動して自動実行する（`.github/workflows/ci.yml`）。

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
