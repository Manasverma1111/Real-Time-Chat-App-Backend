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

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public ChatWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            MessageService messageService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

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

        Message savedMessage =
                messageService.sendMessage(
                        UUID.fromString(chatMessage.getSenderId()),
                        request
                );

        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                savedMessage
        );
    }

    /*
     TYPING INDICATOR
    */
    @MessageMapping("/chat.typing")
    public void typing(TypingEvent typingEvent) {
        messagingTemplate.convertAndSend(
                "/topic/typing/" + typingEvent.getRoomId(),
                typingEvent
        );
    }
}
