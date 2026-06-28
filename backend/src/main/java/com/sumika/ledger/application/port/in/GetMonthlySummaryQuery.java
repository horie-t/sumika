package com.sumika.ledger.application.port.in;

import java.time.YearMonth;

/** 月次サマリ（収入計・支出計・差引＋カテゴリ別内訳）の取得クエリ。 */
public interface GetMonthlySummaryQuery {

  MonthlySummary getMonthlySummary(YearMonth month);
}
