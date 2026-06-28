package com.sumika.ledger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumika.TestcontainersConfiguration;
import com.sumika.support.MockJwtDecoderConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * HTTP からの 登録→取得→更新→削除 を実 PostgreSQL に対して通す E2E。
 *
 * <p>各テストは {@link Transactional} でロールバックして分離する（MockMvc は同一スレッドで
 * 実行されるためテストのトランザクションに参加する）。認証は {@code jwt()} で注入し、
 * JWT デコードは {@link MockJwtDecoderConfiguration} のモックで代替する（Keycloak 不要）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, MockJwtDecoderConfiguration.class})
@Transactional
class LedgerE2ETest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void registerGetUpdateDeleteHappyPath() throws Exception {
    long categoryId =
        idOf(
            this.mockMvc
                .perform(
                    post("/api/categories")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"food\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

    long transactionId =
        idOf(
            this.mockMvc
                .perform(
                    post("/api/transactions")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            "{\"type\":\"EXPENSE\",\"amount\":1200,\"categoryId\":"
                                + categoryId
                                + ",\"occurredOn\":\"2026-06-27\",\"memo\":\"lunch\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1200))
                .andReturn()
                .getResponse()
                .getContentAsString());

    this.mockMvc
        .perform(get("/api/transactions").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value((int) transactionId))
        .andExpect(jsonPath("$[0].memo").value("lunch"));

    this.mockMvc
        .perform(
            put("/api/transactions/" + transactionId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"type\":\"EXPENSE\",\"amount\":2000,\"categoryId\":"
                        + categoryId
                        + ",\"occurredOn\":\"2026-06-28\",\"memo\":\"dinner\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount").value(2000))
        .andExpect(jsonPath("$.memo").value("dinner"));

    this.mockMvc
        .perform(delete("/api/transactions/" + transactionId).with(jwt()))
        .andExpect(status().isNoContent());

    this.mockMvc
        .perform(get("/api/transactions").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void deletingCategoryInUseReturns409() throws Exception {
    long categoryId =
        idOf(
            this.mockMvc
                .perform(
                    post("/api/categories")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"utility\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

    this.mockMvc
        .perform(
            post("/api/transactions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"type\":\"EXPENSE\",\"amount\":500,\"categoryId\":"
                        + categoryId
                        + ",\"occurredOn\":\"2026-06-27\"}"))
        .andExpect(status().isCreated());

    this.mockMvc
        .perform(delete("/api/categories/" + categoryId).with(jwt()))
        .andExpect(status().isConflict());
  }

  private long idOf(String json) throws Exception {
    return this.objectMapper.readTree(json).get("id").asLong();
  }
}
