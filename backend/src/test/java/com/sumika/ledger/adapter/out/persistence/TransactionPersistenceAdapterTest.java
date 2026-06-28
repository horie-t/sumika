package com.sumika.ledger.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sumika.TestcontainersConfiguration;
import com.sumika.ledger.application.port.out.TransactionSearchCriteria;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import com.sumika.ledger.domain.UserId;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest(
    properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  TestcontainersConfiguration.class,
  TransactionPersistenceAdapter.class,
  TransactionMapper.class,
  CategoryPersistenceAdapter.class,
  CategoryMapper.class
})
class TransactionPersistenceAdapterTest {

  private static final UserId USER = UserId.of("user-1");

  @Autowired private TransactionPersistenceAdapter adapter;
  @Autowired private CategoryPersistenceAdapter categoryAdapter;

  private CategoryId newCategory(String name, EntryType type) {
    return this.categoryAdapter.saveCategory(USER, Category.create(name, type)).id().orElseThrow();
  }

  private CategoryId newCategory(UserId user, String name, EntryType type) {
    return this.categoryAdapter.saveCategory(user, Category.create(name, type)).id().orElseThrow();
  }

  @Test
  void savesAndLoadsTransaction() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);

    Transaction saved =
        this.adapter.saveTransaction(
            USER,
            Transaction.create(
                EntryType.EXPENSE, Money.of(1200), food, LocalDate.of(2026, 6, 27), "ランチ"));
    assertTrue(saved.id().isPresent());

    Transaction loaded = this.adapter.loadTransaction(USER, saved.id().orElseThrow()).orElseThrow();
    assertEquals(Money.of(1200), loaded.amount());
    assertEquals(food, loaded.categoryId());
    assertEquals("ランチ", loaded.memo().orElseThrow());
  }

  @Test
  void searchFiltersByCategoryAndDateRangeAndSortsDescending() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);
    CategoryId salary = newCategory("給与", EntryType.INCOME);
    this.adapter.saveTransaction(
        USER,
        Transaction.create(EntryType.EXPENSE, Money.of(500), food, LocalDate.of(2026, 6, 1), null));
    this.adapter.saveTransaction(
        USER,
        Transaction.create(EntryType.EXPENSE, Money.of(800), food, LocalDate.of(2026, 6, 20), null));
    this.adapter.saveTransaction(
        USER,
        Transaction.create(
            EntryType.INCOME, Money.of(300000), salary, LocalDate.of(2026, 6, 25), null));

    assertEquals(3, this.adapter.findTransactions(USER, TransactionSearchCriteria.all()).size());
    assertEquals(
        2,
        this.adapter
            .findTransactions(USER, new TransactionSearchCriteria(null, null, food))
            .size());

    List<Transaction> ranged =
        this.adapter.findTransactions(
            USER,
            new TransactionSearchCriteria(
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 30), null));
    assertEquals(2, ranged.size());
    // 発生日の新しい順
    assertEquals(LocalDate.of(2026, 6, 25), ranged.get(0).occurredOn());
  }

  @Test
  void updatesExistingTransaction() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);
    Transaction saved =
        this.adapter.saveTransaction(
            USER,
            Transaction.create(
                EntryType.EXPENSE, Money.of(1000), food, LocalDate.of(2026, 6, 27), "old"));
    TransactionId id = saved.id().orElseThrow();

    this.adapter.saveTransaction(
        USER,
        Transaction.of(
            id, EntryType.EXPENSE, Money.of(2000), food, LocalDate.of(2026, 6, 28), "new"));

    Transaction loaded = this.adapter.loadTransaction(USER, id).orElseThrow();
    assertEquals(Money.of(2000), loaded.amount());
    assertEquals("new", loaded.memo().orElseThrow());
  }

  @Test
  void deletesTransaction() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);
    Transaction saved =
        this.adapter.saveTransaction(
            USER,
            Transaction.create(EntryType.EXPENSE, Money.of(100), food, LocalDate.of(2026, 6, 27), null));
    TransactionId id = saved.id().orElseThrow();

    this.adapter.deleteTransaction(USER, id);

    assertTrue(this.adapter.loadTransaction(USER, id).isEmpty());
  }

  @Test
  void findTransactionsIsScopedToUser() {
    UserId other = UserId.of("user-2");
    CategoryId mine = newCategory("食費", EntryType.EXPENSE);
    CategoryId theirs = newCategory(other, "食費", EntryType.EXPENSE);
    this.adapter.saveTransaction(
        USER,
        Transaction.create(EntryType.EXPENSE, Money.of(500), mine, LocalDate.of(2026, 6, 1), null));
    this.adapter.saveTransaction(
        other,
        Transaction.create(EntryType.EXPENSE, Money.of(800), theirs, LocalDate.of(2026, 6, 2), null));

    List<Transaction> rows = this.adapter.findTransactions(USER, TransactionSearchCriteria.all());
    assertEquals(1, rows.size());
    assertEquals(Money.of(500), rows.get(0).amount());
  }
}
