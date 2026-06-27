package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import java.util.List;
import java.util.Optional;

/** カテゴリの取得 outgoing port。 */
public interface LoadCategoryPort {

  Optional<Category> loadCategory(CategoryId id);

  List<Category> loadAllCategories();

  boolean existsCategory(CategoryId id);
}
