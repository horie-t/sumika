package com.sumika.ledger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.sumika.TestcontainersConfiguration;
import com.sumika.ledger.application.port.out.CategoryAmount;
import com.sumika.ledger.application.port.out.MonthlyAmount;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.Transaction;
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
  ReportPersistenceAdapter.class,
  TransactionPersistenceAdapter.class,
  TransactionMapper.class,
  CategoryPersistenceAdapter.class,
  CategoryMapper.class
})
class ReportPersistenceAdapterTest {

  private static final UserId USER = UserId.of("user-1");

  @Autowired private ReportPersistenceAdapter adapter;
  @Autowired private TransactionPersistenceAdapter txAdapter;
  @Autowired private CategoryPersistenceAdapter categoryAdapter;

  private CategoryId newCategory(String name, EntryType type) {
    return this.categoryAdapter.saveCategory(USER, Category.create(name, type)).id().orElseThrow();
  }

  private CategoryId newCategory(UserId user, String name, EntryType type) {
    return this.categoryAdapter.saveCategory(user, Category.create(name, type)).id().orElseThrow();
  }

  private void newTransaction(EntryType type, long amount, CategoryId category, LocalDate on) {
    this.txAdapter.saveTransaction(
        USER, Transaction.create(type, Money.of(amount), category, on, null));
  }

  private void newTransaction(
      UserId user, EntryType type, long amount, CategoryId category, LocalDate on) {
    this.txAdapter.saveTransaction(
        user, Transaction.create(type, Money.of(amount), category, on, null));
  }

  @Test
  void aggregatesByCategoryWithinRange() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);
    CategoryId salary = newCategory("給与", EntryType.INCOME);
    newTransaction(EntryType.EXPENSE, 500, food, LocalDate.of(2026, 6, 1));
    newTransaction(EntryType.EXPENSE, 800, food, LocalDate.of(2026, 6, 20));
    newTransaction(EntryType.INCOME, 300000, salary, LocalDate.of(2026, 6, 25));
    // 範囲外（7月）は集計に含めない
    newTransaction(EntryType.EXPENSE, 999, food, LocalDate.of(2026, 7, 1));

    List<CategoryAmount> rows =
        this.adapter.aggregateByCategory(
            USER, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

    assertThat(rows).hasSize(2);
    assertThat(rows)
        .anySatisfy(
            r -> {
              assertThat(r.categoryName()).isEqualTo("食費");
              assertThat(r.type()).isEqualTo(EntryType.EXPENSE);
              assertThat(r.total()).isEqualTo(1300L);
            })
        .anySatisfy(
            r -> {
              assertThat(r.categoryName()).isEqualTo("給与");
              assertThat(r.type()).isEqualTo(EntryType.INCOME);
              assertThat(r.total()).isEqualTo(300000L);
            });
  }

  @Test
  void aggregatesByMonthAndType() {
    CategoryId food = newCategory("食費", EntryType.EXPENSE);
    CategoryId salary = newCategory("給与", EntryType.INCOME);
    newTransaction(EntryType.INCOME, 1000, salary, LocalDate.of(2026, 5, 10));
    newTransaction(EntryType.EXPENSE, 500, food, LocalDate.of(2026, 6, 1));
    newTransaction(EntryType.EXPENSE, 800, food, LocalDate.of(2026, 6, 20));

    List<MonthlyAmount> rows =
        this.adapter.aggregateByMonth(USER, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30));

    assertThat(rows).hasSize(2);
    assertThat(rows)
        .anySatisfy(
            r -> {
              assertThat(r.year()).isEqualTo(2026);
              assertThat(r.month()).isEqualTo(5);
              assertThat(r.type()).isEqualTo(EntryType.INCOME);
              assertThat(r.total()).isEqualTo(1000L);
            })
        .anySatisfy(
            r -> {
              assertThat(r.year()).isEqualTo(2026);
              assertThat(r.month()).isEqualTo(6);
              assertThat(r.type()).isEqualTo(EntryType.EXPENSE);
              assertThat(r.total()).isEqualTo(1300L);
            });
  }

  @Test
  void aggregateByCategoryIsScopedToUser() {
    UserId other = UserId.of("user-2");
    CategoryId mine = newCategory("食費", EntryType.EXPENSE);
    CategoryId theirs = newCategory(other, "食費", EntryType.EXPENSE);
    newTransaction(EntryType.EXPENSE, 500, mine, LocalDate.of(2026, 6, 1));
    newTransaction(other, EntryType.EXPENSE, 800, theirs, LocalDate.of(2026, 6, 2));

    List<CategoryAmount> rows =
        this.adapter.aggregateByCategory(
            USER, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).total()).isEqualTo(500L);
  }
}
