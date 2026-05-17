package com.microservices.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

//    gatewayOpenAPI is a method that creates and configures an OpenAPI instance for the API Gateway,
//    setting the title, version, and description of the API documentation to provide a clear overview
//    of the available endpoints and their purpose for developers and users interacting with the API Gateway.
    @Bean
    public OpenAPI gatewayOpenAPI() {

//       The OpenAPI instance is configured with an Info object that contains metadata about the API,
//       including the title "ConnectHub API Gateway", version "1.0",
//       and a description that indicates it provides aggregated APIs for all microservices,
//       helping users understand the purpose and scope of the API Gateway
//       in the context of the overall microservices' architecture.
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectHub API Gateway")
                        .version("1.0")
                        .description("Aggregated APIs for all microservices"));
    }
}