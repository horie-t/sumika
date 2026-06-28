package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.UserId;

/** カテゴリの削除 outgoing port。利用者でスコープする。 */
public interface DeleteCategoryPort {

  void deleteCategory(UserId userId, CategoryId id);
}
