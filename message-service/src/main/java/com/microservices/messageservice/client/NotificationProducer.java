package com.microservices.messageservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.microservices.messageservice.config.RabbitConfig;
import com.microservices.messageservice.dto.ChatMessage;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(ChatMessage msg) {
        try {
            System.out.println("Sending to RabbitMQ: " + msg.getContent()); // DEBUG

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY,
                    msg
            );
        } catch (Exception e) {
            // NEVER break chat flow
            System.err.println("RabbitMQ send failed: " + e.getMessage());
        }
    }
}