package com.sumika.ledger.application.port.in;

import com.sumika.common.SelfValidating;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.TransactionId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 収支記録 更新の入力モデル。 */
public class UpdateTransactionCommand extends SelfValidating<UpdateTransactionCommand> {

  @NotNull private final TransactionId id;
  @NotNull private final EntryType type;
  @NotNull private final Money amount;
  @NotNull private final CategoryId categoryId;
  @NotNull private final LocalDate occurredOn;

  @Size(max = 255)
  private final String memo;

  public UpdateTransactionCommand(
      TransactionId id,
      EntryType type,
      Money amount,
      CategoryId categoryId,
      LocalDate occurredOn,
      String memo) {
    this.id = id;
    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.occurredOn = occurredOn;
    this.memo = memo;
    validateSelf();
  }

  public TransactionId id() {
    return this.id;
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
