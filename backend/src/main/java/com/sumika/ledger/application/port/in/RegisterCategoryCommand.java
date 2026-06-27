package com.sumika.ledger.application.port.in;

import com.sumika.common.SelfValidating;
import com.sumika.ledger.domain.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** カテゴリ登録の入力モデル。 */
public class RegisterCategoryCommand extends SelfValidating<RegisterCategoryCommand> {

  @NotBlank
  @Size(max = 50)
  private final String name;

  @NotNull private final EntryType type;

  public RegisterCategoryCommand(String name, EntryType type) {
    this.name = name;
    this.type = type;
    validateSelf();
  }

  public String name() {
    return this.name;
  }

  public EntryType type() {
    return this.type;
  }
}
