package com.microservices.notificationservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequest {
    private UUID userId;
    private String type;
    private String message;
}