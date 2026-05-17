package com.microservices.notificationservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class NotificationServiceApplication {

//	main() method serves as the entry point for the Spring Boot application.
//	It uses SpringApplication.run() to launch the application,
//	passing in the NotificationServiceApplication class and any command-line arguments.
	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
