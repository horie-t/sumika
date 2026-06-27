# docs

sumika（家計簿SaaS）の設計メモ・API 仕様などを置く。

## 構成

- `backend/` — Spring Boot API サーバー（ヘキサゴナルアーキテクチャ）
- `frontend/` — React + Vite + TypeScript の Web フロントエンド
- `docker-compose.yml` — ローカル開発用 PostgreSQL（Issue #4 で追加）

## 開発の進め方

- タスクは GitHub Issue / マイルストーン（M0 基盤整備 → M1 backend CRUD → M2 frontend UI）で管理。
- 1 Issue = 1 PR を基本とする。
