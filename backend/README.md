# backend

Spring Boot (Spring Web) / Java で実装する API サーバー。

アーキテクチャは Tom Hombergs『Get Your Hands Dirty on Clean Architecture』(2nd Ed.) に準拠した
ヘキサゴナル（ポート&アダプタ）構成。bounded context 単位でパッケージを切り、各 context を
`domain` / `application`(port in/out, service) / `adapter`(in/web, out/persistence) に分割する。
依存方向は常に内向き（`adapter → application → domain`）で、**ArchUnit**（`HexagonalArchitecture` DSL）で強制している。

## コマンド

事前にローカル DB を起動（実 DB に対して動かす場合）:

```bash
docker compose up -d db    # リポジトリルートで PostgreSQL 16 を起動
```

backend（Java 25 + Gradle wrapper。テストは Docker/Testcontainers を使用）:

```bash
./gradlew build                              # コンパイル + テスト
./gradlew test                               # 全テスト（Testcontainers で実 PostgreSQL 起動）
./gradlew test --tests "com.sumika.SomeTest" # 単一テストクラス
./gradlew bootRun                            # API を :8080 で起動（Swagger UI: /swagger-ui/index.html）
```

## パッケージ構成（bounded context = `ledger`）

```
com.sumika
├── common/                 # 横断（例外ハンドリング, OpenAPI 設定, SelfValidating, ArchUnit 補助）
└── ledger/
    ├── domain/             # フレームワーク非依存のリッチドメインモデル + 値オブジェクト
    ├── application/
    │   ├── port/in/        # ユースケース IF + Command/Query（自己バリデーション）
    │   ├── port/out/       # application が必要とする IF（adapter が実装）
    │   └── service/        # ユースケース実装（@Transactional）
    └── adapter/
        ├── in/web/         # REST コントローラ + Web DTO
        └── out/persistence/# JPA エンティティ / Spring Data リポジトリ / 永続化アダプタ / マッパー
```

- ドメインモデル・JPA エンティティ・Web DTO の **3 モデルを分離**し、境界でマッピングする。
- スキーマは **Flyway**（`src/main/resources/db/migration/`）が管理し、JPA は `ddl-auto=validate`。
- エラーは RFC 7807 `ProblemDetail` で返す（`common` の `@RestControllerAdvice`）。
