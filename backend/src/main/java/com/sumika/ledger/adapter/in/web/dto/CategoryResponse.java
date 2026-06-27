package com.sumika.ledger.adapter.in.web.dto;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;

/** カテゴリのレスポンス。 */
public record CategoryResponse(Long id, String name, EntryType type) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(
        category.id().map(CategoryId::value).orElse(null), category.name(), category.type());
  }
}
