package com.sumika.ledger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumika.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP からの 登録→取得→更新→削除 を実 PostgreSQL に対して通すハッピーパス E2E。 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
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
        .perform(get("/api/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value((int) transactionId))
        .andExpect(jsonPath("$[0].memo").value("lunch"));

    this.mockMvc
        .perform(
            put("/api/transactions/" + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"type\":\"EXPENSE\",\"amount\":2000,\"categoryId\":"
                        + categoryId
                        + ",\"occurredOn\":\"2026-06-28\",\"memo\":\"dinner\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount").value(2000))
        .andExpect(jsonPath("$.memo").value("dinner"));

    this.mockMvc
        .perform(delete("/api/transactions/" + transactionId))
        .andExpect(status().isNoContent());

    this.mockMvc
        .perform(get("/api/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  private long idOf(String json) throws Exception {
    return this.objectMapper.readTree(json).get("id").asLong();
  }
}
