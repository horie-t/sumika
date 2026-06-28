package com.sumika.ledger.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * {@code POST /api/transactions} の入力組み合わせ E2E（REST Assured）。
 *
 * <p>組み合わせは PICT によるペアワイズ表（{@code resources/pairwise/transactions.csv}）で網羅する。
 * 認証は基底クラス（Testcontainers Keycloak）が付与する。
 */
class TransactionApiE2ETest extends AbstractAuthenticatedApiE2ETest {

  @ParameterizedTest(name = "[{index}] type={0} amount={1} catType={2} date={3} memo={4} -> {5}")
  @CsvFileSource(resources = "/pairwise/transactions.csv", numLinesToSkip = 1, delimiter = '\t')
  @DisplayName("取引登録APIのペアワイズ網羅")
  void registerTransactionMatrix(
      String type,
      String amountClass,
      String categoryType,
      String occurredOnClass,
      String memoClass,
      int expectStatus) {

    long categoryId = createCategory(categoryType);
    String body = transactionBody(type, amountClass, categoryId, occurredOnClass, memoClass);

    var response =
        authed()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/transactions")
            .then()
            .statusCode(expectStatus);

    if (expectStatus == 201) {
      response
          .body("id", notNullValue())
          .body("type", equalTo(type))
          .body("amount", equalTo((int) amountValue(amountClass)))
          .body("categoryId", equalTo((int) categoryId));
    } else {
      response.contentType("application/problem+json").body("detail", notNullValue());
    }
  }

  @Test
  @DisplayName("登録した取引が一覧GETに現れる（POST→GET往復スモーク）")
  void registeredTransactionAppearsInList() {
    long categoryId = createCategory("EXPENSE");

    long transactionId =
        authed()
            .contentType(ContentType.JSON)
            .body(transactionBody("EXPENSE", "POSITIVE", categoryId, "VALID", "MAX"))
            .when()
            .post("/transactions")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    authed()
        .accept(ContentType.JSON)
        .when()
        .get("/transactions")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1))
        .body("find { it.id == %d }.amount".formatted(transactionId), equalTo(1000))
        .body("find { it.id == %d }.categoryId".formatted(transactionId), equalTo((int) categoryId));
  }

  @Test
  @DisplayName("未認証は 401 を返す")
  void unauthenticatedReturns401() {
    io.restassured.RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/transactions")
        .then()
        .statusCode(401);
  }

  private long createCategory(String type) {
    return authed()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"cat\",\"type\":\"%s\"}".formatted(type))
        .when()
        .post("/categories")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");
  }

  private String transactionBody(
      String type, String amountClass, long categoryId, String occurredOnClass, String memoClass) {
    return """
        {"type":"%s","amount":%d,"categoryId":%d,"occurredOn":"%s","memo":"%s"}"""
        .formatted(
            type,
            amountValue(amountClass),
            categoryId,
            occurredOnValue(occurredOnClass),
            memoValue(memoClass));
  }

  private long amountValue(String amountClass) {
    return switch (amountClass) {
      case "POSITIVE" -> 1000L;
      case "ZERO" -> 0L;
      case "NEGATIVE" -> -1L;
      default -> throw new IllegalArgumentException("unknown amount class: " + amountClass);
    };
  }

  private String occurredOnValue(String occurredOnClass) {
    return switch (occurredOnClass) {
      case "VALID" -> "2026-06-27";
      case "FUTURE" -> "2999-01-01";
      case "MALFORMED" -> "not-a-date";
      default -> throw new IllegalArgumentException("unknown date class: " + occurredOnClass);
    };
  }

  private String memoValue(String memoClass) {
    return switch (memoClass) {
      case "ZERO" -> "";
      case "MAX" -> "a".repeat(255);
      case "OVER" -> "a".repeat(256);
      default -> throw new IllegalArgumentException("unknown memo class: " + memoClass);
    };
  }
}
