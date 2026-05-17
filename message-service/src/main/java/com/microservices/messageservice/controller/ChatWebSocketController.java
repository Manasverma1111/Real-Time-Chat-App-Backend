package com.microservices.messageservice.controller;

import com.microservices.messageservice.dto.ChatMessage;
import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.dto.TypingEvent;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatWebSocketController {

//    ChatWebSocketController is a Spring controller that handles WebSocket messages for a chat application.
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

//    The constructor of the ChatWebSocketController class takes two parameters: a SimpMessagingTemplate and a MessageService.
    public ChatWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            MessageService messageService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

//    The sendMessage method is annotated with @MessageMapping("/chat.send"),
//    which means it will handle messages sent to the "/chat.send" destination.
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage chatMessage) {

        System.out.println("Message received: " + chatMessage.getContent());

        CreateMessageRequest request = new CreateMessageRequest();

        request.setContent(chatMessage.getContent());
        request.setRoomId(UUID.fromString(chatMessage.getRoomId()));

		/*
		 FINAL FIX:
		 Persist senderName in DB
		 so after refresh username remains visible
		*/
        request.setSenderName(chatMessage.getSenderName());
        request.setAvatarUrl(chatMessage.getAvatarUrl());

//         Save the message to the database using the MessageService and get the saved Message object.
//         This ensures that the message is persisted and can be retrieved later, even after a page refresh.
//         The savedMessage object contains all the details of the message, including any generated IDs or timestamps.
//         This is crucial for maintaining message history and ensuring that messages are not lost on page refresh.

        Message savedMessage =
                messageService.sendMessage(
                        UUID.fromString(chatMessage.getSenderId()),
                        request
                );

//        After saving the message, the method uses the SimpMessagingTemplate
//        to send the saved message to all subscribers of the "/topic/room/{roomId}" destination.
        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                savedMessage
        );
    }

//    The typing method is annotated with @MessageMapping("/chat.typing"),
//    which means it will handle messages sent to the "/chat.typing" destination.
    @MessageMapping("/chat.typing")
    public void typing(TypingEvent typingEvent) {
        messagingTemplate.convertAndSend(
                "/topic/typing/" + typingEvent.getRoomId(),
                typingEvent
        );
    }
}
