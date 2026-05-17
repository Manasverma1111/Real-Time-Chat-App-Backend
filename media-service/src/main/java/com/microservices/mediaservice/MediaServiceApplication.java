package com.microservices.mediaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MediaServiceApplication {

//	The MediaServiceApplication class is the entry point of the Spring Boot application.
//	It is annotated with @SpringBootApplication, which indicates that it is a Spring Boot application,
//	and @EnableDiscoveryClient, which enables service discovery for this application in a microservices' architecture.
//	The main method uses SpringApplication.run() to launch the application.
	public static void main(String[] args) {
		SpringApplication.run(MediaServiceApplication.class, args);
	}

}
