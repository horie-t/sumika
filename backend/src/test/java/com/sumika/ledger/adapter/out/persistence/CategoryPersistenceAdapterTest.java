package com.sumika.ledger.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sumika.TestcontainersConfiguration;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * 実 PostgreSQL（Testcontainers）に対する永続化アダプタの検証。スキーマはエンティティから生成
 * （Flyway は無効化）。Flyway スキーマとエンティティの整合は {@code SumikaApplicationTests}
 * （ddl-auto=validate）で別途検証する。
 */
@DataJpaTest(
    properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, CategoryPersistenceAdapter.class, CategoryMapper.class})
class CategoryPersistenceAdapterTest {

  @Autowired private CategoryPersistenceAdapter adapter;

  @Test
  void savesAndLoadsCategory() {
    Category saved = this.adapter.saveCategory(Category.create("食費", EntryType.EXPENSE));
    assertTrue(saved.id().isPresent());

    Category loaded = this.adapter.loadCategory(saved.id().orElseThrow()).orElseThrow();
    assertEquals("食費", loaded.name());
    assertEquals(EntryType.EXPENSE, loaded.type());
  }

  @Test
  void loadsAllAndChecksExistence() {
    Category income = this.adapter.saveCategory(Category.create("給与", EntryType.INCOME));
    this.adapter.saveCategory(Category.create("食費", EntryType.EXPENSE));

    List<Category> all = this.adapter.loadAllCategories();
    assertEquals(2, all.size());
    assertTrue(this.adapter.existsCategory(income.id().orElseThrow()));
  }

  @Test
  void deletesCategory() {
    Category saved = this.adapter.saveCategory(Category.create("娯楽", EntryType.EXPENSE));
    CategoryId id = saved.id().orElseThrow();

    this.adapter.deleteCategory(id);

    assertFalse(this.adapter.existsCategory(id));
  }
}
