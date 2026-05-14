package com.microservices.notificationservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequest {

    private UUID userId;   // ✅ matches controller & service
    private String type;
    private String message;
    private UUID roomId;

}