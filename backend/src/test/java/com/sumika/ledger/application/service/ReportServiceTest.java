package com.sumika.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  private static final UserId USER = UserId.of("user-1");

  @Mock private LoadReportPort loadReportPort;
  @Mock private CurrentUserProvider currentUserProvider;

  private ReportService service;

  @BeforeEach
  void setUp() {
    when(this.currentUserProvider.currentUserId()).thenReturn(USER);
    this.service = new ReportService(this.loadReportPort, this.currentUserProvider);
  }

  @Test
  void monthlySummaryComputesTotalsAndNet() {
    when(this.loadReportPort.aggregateByCategory(
            eq(USER), eq(LocalDate.of(2026, 6, 1)), eq(LocalDate.of(2026, 6, 30))))
        .thenReturn(
            List.of(
                new CategoryAmount(2L, "食費", EntryType.EXPENSE, 50000),
                new CategoryAmount(1L, "給与", EntryType.INCOME, 300000),
                new CategoryAmount(3L, "日用品", EntryType.EXPENSE, 8000)));

    MonthlySummary summary = this.service.getMonthlySummary(YearMonth.of(2026, 6));

    assertThat(summary.totalIncome()).isEqualTo(Money.of(300000));
    assertThat(summary.totalExpense()).isEqualTo(Money.of(58000));
    assertThat(summary.net()).isEqualTo(Money.of(242000));
    assertThat(summary.categories()).hasSize(3);
    // 収入が先、その後は金額の降順（食費 50000 > 日用品 8000）
    assertThat(summary.categories().get(0).type()).isEqualTo(EntryType.INCOME);
    assertThat(summary.categories().get(1).categoryName()).isEqualTo("食費");
    assertThat(summary.categories().get(2).categoryName()).isEqualTo("日用品");
  }

  @Test
  void monthlyTrendFillsMissingMonthsWithZero() {
    when(this.loadReportPort.aggregateByMonth(
            eq(USER), eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 6, 30))))
        .thenReturn(
            List.of(
                new MonthlyAmount(2026, 4, EntryType.INCOME, 300000),
                new MonthlyAmount(2026, 4, EntryType.EXPENSE, 1000),
                new MonthlyAmount(2026, 6, EntryType.EXPENSE, 5000)));

    List<MonthlyTotal> trend =
        this.service.getMonthlyTrend(YearMonth.of(2026, 4), YearMonth.of(2026, 6));

    assertThat(trend).hasSize(3);
    assertThat(trend.get(0).month()).isEqualTo(YearMonth.of(2026, 4));
    assertThat(trend.get(0).income()).isEqualTo(Money.of(300000));
    assertThat(trend.get(0).expense()).isEqualTo(Money.of(1000));
    // 5月はデータなし → 0 埋め
    assertThat(trend.get(1).month()).isEqualTo(YearMonth.of(2026, 5));
    assertThat(trend.get(1).income()).isEqualTo(Money.ZERO);
    assertThat(trend.get(1).expense()).isEqualTo(Money.ZERO);
    assertThat(trend.get(2).month()).isEqualTo(YearMonth.of(2026, 6));
    assertThat(trend.get(2).expense()).isEqualTo(Money.of(5000));
  }
}
