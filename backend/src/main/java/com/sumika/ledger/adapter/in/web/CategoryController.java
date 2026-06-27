package com.sumika.ledger.adapter.in.web;

import com.sumika.ledger.adapter.in.web.dto.CategoryRequest;
import com.sumika.ledger.adapter.in.web.dto.CategoryResponse;
import com.sumika.ledger.application.port.in.GetCategoriesQuery;
import com.sumika.ledger.application.port.in.ManageCategoryUseCase;
import com.sumika.ledger.application.port.in.RegisterCategoryCommand;
import com.sumika.ledger.application.port.in.UpdateCategoryCommand;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
class CategoryController {

  private final ManageCategoryUseCase manageCategoryUseCase;
  private final GetCategoriesQuery getCategoriesQuery;

  CategoryController(
      ManageCategoryUseCase manageCategoryUseCase, GetCategoriesQuery getCategoriesQuery) {
    this.manageCategoryUseCase = manageCategoryUseCase;
    this.getCategoriesQuery = getCategoriesQuery;
  }

  @PostMapping
  ResponseEntity<CategoryResponse> register(@Valid @RequestBody CategoryRequest request) {
    Category created =
        this.manageCategoryUseCase.registerCategory(
            new RegisterCategoryCommand(request.name(), request.type()));
    CategoryResponse body = CategoryResponse.from(created);
    return ResponseEntity.created(URI.create("/api/categories/" + body.id())).body(body);
  }

  @GetMapping
  List<CategoryResponse> list() {
    return this.getCategoriesQuery.getAllCategories().stream().map(CategoryResponse::from).toList();
  }

  @PutMapping("/{id}")
  CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    Category updated =
        this.manageCategoryUseCase.updateCategory(
            new UpdateCategoryCommand(CategoryId.of(id), request.name(), request.type()));
    return CategoryResponse.from(updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable Long id) {
    this.manageCategoryUseCase.deleteCategory(CategoryId.of(id));
  }
}
