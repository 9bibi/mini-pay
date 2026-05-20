package com.minipay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniPayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniPay API")
                        .version("1.0")
                        .description("Simple digital wallet API for users, deposits, transfers, balances, and transactions"));
    }
}
