package com.sumika.ledger.web;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code /api/reports} の集計 API を controller→service→DB まで実 HTTP で通す E2E（REST Assured）。
 * 認証は基底クラス（Testcontainers Keycloak）が付与する。
 */
class ReportApiE2ETest extends AbstractAuthenticatedApiE2ETest {

  @Test
  @DisplayName("月次サマリ: 収入計・支出計・差引とカテゴリ別内訳を集計する")
  void monthlySummaryAggregatesTotalsAndBreakdown() {
    long salary = createCategory("給与", "INCOME");
    long food = createCategory("食費", "EXPENSE");
    createTransaction("INCOME", 300000, salary, "2026-06-25");
    createTransaction("EXPENSE", 1200, food, "2026-06-10");
    createTransaction("EXPENSE", 800, food, "2026-06-20");
    createTransaction("EXPENSE", 9999, food, "2026-05-31");

    authed()
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

    authed()
        .accept(ContentType.JSON)
        .queryParam("from", "2026-04")
        .queryParam("to", "2026-06")
        .when()
        .get("/reports/monthly-trend")
        .then()
        .statusCode(200)
        .body("size()", equalTo(3))
        .body("find { it.month == '2026-04' }.income", equalTo(0))
        .body("find { it.month == '2026-04' }.expense", equalTo(0))
        .body("find { it.month == '2026-05' }.income", equalTo(1000))
        .body("find { it.month == '2026-06' }.expense", equalTo(2000));
  }

  private long createCategory(String name, String type) {
    return authed()
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
    authed()
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
