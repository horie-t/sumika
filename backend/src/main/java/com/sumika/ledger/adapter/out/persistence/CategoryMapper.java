package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.UserId;
import org.springframework.stereotype.Component;

/** ドメイン {@link Category} と {@link CategoryJpaEntity} の相互変換。 */
@Component
class CategoryMapper {

  CategoryJpaEntity toJpaEntity(UserId userId, Category category) {
    return new CategoryJpaEntity(
        category.id().map(CategoryId::value).orElse(null),
        userId.value(),
        category.name(),
        category.type());
  }

  Category toDomain(CategoryJpaEntity entity) {
    return Category.of(new CategoryId(entity.getId()), entity.getName(), entity.getType());
  }
}
