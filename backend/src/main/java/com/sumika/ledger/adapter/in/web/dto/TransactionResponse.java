package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import java.time.LocalDate;

/** 収支記録のレスポンス。 */
public record TransactionResponse(
    Long id,
    EntryType type,
    long amount,
    Long categoryId,
    LocalDate occurredOn,
    String memo) {

  public static TransactionResponse from(Transaction transaction) {
    return new TransactionResponse(
        transaction.id().map(TransactionId::value).orElse(null),
        transaction.type(),
        transaction.amount().amount().longValueExact(),
        transaction.categoryId().value(),
        transaction.occurredOn(),
        transaction.memo().orElse(null));
  }
}
