package com.microservices.apigateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

//    ApiGatewayApplication is the main entry point for the Spring Boot application,
//    and it is annotated with @SpringBootApplication to enable autoconfiguration and component scanning,
//    and @EnableDiscoveryClient to enable service discovery for the API Gateway in a distributed system,
//    allowing it to register with a service registry (like Eureka) and discover other microservices in the architecture.
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
