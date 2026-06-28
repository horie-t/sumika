package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Money;
import java.time.YearMonth;
import java.util.List;

/**
 * ある月の収支サマリ。
 *
 * @param month 対象月
 * @param totalIncome 収入合計
 * @param totalExpense 支出合計
 * @param net 差引（収入 - 支出。負にもなり得る）
 * @param categories カテゴリ別内訳
 */
public record MonthlySummary(
    YearMonth month,
    Money totalIncome,
    Money totalExpense,
    Money net,
    List<CategorySummaryLine> categories) {}
