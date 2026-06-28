package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.UserId;
import java.util.List;
import java.util.Optional;

/** カテゴリの取得 outgoing port。すべて利用者でスコープする。 */
public interface LoadCategoryPort {

  Optional<Category> loadCategory(UserId userId, CategoryId id);

  List<Category> loadAllCategories(UserId userId);

  boolean existsCategory(UserId userId, CategoryId id);
}
