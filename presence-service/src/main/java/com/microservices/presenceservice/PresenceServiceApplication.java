package com.microservices.presenceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PresenceServiceApplication {

//	PresenceServiceApplication is the main entry point for the Presence Service application,
//	annotated with @SpringBootApplication to enable Spring Boot autoconfiguration and component scanning,
//	and @EnableDiscoveryClient to enable service discovery with a service registry like Eureka.
	public static void main(String[] args) {
		SpringApplication.run(PresenceServiceApplication.class, args);
	}

}
