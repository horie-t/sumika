package com.sumika.ledger.domain;

import java.util.Objects;

/** カテゴリの識別子。 */
public record CategoryId(Long value) {

  public CategoryId {
    Objects.requireNonNull(value, "category id must not be null");
  }

  public static CategoryId of(long value) {
    return new CategoryId(value);
  }
}
