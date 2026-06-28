package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Money;
import java.time.YearMonth;

/**
 * 月別推移の 1 か月分。
 *
 * @param month 対象月
 * @param income その月の収入合計
 * @param expense その月の支出合計
 */
public record MonthlyTotal(YearMonth month, Money income, Money expense) {}
