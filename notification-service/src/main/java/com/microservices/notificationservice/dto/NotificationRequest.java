package com.microservices.notificationservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequest {

//    NotificationRequest is a Data Transfer Object (DTO) that represents the structure of a notification creation request.
    private UUID userId;   // matches controller & service
    private String type;
    private String message;
    private UUID roomId;

}