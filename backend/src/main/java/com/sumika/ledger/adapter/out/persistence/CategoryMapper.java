package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import org.springframework.stereotype.Component;

/** ドメイン {@link Category} と {@link CategoryJpaEntity} の相互変換。 */
@Component
class CategoryMapper {

  CategoryJpaEntity toJpaEntity(Category category) {
    return new CategoryJpaEntity(
        category.id().map(CategoryId::value).orElse(null), category.name(), category.type());
  }

  Category toDomain(CategoryJpaEntity entity) {
    return Category.of(new CategoryId(entity.getId()), entity.getName(), entity.getType());
  }
}
