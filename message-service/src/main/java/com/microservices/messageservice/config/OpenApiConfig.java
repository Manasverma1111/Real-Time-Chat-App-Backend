package com.microservices.messageservice.config;

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

//    openAPI() method defines the OpenAPI specification for the Message Service API,
//    including metadata and security configurations.
    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

//                info() method sets the API metadata, such as title, version, description,
//                and contact information for the API.
                .info(new Info()
                        .title("Message Service API")
                        .version("1.0")
                        .description("Message APIs for Real-Time Chat App")
                        .contact(new Contact()
                                .name("Manas Verma")))

//                addSecurityItem() method adds a security requirement to the API,
//                specifying that the API requires authentication using the defined security scheme.
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

//                components() method defines the security scheme for the API,
//                specifying that it uses HTTP Bearer authentication with JWT tokens.
                .components(
                        new Components()

//                                addSecuritySchemes() method adds a security scheme to the API components,
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