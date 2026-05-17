package com.microservices.mediaservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Configure Swagger/OpenAPI documentation
    @Bean
    public OpenAPI customOpenAPI() {

        // Security scheme name used for JWT authentication
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                // API metadata
                .info(new Info()
                        .title("Media Service API")
                        .version("1.0")
                        .description("Media APIs for Real-Time Chat App")
                        .contact(new Contact()
                                .name("Manas Verma")))

                // Apply JWT security globally
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

                // Define JWT bearer authentication scheme
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}