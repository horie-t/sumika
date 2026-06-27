package com.sumika.ledger.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * 収支記録（1 件の収入または支出）。
 *
 * <p>不変オブジェクト。金額は正、カテゴリ・発生日・種別は必須。メモは任意（空白のみは無しと扱う）。
 * 新規作成時は id を持たず（{@link #create}）、永続化済みのものは id を持つ（{@link #of}）。
 */
public class Transaction {

  /** メモの最大長。 */
  public static final int MAX_MEMO_LENGTH = 255;

  private final TransactionId id;
  private final EntryType type;
  private final Money amount;
  private final CategoryId categoryId;
  private final LocalDate occurredOn;
  private final String memo;

  private Transaction(
      TransactionId id,
      EntryType type,
      Money amount,
      CategoryId categoryId,
      LocalDate occurredOn,
      String memo) {
    this.type = Objects.requireNonNull(type, "type must not be null");
    this.amount = requirePositive(amount);
    this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
    this.occurredOn = Objects.requireNonNull(occurredOn, "occurredOn must not be null");
    this.memo = normalizeMemo(memo);
    this.id = id;
  }

  /** 新規の収支記録（未永続化）を生成する。 */
  public static Transaction create(
      EntryType type, Money amount, CategoryId categoryId, LocalDate occurredOn, String memo) {
    return new Transaction(null, type, amount, categoryId, occurredOn, memo);
  }

  /** 永続化済みの収支記録を再構築する。 */
  public static Transaction of(
      TransactionId id,
      EntryType type,
      Money amount,
      CategoryId categoryId,
      LocalDate occurredOn,
      String memo) {
    return new Transaction(
        Objects.requireNonNull(id, "id must not be null"),
        type,
        amount,
        categoryId,
        occurredOn,
        memo);
  }

  private static Money requirePositive(Money amount) {
    Objects.requireNonNull(amount, "amount must not be null");
    if (!amount.isPositive()) {
      throw new IllegalArgumentException("amount must be positive");
    }
    return amount;
  }

  private static String normalizeMemo(String memo) {
    if (memo == null) {
      return null;
    }
    String trimmed = memo.strip();
    if (trimmed.isEmpty()) {
      return null;
    }
    if (trimmed.length() > MAX_MEMO_LENGTH) {
      throw new IllegalArgumentException("memo must be " + MAX_MEMO_LENGTH + " characters or fewer");
    }
    return trimmed;
  }

  public Optional<TransactionId> id() {
    return Optional.ofNullable(this.id);
  }

  public EntryType type() {
    return this.type;
  }

  public Money amount() {
    return this.amount;
  }

  public CategoryId categoryId() {
    return this.categoryId;
  }

  public LocalDate occurredOn() {
    return this.occurredOn;
  }

  public Optional<String> memo() {
    return Optional.ofNullable(this.memo);
  }
}
