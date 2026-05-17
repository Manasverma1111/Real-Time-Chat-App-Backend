package com.microservices.eurekaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApplication {

//	The EurekaServiceApplication class is the entry point of the Spring Boot application
//	and is annotated with @EnableEurekaServer to enable Eureka Server functionality.
//	This allows the application to act as a service registry
//	where other microservices can register themselves and discover other services.
	public static void main(String[] args) {
		SpringApplication.run(EurekaServiceApplication.class, args);
	}

}
