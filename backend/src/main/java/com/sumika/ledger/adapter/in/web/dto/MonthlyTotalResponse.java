package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.application.port.in.MonthlyTotal;

/** 月別推移 1 か月分のレスポンス。{@code month} は {@code YYYY-MM}。 */
public record MonthlyTotalResponse(String month, long income, long expense) {

  public static MonthlyTotalResponse from(MonthlyTotal total) {
    return new MonthlyTotalResponse(
        total.month().toString(),
        total.income().amount().longValueExact(),
        total.expense().amount().longValueExact());
  }
}
