package com.sumika.ledger.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private static final CategoryId CATEGORY = CategoryId.of(1);
  private static final LocalDate DATE = LocalDate.of(2026, 6, 27);

  @Test
  void createValidTransaction() {
    Transaction tx =
        Transaction.create(EntryType.EXPENSE, Money.of(1200), CATEGORY, DATE, "ランチ");

    assertTrue(tx.id().isEmpty());
    assertEquals(EntryType.EXPENSE, tx.type());
    assertEquals(Money.of(1200), tx.amount());
    assertEquals(CATEGORY, tx.categoryId());
    assertEquals(DATE, tx.occurredOn());
    assertEquals("ランチ", tx.memo().orElseThrow());
  }

  @Test
  void ofHasId() {
    Transaction tx =
        Transaction.of(TransactionId.of(10), EntryType.INCOME, Money.of(300000), CATEGORY, DATE, null);

    assertEquals(TransactionId.of(10), tx.id().orElseThrow());
    assertTrue(tx.memo().isEmpty());
  }

  @Test
  void rejectsNonPositiveAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.create(EntryType.EXPENSE, Money.ZERO, CATEGORY, DATE, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.create(EntryType.EXPENSE, Money.of(-1), CATEGORY, DATE, null));
  }

  @Test
  void rejectsNullRequiredFields() {
    assertThrows(
        NullPointerException.class,
        () -> Transaction.create(null, Money.of(100), CATEGORY, DATE, null));
    assertThrows(
        NullPointerException.class,
        () -> Transaction.create(EntryType.EXPENSE, Money.of(100), null, DATE, null));
    assertThrows(
        NullPointerException.class,
        () -> Transaction.create(EntryType.EXPENSE, Money.of(100), CATEGORY, null, null));
  }

  @Test
  void blankMemoBecomesEmpty() {
    Transaction tx =
        Transaction.create(EntryType.EXPENSE, Money.of(100), CATEGORY, DATE, "   ");
    assertTrue(tx.memo().isEmpty());
  }

  @Test
  void rejectsTooLongMemo() {
    String tooLong = "x".repeat(Transaction.MAX_MEMO_LENGTH + 1);
    assertThrows(
        IllegalArgumentException.class,
        () -> Transaction.create(EntryType.EXPENSE, Money.of(100), CATEGORY, DATE, tooLong));
  }
}
