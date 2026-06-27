package com.sumika.ledger.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sumika.ledger.application.port.in.DeleteTransactionUseCase;
import com.sumika.ledger.application.port.in.GetTransactionsQuery;
import com.sumika.ledger.application.port.in.RegisterTransactionUseCase;
import com.sumika.ledger.application.port.in.UpdateTransactionUseCase;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterTransactionUseCase registerTransactionUseCase;
  @MockitoBean private UpdateTransactionUseCase updateTransactionUseCase;
  @MockitoBean private DeleteTransactionUseCase deleteTransactionUseCase;
  @MockitoBean private GetTransactionsQuery getTransactionsQuery;

  @Test
  void registerReturns201() throws Exception {
    when(this.registerTransactionUseCase.registerTransaction(any()))
        .thenReturn(
            Transaction.of(
                TransactionId.of(7),
                EntryType.EXPENSE,
                Money.of(1200),
                CategoryId.of(1),
                LocalDate.of(2026, 6, 27),
                "lunch"));

    this.mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"type\":\"EXPENSE\",\"amount\":1200,\"categoryId\":1,"
                        + "\"occurredOn\":\"2026-06-27\",\"memo\":\"lunch\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.amount").value(1200))
        .andExpect(jsonPath("$.occurredOn").value("2026-06-27"));
  }

  @Test
  void registerRejectsNonPositiveAmount() throws Exception {
    this.mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"type\":\"EXPENSE\",\"amount\":0,\"categoryId\":1,"
                        + "\"occurredOn\":\"2026-06-27\"}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(this.registerTransactionUseCase);
  }

  @Test
  void listPassesFiltersToQuery() throws Exception {
    when(this.getTransactionsQuery.getTransactions(any(), any(), any()))
        .thenReturn(
            List.of(
                Transaction.of(
                    TransactionId.of(1),
                    EntryType.EXPENSE,
                    Money.of(500),
                    CategoryId.of(1),
                    LocalDate.of(2026, 6, 1),
                    null)));

    this.mockMvc
        .perform(get("/api/transactions").param("from", "2026-06-01").param("categoryId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].amount").value(500));

    verify(this.getTransactionsQuery)
        .getTransactions(eq(LocalDate.of(2026, 6, 1)), isNull(), eq(CategoryId.of(1)));
  }

  @Test
  void deleteReturns204() throws Exception {
    this.mockMvc.perform(delete("/api/transactions/3")).andExpect(status().isNoContent());

    verify(this.deleteTransactionUseCase).deleteTransaction(TransactionId.of(3));
  }
}
