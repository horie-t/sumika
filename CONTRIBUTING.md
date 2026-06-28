# コントリビューションガイド

sumika への開発参加時のルールとメモ。コマンド・アーキテクチャの詳細は各ディレクトリの
README と `CLAUDE.md` を参照。

## 開発フロー

- **1 Issue = 1 PR**。作業はマイルストーン（M0 基盤整備 → …）配下の Issue で管理する。
- `main` は保護ブランチ。直接 push / force-push / 削除は不可。PR 経由（レビュー 0 承認可）で、
  会話の解決と **`ci-success` ステータスチェック**の通過が必須。
- コミットメッセージは Conventional Commits 風に `type(scope): 概要（日本語可）` で書く
  （例: `feat(backend): …` / `test(frontend): …` / `docs: …` / `ci: …` / `fix: …`）。

## フロントエンドの依存とロックファイル（重要）

CI は **Node 22 + npm 11 系の最新**で動く（`npm install -g npm@11`）。`package-lock.json` を
生成・更新した npm のバージョンが古いと、CI の npm が要求する optional 依存（プラットフォーム別の
`@emnapi/*` など）が lock に欠け、`npm ci` が `EUSAGE`（lock 不整合）で失敗することがある。

- **lock を更新する作業（依存の追加・更新）は、CI と同じ npm バージョンで行う。**
  ローカルの npm が古い場合は、その場限りで CI 相当の npm を使う:

  ```bash
  cd frontend
  npx -y npm@11 install        # CI と同じ npm 11 系で lock を再生成
  npx -y npm@11 ci             # 生成した lock で npm ci が通るか検証
  ```

  あるいはローカルの npm 自体を更新する: `npm install -g npm@11`。

- ローカルの古い npm で `npm install` すると lock が逆方向に書き換わる（上記 optional 依存が
  消える）ことがある。その差分はコミットしないこと。意図しない `package-lock.json` の変更は破棄する:
  `git checkout -- frontend/package-lock.json`。

## テスト

実行方法は各 README を参照。

- backend（単体・結合・API E2E）: `cd backend && ./gradlew test`
- frontend 単体: `cd frontend && npm test`
- ブラウザ E2E（Playwright + playwright-bdd / 日本語 Gherkin）:
  [frontend/README.md の「ブラウザ E2E テスト」](frontend/README.md#ブラウザ-e2e-テストplaywright--playwright-bdd)

PR を出す前にローカルで lint / build / テストが通ることを確認する。
