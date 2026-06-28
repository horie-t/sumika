package com.sumika.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.RegisterTransactionCommand;
import com.sumika.ledger.application.port.in.UpdateTransactionCommand;
import com.sumika.ledger.application.port.out.CurrentUserProvider;
import com.sumika.ledger.application.port.out.DeleteTransactionPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.LoadTransactionPort;
import com.sumika.ledger.application.port.out.SaveTransactionPort;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  private static final CategoryId FOOD = CategoryId.of(1);
  private static final LocalDate DATE = LocalDate.of(2026, 6, 27);
  private static final UserId USER = UserId.of("user-1");

  @Mock private LoadTransactionPort loadTransactionPort;
  @Mock private SaveTransactionPort saveTransactionPort;
  @Mock private DeleteTransactionPort deleteTransactionPort;
  @Mock private LoadCategoryPort loadCategoryPort;
  @Mock private CurrentUserProvider currentUserProvider;

  private TransactionService service;

  @BeforeEach
  void setUp() {
    when(this.currentUserProvider.currentUserId()).thenReturn(USER);
    this.service =
        new TransactionService(
            this.loadTransactionPort,
            this.saveTransactionPort,
            this.deleteTransactionPort,
            this.loadCategoryPort,
            this.currentUserProvider);
  }

  private void expenseCategoryExists() {
    when(this.loadCategoryPort.loadCategory(eq(USER), eq(FOOD)))
        .thenReturn(Optional.of(Category.of(FOOD, "食費", EntryType.EXPENSE)));
  }

  @Test
  void registersTransactionWhenCategoryConsistent() {
    expenseCategoryExists();
    when(this.saveTransactionPort.saveTransaction(eq(USER), any()))
        .thenAnswer(
            invocation -> {
              Transaction t = invocation.getArgument(1);
              return Transaction.of(
                  TransactionId.of(7),
                  t.type(),
                  t.amount(),
                  t.categoryId(),
                  t.occurredOn(),
                  t.memo().orElse(null));
            });

    Transaction result =
        this.service.registerTransaction(
            new RegisterTransactionCommand(EntryType.EXPENSE, Money.of(1200), FOOD, DATE, "ランチ"));

    assertThat(result.id()).contains(TransactionId.of(7));
  }

  @Test
  void registerRejectsTypeMismatch() {
    expenseCategoryExists();

    assertThatThrownBy(
            () ->
                this.service.registerTransaction(
                    new RegisterTransactionCommand(
                        EntryType.INCOME, Money.of(100), FOOD, DATE, null)))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(this.saveTransactionPort);
  }

  @Test
  void registerRejectsMissingCategory() {
    when(this.loadCategoryPort.loadCategory(eq(USER), eq(FOOD))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                this.service.registerTransaction(
                    new RegisterTransactionCommand(
                        EntryType.EXPENSE, Money.of(100), FOOD, DATE, null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateRejectsMissingTransaction() {
    when(this.loadTransactionPort.loadTransaction(eq(USER), eq(TransactionId.of(9))))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                this.service.updateTransaction(
                    new UpdateTransactionCommand(
                        TransactionId.of(9), EntryType.EXPENSE, Money.of(100), FOOD, DATE, null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getTransactionsDelegatesWithCriteria() {
    when(this.loadTransactionPort.findTransactions(eq(USER), any())).thenReturn(List.of());

    this.service.getTransactions(DATE, DATE, FOOD);

    ArgumentCaptor<TransactionSearchCriteria> captor =
        ArgumentCaptor.forClass(TransactionSearchCriteria.class);
    verify(this.loadTransactionPort).findTransactions(eq(USER), captor.capture());
    assertThat(captor.getValue().categoryId()).isEqualTo(FOOD);
    assertThat(captor.getValue().from()).isEqualTo(DATE);
  }

  @Test
  void deletesExistingTransaction() {
    when(this.loadTransactionPort.loadTransaction(eq(USER), eq(TransactionId.of(3))))
        .thenReturn(
            Optional.of(
                Transaction.of(
                    TransactionId.of(3), EntryType.EXPENSE, Money.of(100), FOOD, DATE, null)));

    this.service.deleteTransaction(TransactionId.of(3));

    verify(this.deleteTransactionPort).deleteTransaction(eq(USER), eq(TransactionId.of(3)));
  }
}
