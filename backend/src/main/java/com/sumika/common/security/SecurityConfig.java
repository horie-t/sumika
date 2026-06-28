package com.sumika.common.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * OAuth2 Resource Server 設定。{@code /api/**} は Keycloak 発行の JWT を要求し、
 * API ドキュメントとヘルスは公開する。認証/認可エラーは RFC 7807 ProblemDetail で返す。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(problemEntryPoint())
                    .accessDeniedHandler(problemAccessDeniedHandler()));
    return http.build();
  }

  private AuthenticationEntryPoint problemEntryPoint() {
    return (request, response, authException) ->
        writeProblem(response, HttpStatus.UNAUTHORIZED, "認証が必要です");
  }

  private AccessDeniedHandler problemAccessDeniedHandler() {
    return (request, response, accessDeniedException) ->
        writeProblem(response, HttpStatus.FORBIDDEN, "アクセスが許可されていません");
  }

  /** RFC 7807 形式の最小 JSON を直接書き出す（ObjectMapper 依存を避ける）。 */
  private void writeProblem(HttpServletResponse response, HttpStatus status, String detail)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    String body =
        "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}"
            .formatted(status.getReasonPhrase(), status.value(), detail);
    response.getWriter().write(body);
  }
}
