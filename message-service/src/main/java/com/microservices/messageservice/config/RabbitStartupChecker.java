package com.microservices.messageservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitStartupChecker {

    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
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