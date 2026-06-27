package com.sumika.ledger.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * 収支のカテゴリ（分類）。収入用・支出用のいずれかの種別を持つ。
 *
 * <p>不変オブジェクト。新規作成時は id を持たず（{@link #create}）、永続化済みのものは
 * id を持つ（{@link #of}）。
 */
public class Category {

  /** 名前の最大長。 */
  public static final int MAX_NAME_LENGTH = 50;

  private final CategoryId id;
  private final String name;
  private final EntryType type;

  private Category(CategoryId id, String name, EntryType type) {
    this.name = requireValidName(name);
    this.type = Objects.requireNonNull(type, "type must not be null");
    this.id = id;
  }

  /** 新規カテゴリ（未永続化）を生成する。 */
  public static Category create(String name, EntryType type) {
    return new Category(null, name, type);
  }

  /** 永続化済みカテゴリを再構築する。 */
  public static Category of(CategoryId id, String name, EntryType type) {
    return new Category(Objects.requireNonNull(id, "id must not be null"), name, type);
  }

  private static String requireValidName(String name) {
    Objects.requireNonNull(name, "name must not be null");
    String trimmed = name.strip();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (trimmed.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException("name must be " + MAX_NAME_LENGTH + " characters or fewer");
    }
    return trimmed;
  }

  public Optional<CategoryId> id() {
    return Optional.ofNullable(this.id);
  }

  public String name() {
    return this.name;
  }

  public EntryType type() {
    return this.type;
  }
}
