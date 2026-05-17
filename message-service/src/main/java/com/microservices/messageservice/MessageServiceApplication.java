package com.microservices.messageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class MessageServiceApplication {

//	The MessageServiceApplication class is the entry point of the Spring Boot application for the messaging service.

	public static void main(String[] args) {
		SpringApplication.run(MessageServiceApplication.class, args);
	}

	/*
	 REQUIRED FOR:
	 - notification member fetch
	 - inter-service room API calls
	*/
//	The restTemplate method is annotated with @Bean, which means it will be managed by the Spring container
//	and can be injected into other components of the application.
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}