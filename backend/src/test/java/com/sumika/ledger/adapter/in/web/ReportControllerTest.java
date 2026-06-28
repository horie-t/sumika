package com.sumika.ledger.adapter.in.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sumika.ledger.application.port.in.CategorySummaryLine;
import com.sumika.ledger.application.port.in.GetMonthlySummaryQuery;
import com.sumika.ledger.application.port.in.GetMonthlyTrendQuery;
import com.sumika.ledger.application.port.in.MonthlySummary;
import com.sumika.ledger.application.port.in.MonthlyTotal;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetMonthlySummaryQuery getMonthlySummaryQuery;
  @MockitoBean private GetMonthlyTrendQuery getMonthlyTrendQuery;

  @Test
  void monthlySummaryReturnsTotalsAndBreakdown() throws Exception {
    when(this.getMonthlySummaryQuery.getMonthlySummary(YearMonth.of(2026, 6)))
        .thenReturn(
            new MonthlySummary(
                YearMonth.of(2026, 6),
                Money.of(300000),
                Money.of(58000),
                Money.of(242000),
                List.of(new CategorySummaryLine(2L, "食費", EntryType.EXPENSE, Money.of(50000)))));

    this.mockMvc
        .perform(get("/api/reports/monthly-summary").param("month", "2026-06"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.month").value("2026-06"))
        .andExpect(jsonPath("$.totalIncome").value(300000))
        .andExpect(jsonPath("$.totalExpense").value(58000))
        .andExpect(jsonPath("$.net").value(242000))
        .andExpect(jsonPath("$.categories[0].categoryName").value("食費"))
        .andExpect(jsonPath("$.categories[0].total").value(50000));

    verify(this.getMonthlySummaryQuery).getMonthlySummary(YearMonth.of(2026, 6));
  }

  @Test
  void monthlyTrendReturnsSeriesAndBindsYearMonthParams() throws Exception {
    when(this.getMonthlyTrendQuery.getMonthlyTrend(YearMonth.of(2026, 5), YearMonth.of(2026, 6)))
        .thenReturn(
            List.of(
                new MonthlyTotal(YearMonth.of(2026, 5), Money.of(1000), Money.ZERO),
                new MonthlyTotal(YearMonth.of(2026, 6), Money.ZERO, Money.of(1300))));

    this.mockMvc
        .perform(get("/api/reports/monthly-trend").param("from", "2026-05").param("to", "2026-06"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].month").value("2026-05"))
        .andExpect(jsonPath("$[0].income").value(1000))
        .andExpect(jsonPath("$[1].month").value("2026-06"))
        .andExpect(jsonPath("$[1].expense").value(1300));

    verify(this.getMonthlyTrendQuery).getMonthlyTrend(YearMonth.of(2026, 5), YearMonth.of(2026, 6));
  }
}
