package com.sumika.ledger.application.port.in;

import com.sumika.common.SelfValidating;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** カテゴリ更新の入力モデル。 */
public class UpdateCategoryCommand extends SelfValidating<UpdateCategoryCommand> {

  @NotNull private final CategoryId id;

  @NotBlank
  @Size(max = 50)
  private final String name;

  @NotNull private final EntryType type;

  public UpdateCategoryCommand(CategoryId id, String name, EntryType type) {
    this.id = id;
    this.name = name;
    this.type = type;
    validateSelf();
  }

  public CategoryId id() {
    return this.id;
  }

  public String name() {
    return this.name;
  }

  public EntryType type() {
    return this.type;
  }
}
