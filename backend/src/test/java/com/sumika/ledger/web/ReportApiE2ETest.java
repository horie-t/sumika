package com.sumika.ledger.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.sumika.TestcontainersConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * {@code /api/reports} の集計 API を controller→service→DB まで実 HTTP で通す E2E（REST Assured）。
 *
 * <p>{@link ReportControllerTest}（service mock）/{@link
 * com.sumika.ledger.adapter.out.persistence.ReportPersistenceAdapterTest}（DB 集計）を、
 * 実ポート・実 DB で結合して検証する。各テスト前に {@link Sql}（{@code /sql/clean.sql}）で分離する。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReportApiE2ETest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = this.port;
    RestAssured.basePath = "/api";
  }

  @Test
  @DisplayName("月次サマリ: 収入計・支出計・差引とカテゴリ別内訳を集計する")
  void monthlySummaryAggregatesTotalsAndBreakdown() {
    long salary = createCategory("給与", "INCOME");
    long food = createCategory("食費", "EXPENSE");
    createTransaction("INCOME", 300000, salary, "2026-06-25");
    createTransaction("EXPENSE", 1200, food, "2026-06-10");
    createTransaction("EXPENSE", 800, food, "2026-06-20");
    // 範囲外（5月）は当月サマリに含めない
    createTransaction("EXPENSE", 9999, food, "2026-05-31");

    given()
        .accept(ContentType.JSON)
        .queryParam("month", "2026-06")
        .when()
        .get("/reports/monthly-summary")
        .then()
        .statusCode(200)
        .body("month", equalTo("2026-06"))
        .body("totalIncome", equalTo(300000))
        .body("totalExpense", equalTo(2000))
        .body("net", equalTo(298000))
        .body("categories.size()", equalTo(2))
        // 収入が先に並ぶ
        .body("categories[0].type", equalTo("INCOME"))
        .body("categories.find { it.categoryName == '食費' }.total", equalTo(2000))
        .body("categories.find { it.categoryName == '給与' }.total", equalTo(300000));
  }

  @Test
  @DisplayName("月別推移: 各月の収入/支出を返し、欠損月は 0 で埋める")
  void monthlyTrendFillsMissingMonths() {
    long salary = createCategory("給与", "INCOME");
    long food = createCategory("食費", "EXPENSE");
    createTransaction("INCOME", 1000, salary, "2026-05-10");
    createTransaction("EXPENSE", 2000, food, "2026-06-15");

    given()
        .accept(ContentType.JSON)
        .queryParam("from", "2026-04")
        .queryParam("to", "2026-06")
        .when()
        .get("/reports/monthly-trend")
        .then()
        .statusCode(200)
        .body("size()", equalTo(3))
        // 4月はデータなし → 0 埋め
        .body("find { it.month == '2026-04' }.income", equalTo(0))
        .body("find { it.month == '2026-04' }.expense", equalTo(0))
        .body("find { it.month == '2026-05' }.income", equalTo(1000))
        .body("find { it.month == '2026-06' }.expense", equalTo(2000));
  }

  private long createCategory(String name, String type) {
    return given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"%s\",\"type\":\"%s\"}".formatted(name, type))
        .when()
        .post("/categories")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");
  }

  private void createTransaction(String type, long amount, long categoryId, String occurredOn) {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"type\":\"%s\",\"amount\":%d,\"categoryId\":%d,\"occurredOn\":\"%s\"}"
                .formatted(type, amount, categoryId, occurredOn))
        .when()
        .post("/transactions")
        .then()
        .statusCode(201);
  }
}
