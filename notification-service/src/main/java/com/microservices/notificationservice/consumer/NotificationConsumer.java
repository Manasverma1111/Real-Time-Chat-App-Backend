package com.microservices.notificationservice.consumer;

import com.microservices.notificationservice.dto.ChatMessage;
import com.microservices.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
            queues = "notification.queue",
            containerFactory = "rabbitListenerContainerFactory" // ✅ CRITICAL FIX
    )
    public void receive(ChatMessage msg) {

        System.out.println("📩 RECEIVED FROM RABBIT: " + msg.getContent());

        UUID userId = UUID.fromString(msg.getSenderId());

        notificationService.createNotification(
                userId,
                "CHAT",
                msg.getSenderName() + ": " + msg.getContent()
        );

        System.out.println("🔥 CONSUMER ACTIVE");
        System.out.println("FULL OBJECT: " + msg);
    }
}