package com.sumika.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sumika.common.CategoryInUseException;
import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.RegisterCategoryCommand;
import com.sumika.ledger.application.port.in.UpdateCategoryCommand;
import com.sumika.ledger.application.port.out.DeleteCategoryPort;
import com.sumika.ledger.application.port.out.LoadCategoryPort;
import com.sumika.ledger.application.port.out.LoadTransactionPort;
import com.sumika.ledger.application.port.out.SaveCategoryPort;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private LoadCategoryPort loadCategoryPort;
  @Mock private SaveCategoryPort saveCategoryPort;
  @Mock private DeleteCategoryPort deleteCategoryPort;
  @Mock private LoadTransactionPort loadTransactionPort;

  private CategoryService service;

  @BeforeEach
  void setUp() {
    this.service =
        new CategoryService(
            this.loadCategoryPort,
            this.saveCategoryPort,
            this.deleteCategoryPort,
            this.loadTransactionPort);
  }

  @Test
  void registersNewCategory() {
    when(this.saveCategoryPort.saveCategory(any()))
        .thenAnswer(
            invocation -> {
              Category c = invocation.getArgument(0);
              return Category.of(CategoryId.of(1), c.name(), c.type());
            });

    Category result =
        this.service.registerCategory(new RegisterCategoryCommand("食費", EntryType.EXPENSE));

    assertThat(result.id()).contains(CategoryId.of(1));
    ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
    verify(this.saveCategoryPort).saveCategory(captor.capture());
    assertThat(captor.getValue().id()).isEmpty();
    assertThat(captor.getValue().name()).isEqualTo("食費");
  }

  @Test
  void updateFailsWhenCategoryMissing() {
    when(this.loadCategoryPort.existsCategory(CategoryId.of(99))).thenReturn(false);

    assertThatThrownBy(
            () ->
                this.service.updateCategory(
                    new UpdateCategoryCommand(CategoryId.of(99), "食費", EntryType.EXPENSE)))
        .isInstanceOf(ResourceNotFoundException.class);
    verifyNoInteractions(this.saveCategoryPort);
  }

  @Test
  void deletesExistingCategory() {
    when(this.loadCategoryPort.existsCategory(CategoryId.of(5))).thenReturn(true);
    when(this.loadTransactionPort.existsByCategory(CategoryId.of(5))).thenReturn(false);

    this.service.deleteCategory(CategoryId.of(5));

    verify(this.deleteCategoryPort).deleteCategory(CategoryId.of(5));
  }

  @Test
  void deleteFailsWhenCategoryInUse() {
    when(this.loadCategoryPort.existsCategory(CategoryId.of(5))).thenReturn(true);
    when(this.loadTransactionPort.existsByCategory(CategoryId.of(5))).thenReturn(true);

    assertThatThrownBy(() -> this.service.deleteCategory(CategoryId.of(5)))
        .isInstanceOf(CategoryInUseException.class);
    verifyNoInteractions(this.deleteCategoryPort);
  }

  @Test
  void getAllDelegatesToPort() {
    when(this.loadCategoryPort.loadAllCategories())
        .thenReturn(List.of(Category.of(CategoryId.of(1), "食費", EntryType.EXPENSE)));

    assertThat(this.service.getAllCategories()).hasSize(1);
  }
}
