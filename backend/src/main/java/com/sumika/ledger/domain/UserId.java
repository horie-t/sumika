package com.sumika.ledger.domain;

import java.util.Objects;

/** 利用者の識別子。認証基盤（Keycloak）の JWT subject（{@code sub}）を保持する。 */
public record UserId(String value) {

  public UserId {
    Objects.requireNonNull(value, "user id must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("user id must not be blank");
    }
  }

  public static UserId of(String value) {
    return new UserId(value);
  }
}
