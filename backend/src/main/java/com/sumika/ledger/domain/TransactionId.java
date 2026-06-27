package com.sumika.ledger.domain;

import java.util.Objects;

/** 収支記録の識別子。 */
public record TransactionId(Long value) {

  public TransactionId {
    Objects.requireNonNull(value, "transaction id must not be null");
  }

  public static TransactionId of(long value) {
    return new TransactionId(value);
  }
}
