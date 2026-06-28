package com.sumika.ledger.application.port.in;

import java.time.YearMonth;
import java.util.List;

/** 月別推移（{@code from}〜{@code to} の各月の収入/支出）の取得クエリ。欠損月は 0 で埋める。 */
public interface GetMonthlyTrendQuery {

  List<MonthlyTotal> getMonthlyTrend(YearMonth from, YearMonth to);
}
