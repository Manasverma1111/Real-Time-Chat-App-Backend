package com.microservices.notificationservice.consumer;

import com.microservices.notificationservice.config.RabbitConfig;
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
            queues = RabbitConfig.QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(ChatMessage msg) {

        try {

            System.out.println("=================================");
            System.out.println("📩 RECEIVED FROM RABBIT");
            System.out.println("TARGET USER: " + msg.getSenderId());
            System.out.println("SENDER NAME: " + msg.getSenderName());
            System.out.println("MESSAGE: " + msg.getContent());
            System.out.println("=================================");

            /*
             senderId field now stores
             TARGET USER ID
            */
            UUID targetUserId =
                    UUID.fromString(
                            msg.getSenderId()
                    );

            String notificationMessage =
                    msg.getSenderName()
                            + ": "
                            + msg.getContent();

            notificationService.createNotification(
                    targetUserId,
                    "CHAT",
                    notificationMessage
            );

            System.out.println("✅ NOTIFICATION SAVED");

        } catch (Exception e) {

            System.err.println(
                    "❌ NOTIFICATION CONSUMER FAILED"
            );

            e.printStackTrace();
        }
    }
}