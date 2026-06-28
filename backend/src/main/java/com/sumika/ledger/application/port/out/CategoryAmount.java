package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.EntryType;

/**
 * カテゴリ別集計の生結果（DB 集計の 1 行）。
 *
 * @param categoryId カテゴリ ID
 * @param categoryName カテゴリ名
 * @param type 収支種別
 * @param total 合計金額（最小通貨単位）
 */
public record CategoryAmount(Long categoryId, String categoryName, EntryType type, long total) {}
