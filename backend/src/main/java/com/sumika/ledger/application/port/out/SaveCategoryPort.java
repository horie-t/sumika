package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.Category;

/** カテゴリの保存 outgoing port。永続化後（id 付き）のカテゴリを返す。 */
public interface SaveCategoryPort {

  Category saveCategory(Category category);
}
