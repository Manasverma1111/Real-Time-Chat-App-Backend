package com.microservices.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("ConnectHub API Gateway")
                        .version("1.0")
                        .description("Aggregated APIs for all microservices"));
    }
}