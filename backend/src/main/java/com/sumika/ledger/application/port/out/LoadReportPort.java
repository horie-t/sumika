package com.sumika.ledger.application.port.out;

import java.time.LocalDate;
import java.util.List;

/** 集計・レポート用の取得 outgoing port。集計は DB 側で行う。 */
public interface LoadReportPort {

  /** 期間内（{@code from}〜{@code to} 含む）の収支をカテゴリ別に合計する。 */
  List<CategoryAmount> aggregateByCategory(LocalDate from, LocalDate to);

  /** 期間内（{@code from}〜{@code to} 含む）の収支を年月・種別ごとに合計する。 */
  List<MonthlyAmount> aggregateByMonth(LocalDate from, LocalDate to);
}
