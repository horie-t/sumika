package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.application.port.out.CategoryAmount;
import com.sumika.ledger.application.port.out.LoadReportPort;
import com.sumika.ledger.application.port.out.MonthlyAmount;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.UserId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/** 集計・レポートの out ポートを JPA（DB 側 GROUP BY）で実装する永続化アダプタ。利用者でスコープする。 */
@Component
class ReportPersistenceAdapter implements LoadReportPort {

  private final TransactionJpaRepository repository;

  ReportPersistenceAdapter(TransactionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CategoryAmount> aggregateByCategory(UserId userId, LocalDate from, LocalDate to) {
    return this.repository.aggregateByCategory(userId.value(), from, to).stream()
        .map(p -> new CategoryAmount(p.getCategoryId(), p.getCategoryName(), p.getType(), p.getTotal()))
        .toList();
  }

  @Override
  public List<MonthlyAmount> aggregateByMonth(UserId userId, LocalDate from, LocalDate to) {
    return this.repository.aggregateByMonth(userId.value(), from, to).stream()
        .map(p -> new MonthlyAmount(p.getYear(), p.getMonth(), EntryType.valueOf(p.getType()), p.getTotal()))
        .toList();
  }
}
