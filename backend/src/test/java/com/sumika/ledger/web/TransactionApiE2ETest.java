package com.sumika.ledger.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.sumika.TestcontainersConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * {@code POST /api/transactions} の入力組み合わせ E2E（REST Assured）。
 *
 * <p>組み合わせは PICT によるペアワイズ表（{@code resources/pairwise/transactions.csv}）で網羅し、
 * 全直積より少ない件数で全因子ペアを被覆する。期待ステータスは表側で導出済みのため、
 * テストは「組み立てて投げ、ステータス＋必要なら本文/ProblemDetail を検証」に徹する。
 *
 * <p>REST Assured は実ポート（別スレッド）で動くため {@link org.springframework.transaction.annotation.Transactional}
 * のロールバックは効かない。各テスト前に {@link Sql}（{@code /sql/clean.sql}）でテーブルを空にして分離する。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransactionApiE2ETest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = this.port;
    RestAssured.basePath = "/api";
  }

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
        given()
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
      // RFC 7807 ProblemDetail
      response.contentType("application/problem+json").body("detail", notNullValue());
    }
  }

  @Test
  @DisplayName("登録した取引が一覧GETに現れる（POST→GET往復スモーク）")
  void registeredTransactionAppearsInList() {
    long categoryId = createCategory("EXPENSE");

    long transactionId =
        given()
            .contentType(ContentType.JSON)
            .body(transactionBody("EXPENSE", "POSITIVE", categoryId, "VALID", "MAX"))
            .when()
            .post("/transactions")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .accept(ContentType.JSON)
        .when()
        .get("/transactions")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1))
        .body("find { it.id == %d }.amount".formatted(transactionId), equalTo(1000))
        .body("find { it.id == %d }.categoryId".formatted(transactionId), equalTo((int) categoryId));
  }

  /** 指定種別のカテゴリを 1 件作成し、払い出された id を返す。 */
  private long createCategory(String type) {
    return given()
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

  /** 同値クラスから取引リクエストの JSON 本文を組み立てる。 */
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
      case "MALFORMED" -> "not-a-date"; // LocalDate へ解析不能 → 400
      default -> throw new IllegalArgumentException("unknown date class: " + occurredOnClass);
    };
  }

  private String memoValue(String memoClass) {
    return switch (memoClass) {
      case "ZERO" -> "";
      case "MAX" -> "a".repeat(255); // 上限ちょうど → OK
      case "OVER" -> "a".repeat(256); // 上限超過 → @Size 違反 → 400
      default -> throw new IllegalArgumentException("unknown memo class: " + memoClass);
    };
  }
}
