package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.CategoryId;

/** カテゴリの削除 outgoing port。 */
public interface DeleteCategoryPort {

  void deleteCategory(CategoryId id);
}
