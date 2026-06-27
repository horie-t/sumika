package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;

/** カテゴリの登録・更新・削除ユースケース。 */
public interface ManageCategoryUseCase {

  Category registerCategory(RegisterCategoryCommand command);

  Category updateCategory(UpdateCategoryCommand command);

  void deleteCategory(CategoryId id);
}
