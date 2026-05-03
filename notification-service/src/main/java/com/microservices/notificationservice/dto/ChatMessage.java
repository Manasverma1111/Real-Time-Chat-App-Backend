package com.microservices.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ CRITICAL
public class ChatMessage {

    private String senderId;
    private String senderName;
    private String roomId;
    private String content;
}