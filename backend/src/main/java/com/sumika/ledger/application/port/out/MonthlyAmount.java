package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.EntryType;

/**
 * 月別集計の生結果（DB 集計の 1 行）。
 *
 * @param year 年
 * @param month 月（1〜12）
 * @param type 収支種別
 * @param total 合計金額（最小通貨単位）
 */
public record MonthlyAmount(int year, int month, EntryType type, long total) {}
