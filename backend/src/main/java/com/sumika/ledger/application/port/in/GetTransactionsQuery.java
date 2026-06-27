package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.Transaction;
import java.time.LocalDate;
import java.util.List;

/** 収支記録の取得クエリ。各引数は {@code null} で「絞り込みなし」を表す。 */
public interface GetTransactionsQuery {

  List<Transaction> getTransactions(LocalDate from, LocalDate to, CategoryId categoryId);
}
