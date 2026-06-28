# sumika

家計簿管理の SaaS サービスです。

収支記録（収入/支出）の登録・一覧・編集・削除、カテゴリ管理、月次の集計・レポート（サマリ／カテゴリ別／推移）ができる。
モノレポ構成（`backend/` Spring Boot API + `frontend/` React SPA）で、DB は PostgreSQL。
認証は現状未実装（単一ユーザー前提。将来 `user_id` を追加できる設計）。

## 構成

- `backend/` — Spring Boot (Java) / ヘキサゴナルアーキテクチャ（[README](backend/README.md)）
- `frontend/` — React + Vite + TypeScript（[README](frontend/README.md)）
- `docs/` — 開発メモ・接続情報（[docs/README.md](docs/README.md)）
- `docker-compose.yml` — ローカル開発用 PostgreSQL

## クイックスタート

```bash
# 1. DB 起動
docker compose up -d db

# 2. backend（:8080、Swagger UI: /swagger-ui/index.html）
cd backend && ./gradlew bootRun

# 3. frontend（:5173、/api を backend へ proxy）
cd frontend && npm install && npm run dev
```

ブラウザで http://localhost:5173 を開く。

## 開発

- タスクは GitHub Issue / マイルストーン（M0 基盤整備 → M1 backend CRUD → M2 frontend UI → M3 集計・レポート）で管理。
- 1 Issue = 1 PR。詳細なコマンド・アーキテクチャは各ディレクトリの README と `CLAUDE.md`、開発ルールは `CONTRIBUTING.md` を参照。

## テスト

- **backend（単体・結合・API E2E）**: `cd backend && ./gradlew test`。Testcontainers で実 PostgreSQL を使用。API E2E は REST Assured + PICT ペアワイズ。
- **frontend 単体**: `cd frontend && npm test`（Vitest）。
- **ブラウザ E2E（Playwright + playwright-bdd / 日本語 Gherkin）**: フルスタックを起動して実行する。

  ```bash
  docker compose up -d db                      # 1. DB
  cd backend && ./gradlew bootRun              # 2. backend(:8080)
  cd frontend && npm install && npm run e2e    # 3. E2E（Vite は Playwright が自動起動）
  ```

  詳細・補助コマンド・環境変数は [frontend/README.md の「ブラウザ E2E テスト」](frontend/README.md#ブラウザ-e2e-テストplaywright--playwright-bdd) を参照。

## デプロイ（AWS / PoC）

ECS Fargate Spot + S3/CloudFront + Aurora Serverless v2 を Terraform / GitHub Actions(OIDC) で。
手順は [docs/deploy.md](docs/deploy.md)、IaC は [infra/](infra/) を参照。

## ライセンス

BSD 3-Clause License
