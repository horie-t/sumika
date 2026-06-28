# docs

sumika（家計簿SaaS）の設計メモ・API 仕様などを置く。

## 構成

- `backend/` — Spring Boot API サーバー（ヘキサゴナルアーキテクチャ）
- `frontend/` — React + Vite + TypeScript の Web フロントエンド
- `docker-compose.yml` — ローカル開発用 PostgreSQL

## ローカル開発用 DB（PostgreSQL）

```bash
docker compose up -d db    # 起動
docker compose down        # 停止（データは保持）
docker compose down -v     # 破棄（ボリュームごと削除）
```

| 項目 | 既定値 |
|------|--------|
| host:port | `localhost:5432` |
| database | `sumika` |
| user / password | `sumika` / `sumika` |
| JDBC URL | `jdbc:postgresql://localhost:5432/sumika` |

backend は上記を既定値として接続する（`backend/src/main/resources/application.yml`）。
環境変数 `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
で上書き可能。スキーマは Flyway（`backend/src/main/resources/db/migration/`）で管理する。

## フロントエンド

```bash
cd frontend
npm install
npm run dev    # http://localhost:5173（/api を backend :8080 へ proxy）
npm test       # Vitest
```

- backend 起動中（`docker compose up -d db` ＋ `./gradlew bootRun`）であれば、dev サーバ経由でブラウザから API に到達できる（同一オリジン扱いで CORS 不要）。
- API の TypeScript 型は backend の `/v3/api-docs` を `frontend/openapi.json` に保存し、`npm run gen:api` で生成する（API 変更時に再生成）。

## API ドキュメント

backend 起動中に Swagger UI（`http://localhost:8080/swagger-ui/index.html`）/ OpenAPI（`/v3/api-docs`）で確認できる。

## 開発の進め方

- タスクは GitHub Issue / マイルストーン（M0 基盤整備 → M1 backend CRUD → M2 frontend UI → M3 集計・レポート）で管理。
- 1 Issue = 1 PR を基本とする。開発ルールは `CONTRIBUTING.md` を参照。
