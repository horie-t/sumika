package com.sumika.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** springdoc が公開する OpenAPI のメタ情報。 */
@Configuration
class OpenApiConfig {

  @Bean
  OpenAPI sumikaOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("sumika API")
                .description("家計簿管理 SaaS の API")
                .version("v1"));
  }
}
