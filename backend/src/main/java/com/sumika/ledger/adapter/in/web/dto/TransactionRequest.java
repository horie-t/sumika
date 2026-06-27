package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.domain.EntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 収支記録の作成/更新リクエスト。金額は最小通貨単位（円）。 */
public record TransactionRequest(
    @NotNull EntryType type,
    @NotNull @Positive Long amount,
    @NotNull Long categoryId,
    @NotNull LocalDate occurredOn,
    @Size(max = 255) String memo) {}
