package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Category;
import java.util.List;

/** カテゴリ取得クエリ。 */
public interface GetCategoriesQuery {

  List<Category> getAllCategories();
}
