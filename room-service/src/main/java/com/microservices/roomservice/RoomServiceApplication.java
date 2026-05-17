package com.microservices.roomservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RoomServiceApplication {

//    RoomServiceApplication is the main entry point for the Spring Boot application,
//    and it is annotated with @SpringBootApplication to enable autoconfiguration and component scanning,
//    and @EnableDiscoveryClient to enable service discovery for this microservice in a distributed system.
    public static void main(String[] args) {
        SpringApplication.run(RoomServiceApplication.class, args);
    }

}
