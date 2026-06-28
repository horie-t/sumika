package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;

/**
 * カテゴリ別集計の 1 行。指定期間内の、あるカテゴリの合計金額。
 *
 * @param categoryId カテゴリ ID
 * @param categoryName カテゴリ名
 * @param type 収支種別（カテゴリの種別）
 * @param total 合計金額（正の値）
 */
public record CategorySummaryLine(
    Long categoryId, String categoryName, EntryType type, Money total) {}
