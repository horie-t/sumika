package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.application.port.in.MonthlySummary;
import java.util.List;

/** 月次サマリのレスポンス。{@code month} は {@code YYYY-MM}。 */
public record MonthlySummaryResponse(
    String month,
    long totalIncome,
    long totalExpense,
    long net,
    List<CategorySummaryLineResponse> categories) {

  public static MonthlySummaryResponse from(MonthlySummary summary) {
    return new MonthlySummaryResponse(
        summary.month().toString(),
        summary.totalIncome().amount().longValueExact(),
        summary.totalExpense().amount().longValueExact(),
        summary.net().amount().longValueExact(),
        summary.categories().stream().map(CategorySummaryLineResponse::from).toList());
  }
}
