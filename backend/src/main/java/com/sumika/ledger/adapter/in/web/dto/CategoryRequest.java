package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.domain.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** カテゴリの作成/更新リクエスト。 */
public record CategoryRequest(
    @NotBlank @Size(max = 50) String name, @NotNull EntryType type) {}
