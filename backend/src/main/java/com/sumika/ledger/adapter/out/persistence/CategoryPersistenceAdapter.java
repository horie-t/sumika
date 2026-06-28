package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.application.port.out.DeleteCategoryPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.SaveCategoryPort;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** カテゴリの out ポート群を JPA で実装する永続化アダプタ。すべて利用者でスコープする。 */
@Component
class CategoryPersistenceAdapter implements LoadCategoryPort, SaveCategoryPort, DeleteCategoryPort {

  private final CategoryJpaRepository repository;
  private final CategoryMapper mapper;

  CategoryPersistenceAdapter(CategoryJpaRepository repository, CategoryMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Category> loadCategory(UserId userId, CategoryId id) {
    return this.repository.findByUserIdAndId(userId.value(), id.value()).map(this.mapper::toDomain);
  }

  @Override
  public List<Category> loadAllCategories(UserId userId) {
    return this.repository.findByUserIdOrderById(userId.value()).stream()
        .map(this.mapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsCategory(UserId userId, CategoryId id) {
    return this.repository.existsByUserIdAndId(userId.value(), id.value());
  }

  @Override
  public Category saveCategory(UserId userId, Category category) {
    CategoryJpaEntity saved = this.repository.save(this.mapper.toJpaEntity(userId, category));
    return this.mapper.toDomain(saved);
  }

  @Override
  public void deleteCategory(UserId userId, CategoryId id) {
    this.repository.deleteByUserIdAndId(userId.value(), id.value());
  }
}
