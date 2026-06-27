package com.sumika.ledger.domain;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 金額を表す値オブジェクト。最小通貨単位（円）を {@link BigInteger} で保持する。
 *
 * <p>不変・値等価。負値も表現できる（残高計算などに使うため）。取引金額が正であることの
 * 強制は {@link Transaction} 側で行う。
 */
public record Money(BigInteger amount) {

  public static final Money ZERO = Money.of(0L);

  public Money {
    Objects.requireNonNull(amount, "amount must not be null");
  }

  public static Money of(long value) {
    return new Money(BigInteger.valueOf(value));
  }

  public static Money of(BigInteger value) {
    return new Money(value);
  }

  public Money plus(Money other) {
    return new Money(this.amount.add(other.amount));
  }

  public Money minus(Money other) {
    return new Money(this.amount.subtract(other.amount));
  }

  public boolean isPositive() {
    return this.amount.signum() > 0;
  }

  public boolean isNegative() {
    return this.amount.signum() < 0;
  }

  public boolean isZero() {
    return this.amount.signum() == 0;
  }
}
