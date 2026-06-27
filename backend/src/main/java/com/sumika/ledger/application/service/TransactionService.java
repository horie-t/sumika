package com.sumika.ledger.application.service;

import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.DeleteTransactionUseCase;
import com.sumika.ledger.application.port.in.GetTransactionsQuery;
import com.sumika.ledger.application.port.in.RegisterTransactionCommand;
import com.sumika.ledger.application.port.in.RegisterTransactionUseCase;
import com.sumika.ledger.application.port.in.UpdateTransactionCommand;
import com.sumika.ledger.application.port.in.UpdateTransactionUseCase;
import com.sumika.ledger.application.port.out.DeleteTransactionPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.LoadTransactionPort;
import com.sumika.ledger.application.port.out.SaveTransactionPort;
import com.sumika.ledger.application.port.out.TransactionSearchCriteria;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class TransactionService
    implements RegisterTransactionUseCase,
        UpdateTransactionUseCase,
        DeleteTransactionUseCase,
        GetTransactionsQuery {

  private final LoadTransactionPort loadTransactionPort;
  private final SaveTransactionPort saveTransactionPort;
  private final DeleteTransactionPort deleteTransactionPort;
  private final LoadCategoryPort loadCategoryPort;

  TransactionService(
      LoadTransactionPort loadTransactionPort,
      SaveTransactionPort saveTransactionPort,
      DeleteTransactionPort deleteTransactionPort,
      LoadCategoryPort loadCategoryPort) {
    this.loadTransactionPort = loadTransactionPort;
    this.saveTransactionPort = saveTransactionPort;
    this.deleteTransactionPort = deleteTransactionPort;
    this.loadCategoryPort = loadCategoryPort;
  }

  @Override
  public Transaction registerTransaction(RegisterTransactionCommand command) {
    requireConsistentCategory(command.categoryId(), command.type());
    Transaction transaction =
        Transaction.create(
            command.type(),
            command.amount(),
            command.categoryId(),
            command.occurredOn(),
            command.memo());
    return this.saveTransactionPort.saveTransaction(transaction);
  }

  @Override
  public Transaction updateTransaction(UpdateTransactionCommand command) {
    requireTransactionExists(command.id());
    requireConsistentCategory(command.categoryId(), command.type());
    Transaction transaction =
        Transaction.of(
            command.id(),
            command.type(),
            command.amount(),
            command.categoryId(),
            command.occurredOn(),
            command.memo());
    return this.saveTransactionPort.saveTransaction(transaction);
  }

  @Override
  public void deleteTransaction(TransactionId id) {
    requireTransactionExists(id);
    this.deleteTransactionPort.deleteTransaction(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Transaction> getTransactions(LocalDate from, LocalDate to, CategoryId categoryId) {
    return this.loadTransactionPort.findTransactions(
        new TransactionSearchCriteria(from, to, categoryId));
  }

  private void requireTransactionExists(TransactionId id) {
    if (this.loadTransactionPort.loadTransaction(id).isEmpty()) {
      throw new ResourceNotFoundException("収支記録が見つかりません: " + id.value());
    }
  }

  /** カテゴリの存在と、収支種別がカテゴリ種別に一致することを検証する。 */
  private void requireConsistentCategory(CategoryId categoryId, EntryType type) {
    Category category =
        this.loadCategoryPort
            .loadCategory(categoryId)
            .orElseThrow(
                () -> new ResourceNotFoundException("カテゴリが見つかりません: " + categoryId.value()));
    if (category.type() != type) {
      throw new IllegalArgumentException("収支の種別がカテゴリの種別と一致しません");
    }
  }
}
