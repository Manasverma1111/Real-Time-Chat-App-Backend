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

//    NotificationConsumer is a component that listens for incoming chat messages
//    from RabbitMQ and creates notifications based on those messages.
    private final NotificationService notificationService;

//    The receive() method is annotated with @RabbitListener,
//    which indicates that it should be invoked whenever a message is received on the specified queue ("notification.queue").
    @RabbitListener(
            queues = RabbitConfig.QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(ChatMessage msg) {

//        When a message is received, the method prints out the details of the message,
//        including the target user ID, sender name, message content, and room ID.
        try {

            System.out.println("=================================");
            System.out.println("📩 RECEIVED FROM RABBIT");
            System.out.println("TARGET USER: " + msg.getSenderId());
            System.out.println("SENDER NAME: " + msg.getSenderName());
            System.out.println("MESSAGE:     " + msg.getContent());
            System.out.println("ROOM:        " + msg.getRoomId());
            System.out.println("=================================");


//          senderId field stores TARGET USER ID

            UUID targetUserId = UUID.fromString(msg.getSenderId());


//          roomId for per-room badge grouping

            UUID roomId = UUID.fromString(msg.getRoomId());

            String notificationMessage =
                    msg.getSenderName() + ": " + msg.getContent();

            notificationService.createNotification(
                    targetUserId,
                    roomId,
                    "CHAT",
                    notificationMessage
            );

            System.out.println("✅ NOTIFICATION SAVED");

//            The createNotification() method of the notificationService is called to create a new notification for the target user,
//            using the sender's name and message content to construct the notification message.
        } catch (Exception e) {
            System.err.println("❌ NOTIFICATION CONSUMER FAILED");
            e.printStackTrace();
        }
    }
}