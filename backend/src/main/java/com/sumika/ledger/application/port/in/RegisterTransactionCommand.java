package com.sumika.ledger.application.port.in;

import com.sumika.common.SelfValidating;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 収支記録 登録の入力モデル。金額の正値などの業務不変条件はドメイン側で保証する。 */
public class RegisterTransactionCommand extends SelfValidating<RegisterTransactionCommand> {

  @NotNull private final EntryType type;
  @NotNull private final Money amount;
  @NotNull private final CategoryId categoryId;
  @NotNull private final LocalDate occurredOn;

  @Size(max = 255)
  private final String memo;

  public RegisterTransactionCommand(
      EntryType type, Money amount, CategoryId categoryId, LocalDate occurredOn, String memo) {
    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.occurredOn = occurredOn;
    this.memo = memo;
    validateSelf();
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

  public String memo() {
    return this.memo;
  }
}
