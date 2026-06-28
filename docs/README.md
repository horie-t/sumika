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

## 認証（Keycloak / OIDC）

M4 で Keycloak による認証・マルチユーザーを導入する。ローカルは docker-compose で起動する。

```bash
docker compose up -d db keycloak   # DB と認証サーバーを起動
```

| 項目 | 既定値 |
|------|--------|
| 管理コンソール | `http://localhost:8081/`（admin / admin） |
| realm | `sumika`（`keycloak/realm-sumika.json` を起動時インポート） |
| issuer | `http://localhost:8081/realms/sumika` |
| SPA クライアント | `sumika-frontend`（public / PKCE S256 / Direct Access Grants 有効） |
| デモユーザー | `demo` / `demo`（`sub` 固定 = `11111111-…`） |

- backend は OAuth2 Resource Server として上記 issuer の JWT を検証する
  （`SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` で上書き可、既定はローカル Keycloak）。
- frontend は keycloak-js でログインし、アクセストークンを `Authorization: Bearer` で送る。
- 8081 は backend(8080) との衝突回避。realm/ユーザーはコミット済み JSON 由来なので再現可能。

### 本番（AWS）方針メモ（M4 では未実装）

PoC（M5）は ECS/Aurora/S3+CloudFront 構成。本番の Keycloak は (a) ECS へ自前ホスト（専用 RDS スキーマ・SG 追加）、
または (b) マネージド（AWS Cognito 等）への置き換えが候補。いずれも backend には issuer-uri を環境変数で渡すだけで接続できる。
具体化は後続マイルストーンで扱う。

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
