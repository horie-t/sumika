package com.sumika.ledger.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void ofLongHoldsAmount() {
    assertEquals(BigInteger.valueOf(1500), Money.of(1500).amount());
  }

  @Test
  void plusAndMinus() {
    assertEquals(Money.of(300), Money.of(100).plus(Money.of(200)));
    assertEquals(Money.of(100), Money.of(300).minus(Money.of(200)));
  }

  @Test
  void signPredicates() {
    assertTrue(Money.of(1).isPositive());
    assertTrue(Money.of(-1).isNegative());
    assertTrue(Money.ZERO.isZero());
    assertFalse(Money.ZERO.isPositive());
  }

  @Test
  void valueEquality() {
    assertEquals(Money.of(42), Money.of(BigInteger.valueOf(42)));
    assertEquals(Money.of(42).hashCode(), Money.of(42).hashCode());
  }

  @Test
  void rejectsNullAmount() {
    assertThrows(NullPointerException.class, () -> Money.of((BigInteger) null));
  }
}
