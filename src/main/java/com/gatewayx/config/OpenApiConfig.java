package com.gatewayx.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayXOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GatewayX API")
                        .description("API rate-limiting and developer platform — rate limiting, idempotency, usage metering, and webhooks")
                        .version("1.0"));
    }
}