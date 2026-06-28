package com.sumika.ledger.adapter.in.web;

import com.sumika.ledger.adapter.in.web.dto.MonthlySummaryResponse;
import com.sumika.ledger.adapter.in.web.dto.MonthlyTotalResponse;
import com.sumika.ledger.application.port.in.GetMonthlySummaryQuery;
import com.sumika.ledger.application.port.in.GetMonthlyTrendQuery;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
class ReportController {

  private final GetMonthlySummaryQuery getMonthlySummaryQuery;
  private final GetMonthlyTrendQuery getMonthlyTrendQuery;

  ReportController(
      GetMonthlySummaryQuery getMonthlySummaryQuery, GetMonthlyTrendQuery getMonthlyTrendQuery) {
    this.getMonthlySummaryQuery = getMonthlySummaryQuery;
    this.getMonthlyTrendQuery = getMonthlyTrendQuery;
  }

  /** 選択月の収入計・支出計・差引＋カテゴリ別内訳。 */
  @GetMapping("/monthly-summary")
  MonthlySummaryResponse monthlySummary(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
    return MonthlySummaryResponse.from(this.getMonthlySummaryQuery.getMonthlySummary(month));
  }

  /** {@code from}〜{@code to} の各月の収入/支出（欠損月は 0）。 */
  @GetMapping("/monthly-trend")
  List<MonthlyTotalResponse> monthlyTrend(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth from,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth to) {
    return this.getMonthlyTrendQuery.getMonthlyTrend(from, to).stream()
        .map(MonthlyTotalResponse::from)
        .toList();
  }
}
