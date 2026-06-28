-- REST Assured の E2E は実ポート（別スレッド）で動くため、テストの @Transactional では
-- ロールバックされない。各テスト実行前にこのスクリプトでテーブルを空にして分離する。
-- RESTART IDENTITY で id 採番もリセットし、CASCADE で外部キー依存ごと削除する。
truncate table transactions, categories restart identity cascade;
