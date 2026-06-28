package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.application.port.out.CategoryAmount;
import com.sumika.ledger.application.port.out.LoadReportPort;
import com.sumika.ledger.application.port.out.MonthlyAmount;
import com.sumika.ledger.domain.EntryType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/** 集計・レポートの out ポートを JPA（DB 側 GROUP BY）で実装する永続化アダプタ。 */
@Component
class ReportPersistenceAdapter implements LoadReportPort {

  private final TransactionJpaRepository repository;

  ReportPersistenceAdapter(TransactionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CategoryAmount> aggregateByCategory(LocalDate from, LocalDate to) {
    return this.repository.aggregateByCategory(from, to).stream()
        .map(
            p ->
                new CategoryAmount(
                    p.getCategoryId(), p.getCategoryName(), p.getType(), p.getTotal()))
        .toList();
  }

  @Override
  public List<MonthlyAmount> aggregateByMonth(LocalDate from, LocalDate to) {
    return this.repository.aggregateByMonth(from, to).stream()
        .map(
            p ->
                new MonthlyAmount(
                    p.getYear(), p.getMonth(), EntryType.valueOf(p.getType()), p.getTotal()))
        .toList();
  }
}
