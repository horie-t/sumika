package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.UserId;
import java.time.LocalDate;
import java.util.List;

/** 集計・レポート用の取得 outgoing port。集計は DB 側で行い、利用者でスコープする。 */
public interface LoadReportPort {

  /** その利用者の、期間内（{@code from}〜{@code to} 含む）の収支をカテゴリ別に合計する。 */
  List<CategoryAmount> aggregateByCategory(UserId userId, LocalDate from, LocalDate to);

  /** その利用者の、期間内（{@code from}〜{@code to} 含む）の収支を年月・種別ごとに合計する。 */
  List<MonthlyAmount> aggregateByMonth(UserId userId, LocalDate from, LocalDate to);
}
