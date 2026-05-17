package com.microservices.roomservice.config;

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

//    OpenApiConfig is a configuration class that defines a custom OpenAPI bean for the Room Service API documentation.
    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

// info() method is used to set the basic information about the API, such as the title, version,
// description, and contact details.
                .info(new Info()
                        .title("Room Service API")
                        .version("1.0")
                        .description("Room APIs for Real-Time Chat App")
                        .contact(new Contact()
                                .name("Manas Verma")))

//                addSecurityItem() method is used to define the security requirements for the API,
//                specifying that the API requires a bearer token for authentication.
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

//                components() method is used to define the security scheme for the API,
//                specifying that it uses HTTP Bearer authentication with JWT tokens.
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