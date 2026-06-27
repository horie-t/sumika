package com.sumika.ledger.application.service;

import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.GetCategoriesQuery;
import com.sumika.ledger.application.port.in.ManageCategoryUseCase;
import com.sumika.ledger.application.port.in.RegisterCategoryCommand;
import com.sumika.ledger.application.port.in.UpdateCategoryCommand;
import com.sumika.ledger.application.port.out.DeleteCategoryPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.SaveCategoryPort;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CategoryService implements ManageCategoryUseCase, GetCategoriesQuery {

  private final LoadCategoryPort loadCategoryPort;
  private final SaveCategoryPort saveCategoryPort;
  private final DeleteCategoryPort deleteCategoryPort;

  CategoryService(
      LoadCategoryPort loadCategoryPort,
      SaveCategoryPort saveCategoryPort,
      DeleteCategoryPort deleteCategoryPort) {
    this.loadCategoryPort = loadCategoryPort;
    this.saveCategoryPort = saveCategoryPort;
    this.deleteCategoryPort = deleteCategoryPort;
  }

  @Override
  public Category registerCategory(RegisterCategoryCommand command) {
    return this.saveCategoryPort.saveCategory(Category.create(command.name(), command.type()));
  }

  @Override
  public Category updateCategory(UpdateCategoryCommand command) {
    requireCategoryExists(command.id());
    return this.saveCategoryPort.saveCategory(
        Category.of(command.id(), command.name(), command.type()));
  }

  @Override
  public void deleteCategory(CategoryId id) {
    requireCategoryExists(id);
    this.deleteCategoryPort.deleteCategory(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> getAllCategories() {
    return this.loadCategoryPort.loadAllCategories();
  }

  private void requireCategoryExists(CategoryId id) {
    if (!this.loadCategoryPort.existsCategory(id)) {
      throw new ResourceNotFoundException("category not found: " + id.value());
    }
  }
}
