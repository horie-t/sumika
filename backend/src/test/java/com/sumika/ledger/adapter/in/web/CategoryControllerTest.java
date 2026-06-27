package com.sumika.ledger.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sumika.common.ResourceNotFoundException;
import com.sumika.ledger.application.port.in.GetCategoriesQuery;
import com.sumika.ledger.application.port.in.ManageCategoryUseCase;
import com.sumika.ledger.domain.Category;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.EntryType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ManageCategoryUseCase manageCategoryUseCase;
  @MockitoBean private GetCategoriesQuery getCategoriesQuery;

  @Test
  void registerReturns201WithLocation() throws Exception {
    when(this.manageCategoryUseCase.registerCategory(any()))
        .thenReturn(Category.of(CategoryId.of(1), "food", EntryType.EXPENSE));

    this.mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"food\",\"type\":\"EXPENSE\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/categories/1"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("food"))
        .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  void listReturnsCategories() throws Exception {
    when(this.getCategoriesQuery.getAllCategories())
        .thenReturn(List.of(Category.of(CategoryId.of(1), "food", EntryType.EXPENSE)));

    this.mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("food"));
  }

  @Test
  void registerRejectsInvalidBody() throws Exception {
    this.mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"type\":\"EXPENSE\"}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(this.manageCategoryUseCase);
  }

  @Test
  void deleteReturns204() throws Exception {
    this.mockMvc.perform(delete("/api/categories/5")).andExpect(status().isNoContent());

    verify(this.manageCategoryUseCase).deleteCategory(CategoryId.of(5));
  }

  @Test
  void updateReturns404AsProblemDetail() throws Exception {
    when(this.manageCategoryUseCase.updateCategory(any()))
        .thenThrow(new ResourceNotFoundException("カテゴリが見つかりません: 99"));

    this.mockMvc
        .perform(
            put("/api/categories/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"food\",\"type\":\"EXPENSE\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("カテゴリが見つかりません: 99"));
  }
}
