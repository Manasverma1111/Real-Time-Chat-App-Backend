package com.microservices.messageservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.microservices.messageservice.config.RabbitConfig;
import com.microservices.messageservice.dto.ChatMessage;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

// rabbit Template is a Spring AMQP RabbitTemplate that provides methods for sending messages to RabbitMQ.
// It is injected into the NotificationProducer class using constructor injection,
// which is facilitated by the @RequiredArgsConstructor annotation from Lombok.
// This allows the send method to use rabbitTemplate to send ChatMessage objects to the configured RabbitMQ exchange and routing key.
    private final RabbitTemplate rabbitTemplate;

//    The send method takes a ChatMessage object as a parameter and attempts to send it to RabbitMQ
//    using the rabbitTemplate's convertAndSend method.
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