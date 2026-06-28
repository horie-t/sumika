package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.application.port.in.CategorySummaryLine;
import com.sumika.ledger.domain.EntryType;

/** カテゴリ別内訳 1 行のレスポンス。 */
public record CategorySummaryLineResponse(
    Long categoryId, String categoryName, EntryType type, long total) {

  public static CategorySummaryLineResponse from(CategorySummaryLine line) {
    return new CategorySummaryLineResponse(
        line.categoryId(), line.categoryName(), line.type(), line.total().amount().longValueExact());
  }
}
