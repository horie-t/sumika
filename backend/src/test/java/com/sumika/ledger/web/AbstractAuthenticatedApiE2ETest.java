package com.sumika.ledger.web;

import static io.restassured.RestAssured.given;

import com.sumika.TestcontainersConfiguration;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;

/**
 * 認証つきの full-stack API E2E 基底クラス（REST Assured）。
 *
 * <p>Testcontainers の Keycloak（コミット済み {@code realm-sumika.json} をインポート）で実トークンを発行し、
 * Resource Server の issuer-uri をそのコンテナに向ける。各テストは {@link Sql} でテーブルを空にして分離する。
 * Keycloak はシングルトンコンテナとして JVM 内で 1 度だけ起動し、サブクラス間で共有する。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class AbstractAuthenticatedApiE2ETest {

  static final KeycloakContainer KEYCLOAK =
      new KeycloakContainer("quay.io/keycloak/keycloak:26.0").withRealmImportFile("/realm-sumika.json");

  static {
    KEYCLOAK.start();
  }

  private static String cachedToken;

  @LocalServerPort private int port;

  @DynamicPropertySource
  static void authProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.security.oauth2.resourceserver.jwt.issuer-uri",
        () -> KEYCLOAK.getAuthServerUrl() + "/realms/sumika");
  }

  @BeforeEach
  void setUpRestAssured() {
    RestAssured.port = this.port;
    RestAssured.basePath = "/api";
  }

  /** demo ユーザーの password grant で取得したアクセストークン（JVM 内でキャッシュ）。 */
  protected static String accessToken() {
    if (cachedToken == null) {
      cachedToken =
          given()
              .baseUri(KEYCLOAK.getAuthServerUrl())
              .basePath("/realms/sumika/protocol/openid-connect/token")
              .contentType(ContentType.URLENC)
              .formParam("client_id", "sumika-frontend")
              .formParam("grant_type", "password")
              .formParam("username", "demo")
              .formParam("password", "demo")
              .when()
              .post()
              .then()
              .statusCode(200)
              .extract()
              .path("access_token");
    }
    return cachedToken;
  }

  /** 認証済みリクエストの起点（Bearer トークン付き）。 */
  protected RequestSpecification authed() {
    return given().auth().oauth2(accessToken());
  }
}
