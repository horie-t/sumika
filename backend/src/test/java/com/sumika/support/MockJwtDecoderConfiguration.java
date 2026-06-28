package com.sumika.support;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * 実 Keycloak を起動しないテスト（コンテキスト起動・MockMvc + jwt() 利用）向けに、
 * OAuth2 Resource Server の {@link JwtDecoder} をモックで差し替える。これにより起動時の
 * issuer-uri への JWKS 取得（ネットワークアクセス）を回避する。jwt() postprocessor は
 * デコーダを介さず認証を注入するため、モックの戻り値は使われない。
 */
@TestConfiguration(proxyBeanMethods = false)
public class MockJwtDecoderConfiguration {

  @Bean
  JwtDecoder jwtDecoder() {
    return mock(JwtDecoder.class);
  }
}
