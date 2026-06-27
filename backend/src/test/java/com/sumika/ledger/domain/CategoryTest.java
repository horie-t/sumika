package com.sumika.ledger.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void createHasNoIdAndTrimsName() {
    Category category = Category.create("  食費  ", EntryType.EXPENSE);

    assertTrue(category.id().isEmpty());
    assertEquals("食費", category.name());
    assertEquals(EntryType.EXPENSE, category.type());
  }

  @Test
  void ofHasId() {
    Category category = Category.of(CategoryId.of(1), "給与", EntryType.INCOME);

    assertEquals(CategoryId.of(1), category.id().orElseThrow());
    assertEquals(EntryType.INCOME, category.type());
  }

  @Test
  void rejectsBlankName() {
    assertThrows(
        IllegalArgumentException.class, () -> Category.create("   ", EntryType.EXPENSE));
  }

  @Test
  void rejectsTooLongName() {
    String tooLong = "あ".repeat(Category.MAX_NAME_LENGTH + 1);
    assertThrows(
        IllegalArgumentException.class, () -> Category.create(tooLong, EntryType.EXPENSE));
  }

  @Test
  void rejectsNullType() {
    assertThrows(NullPointerException.class, () -> Category.create("食費", null));
  }
}
