package com.microservices.messageservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitStartupChecker {

//    RabbitStartupChecker is a Spring component that checks the availability of RabbitMQ at application startup.
    private final RabbitTemplate rabbitTemplate;

//    The init() method is annotated with @PostConstruct, which means it will be executed after the bean is initialized.
    @PostConstruct
    public void init() {

//        The method attempts to execute a simple operation using the rabbitTemplate to check if RabbitMQ is available.
        try {
            System.out.println("🚀 Initializing RabbitMQ connection...");
            rabbitTemplate.execute(channel -> {
                System.out.println("✅ RabbitMQ connected at startup");
                return true;
            });
        } catch (Exception e) {
            System.err.println("❌ RabbitMQ NOT available at startup: " + e.getMessage());
        }
    }
}