package com.sumika.ledger.application.service;

import com.sumika.ledger.application.port.in.CategorySummaryLine;
import com.sumika.ledger.application.port.in.GetMonthlySummaryQuery;
import com.sumika.ledger.application.port.in.GetMonthlyTrendQuery;
import com.sumika.ledger.application.port.in.MonthlySummary;
import com.sumika.ledger.application.port.in.MonthlyTotal;
import com.sumika.ledger.application.port.out.CategoryAmount;
import com.sumika.ledger.application.port.out.CurrentUserProvider;
import com.sumika.ledger.application.port.out.LoadReportPort;
import com.sumika.ledger.application.port.out.MonthlyAmount;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.UserId;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 集計・レポートのユースケース実装。読み取り専用で DB 集計の結果を読み取りモデルに組み立てる。 */
@Service
@Transactional(readOnly = true)
class ReportService implements GetMonthlySummaryQuery, GetMonthlyTrendQuery {

  private final LoadReportPort loadReportPort;
  private final CurrentUserProvider currentUserProvider;

  ReportService(LoadReportPort loadReportPort, CurrentUserProvider currentUserProvider) {
    this.loadReportPort = loadReportPort;
    this.currentUserProvider = currentUserProvider;
  }

  @Override
  public MonthlySummary getMonthlySummary(YearMonth month) {
    UserId userId = this.currentUserProvider.currentUserId();
    List<CategoryAmount> rows =
        this.loadReportPort.aggregateByCategory(userId, month.atDay(1), month.atEndOfMonth());

    Money totalIncome = sum(rows, EntryType.INCOME);
    Money totalExpense = sum(rows, EntryType.EXPENSE);

    // 種別（収入が先）→ 金額の降順で安定的に並べる。
    List<CategorySummaryLine> categories =
        rows.stream()
            .map(r -> new CategorySummaryLine(r.categoryId(), r.categoryName(), r.type(), Money.of(r.total())))
            .sorted(
                Comparator.comparing(CategorySummaryLine::type)
                    .thenComparing(
                        (CategorySummaryLine l) -> l.total().amount(), Comparator.reverseOrder()))
            .toList();

    return new MonthlySummary(
        month, totalIncome, totalExpense, totalIncome.minus(totalExpense), categories);
  }

  @Override
  public List<MonthlyTotal> getMonthlyTrend(YearMonth from, YearMonth to) {
    UserId userId = this.currentUserProvider.currentUserId();
    LocalDate rangeStart = from.atDay(1);
    LocalDate rangeEnd = to.atEndOfMonth();
    List<MonthlyAmount> rows = this.loadReportPort.aggregateByMonth(userId, rangeStart, rangeEnd);

    Map<YearMonth, Money> incomeByMonth = new HashMap<>();
    Map<YearMonth, Money> expenseByMonth = new HashMap<>();
    for (MonthlyAmount row : rows) {
      YearMonth ym = YearMonth.of(row.year(), row.month());
      Map<YearMonth, Money> target =
          row.type() == EntryType.INCOME ? incomeByMonth : expenseByMonth;
      target.merge(ym, Money.of(row.total()), Money::plus);
    }

    // from〜to を連続させ、欠損月は 0 で埋める。
    List<MonthlyTotal> trend = new ArrayList<>();
    for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
      trend.add(
          new MonthlyTotal(
              ym,
              incomeByMonth.getOrDefault(ym, Money.ZERO),
              expenseByMonth.getOrDefault(ym, Money.ZERO)));
    }
    return trend;
  }

  private static Money sum(List<CategoryAmount> rows, EntryType type) {
    return rows.stream()
        .filter(r -> r.type() == type)
        .map(r -> Money.of(r.total()))
        .reduce(Money.ZERO, Money::plus);
  }
}
