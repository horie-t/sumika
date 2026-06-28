-- M4: 認証・マルチユーザー。各レコードを所有する利用者（Keycloak の sub）を追加する。
-- 新規前提（既存行のバックフィルは行わない。ローカルは docker compose down -v で作り直す）。

alter table categories   add column user_id varchar(64) not null;
alter table transactions add column user_id varchar(64) not null;

-- 利用者ごとの一覧・集計・期間絞り込みを効率化する複合インデックス。
create index idx_categories_user        on categories (user_id);
create index idx_transactions_user_date on transactions (user_id, occurred_on);
create index idx_transactions_user_cat  on transactions (user_id, category_id);
