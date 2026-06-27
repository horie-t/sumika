package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.application.port.out.DeleteCategoryPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.SaveCategoryPort;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** カテゴリの out ポート群を JPA で実装する永続化アダプタ。 */
@Component
class CategoryPersistenceAdapter
    implements LoadCategoryPort, SaveCategoryPort, DeleteCategoryPort {

  private final CategoryJpaRepository repository;
  private final CategoryMapper mapper;

  CategoryPersistenceAdapter(CategoryJpaRepository repository, CategoryMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Category> loadCategory(CategoryId id) {
    return this.repository.findById(id.value()).map(this.mapper::toDomain);
  }

  @Override
  public List<Category> loadAllCategories() {
    return this.repository.findAll().stream().map(this.mapper::toDomain).toList();
  }

  @Override
  public boolean existsCategory(CategoryId id) {
    return this.repository.existsById(id.value());
  }

  @Override
  public Category saveCategory(Category category) {
    CategoryJpaEntity saved = this.repository.save(this.mapper.toJpaEntity(category));
    return this.mapper.toDomain(saved);
  }

  @Override
  public void deleteCategory(CategoryId id) {
    this.repository.deleteById(id.value());
  }
}
