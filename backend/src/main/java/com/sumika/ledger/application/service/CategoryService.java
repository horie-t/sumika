package com.sumika.ledger.application.service;

import com.sumika.common.CategoryInUseException;
import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.GetCategoriesQuery;
import com.sumika.ledger.application.port.in.ManageCategoryUseCase;
import com.sumika.ledger.application.port.in.RegisterCategoryCommand;
import com.sumika.ledger.application.port.in.UpdateCategoryCommand;
import com.sumika.ledger.application.port.out.CurrentUserProvider;
import com.sumika.ledger.application.port.out.DeleteCategoryPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.LoadTransactionPort;
import com.sumika.ledger.application.port.out.SaveCategoryPort;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CategoryService implements ManageCategoryUseCase, GetCategoriesQuery {

  private final LoadCategoryPort loadCategoryPort;
  private final SaveCategoryPort saveCategoryPort;
  private final DeleteCategoryPort deleteCategoryPort;
  private final LoadTransactionPort loadTransactionPort;
  private final CurrentUserProvider currentUserProvider;

  CategoryService(
      LoadCategoryPort loadCategoryPort,
      SaveCategoryPort saveCategoryPort,
      DeleteCategoryPort deleteCategoryPort,
      LoadTransactionPort loadTransactionPort,
      CurrentUserProvider currentUserProvider) {
    this.loadCategoryPort = loadCategoryPort;
    this.saveCategoryPort = saveCategoryPort;
    this.deleteCategoryPort = deleteCategoryPort;
    this.loadTransactionPort = loadTransactionPort;
    this.currentUserProvider = currentUserProvider;
  }

  @Override
  public Category registerCategory(RegisterCategoryCommand command) {
    UserId userId = this.currentUserProvider.currentUserId();
    return this.saveCategoryPort.saveCategory(
        userId, Category.create(command.name(), command.type()));
  }

  @Override
  public Category updateCategory(UpdateCategoryCommand command) {
    UserId userId = this.currentUserProvider.currentUserId();
    requireCategoryExists(userId, command.id());
    return this.saveCategoryPort.saveCategory(
        userId, Category.of(command.id(), command.name(), command.type()));
  }

  @Override
  public void deleteCategory(CategoryId id) {
    UserId userId = this.currentUserProvider.currentUserId();
    requireCategoryExists(userId, id);
    if (this.loadTransactionPort.existsByCategory(userId, id)) {
      throw new CategoryInUseException("このカテゴリは収支記録で使用されているため削除できません");
    }
    this.deleteCategoryPort.deleteCategory(userId, id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> getAllCategories() {
    return this.loadCategoryPort.loadAllCategories(this.currentUserProvider.currentUserId());
  }

  private void requireCategoryExists(UserId userId, CategoryId id) {
    if (!this.loadCategoryPort.existsCategory(userId, id)) {
      throw new ResourceNotFoundException("カテゴリが見つかりません: " + id.value());
    }
  }
}
