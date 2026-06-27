# frontend

React + Vite + TypeScript で実装する Web フロントエンド。

- ルーティング: React Router
- サーバー状態: TanStack Query
- HTTP: axios（`src/api/client.ts` の共有インスタンス）
- Lint: oxlint / Format: Prettier

## コマンド

```bash
npm install          # 依存インストール
npm run dev          # 開発サーバー (http://localhost:5173)
npm run build        # 型チェック + 本番ビルド
npm run lint         # oxlint
npm run format       # Prettier で整形
npm run format:check # 整形差分チェック（CI 用）
```

API のベース URL は `.env`（`VITE_API_BASE_URL`）で上書きできる。`.env.example` を参照。

## ディレクトリ方針

```
src/
  api/         # axios クライアントと API 呼び出し（client.ts ほか）
  features/    # 機能単位のモジュール（例: transactions, categories）
               #   各 feature 内に components / hooks / api / types を凝集させる
  components/  # 機能横断の共有 UI コンポーネント
  pages/       # ルートに対応する画面コンポーネント
  main.tsx     # エントリ。QueryClientProvider / BrowserRouter を配線
  App.tsx      # ルーティング定義
```

> `features/` `components/` は機能追加（M2）時に作成する。
