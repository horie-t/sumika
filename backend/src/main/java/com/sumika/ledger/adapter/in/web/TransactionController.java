package com.sumika.ledger.adapter.in.web;

import com.sumika.ledger.adapter.in.web.dto.TransactionRequest;
import com.sumika.ledger.adapter.in.web.dto.TransactionResponse;
import com.sumika.ledger.application.port.in.DeleteTransactionUseCase;
import com.sumika.ledger.application.port.in.GetTransactionsQuery;
import com.sumika.ledger.application.port.in.RegisterTransactionCommand;
import com.sumika.ledger.application.port.in.RegisterTransactionUseCase;
import com.sumika.ledger.application.port.in.UpdateTransactionCommand;
import com.sumika.ledger.application.port.in.UpdateTransactionUseCase;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
class TransactionController {

  private final RegisterTransactionUseCase registerTransactionUseCase;
  private final UpdateTransactionUseCase updateTransactionUseCase;
  private final DeleteTransactionUseCase deleteTransactionUseCase;
  private final GetTransactionsQuery getTransactionsQuery;

  TransactionController(
      RegisterTransactionUseCase registerTransactionUseCase,
      UpdateTransactionUseCase updateTransactionUseCase,
      DeleteTransactionUseCase deleteTransactionUseCase,
      GetTransactionsQuery getTransactionsQuery) {
    this.registerTransactionUseCase = registerTransactionUseCase;
    this.updateTransactionUseCase = updateTransactionUseCase;
    this.deleteTransactionUseCase = deleteTransactionUseCase;
    this.getTransactionsQuery = getTransactionsQuery;
  }

  @PostMapping
  ResponseEntity<TransactionResponse> register(@Valid @RequestBody TransactionRequest request) {
    Transaction created =
        this.registerTransactionUseCase.registerTransaction(
            new RegisterTransactionCommand(
                request.type(),
                Money.of(request.amount()),
                CategoryId.of(request.categoryId()),
                request.occurredOn(),
                request.memo()));
    TransactionResponse body = TransactionResponse.from(created);
    return ResponseEntity.created(URI.create("/api/transactions/" + body.id())).body(body);
  }

  @GetMapping
  List<TransactionResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) Long categoryId) {
    CategoryId category = categoryId == null ? null : CategoryId.of(categoryId);
    return this.getTransactionsQuery.getTransactions(from, to, category).stream()
        .map(TransactionResponse::from)
        .toList();
  }

  @PutMapping("/{id}")
  TransactionResponse update(
      @PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
    Transaction updated =
        this.updateTransactionUseCase.updateTransaction(
            new UpdateTransactionCommand(
                TransactionId.of(id),
                request.type(),
                Money.of(request.amount()),
                CategoryId.of(request.categoryId()),
                request.occurredOn(),
                request.memo()));
    return TransactionResponse.from(updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable Long id) {
    this.deleteTransactionUseCase.deleteTransaction(TransactionId.of(id));
  }
}
