package com.microservices.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data

//@JsonIgnoreProperties(ignoreUnknown = true) annotation is used to instruct the JSON deserializer
// to ignore any properties in the incoming JSON that do not match fields in the ChatMessage class.
@JsonIgnoreProperties(ignoreUnknown = true) // CRITICAL
public class ChatMessage {

//    ChatMessage is a Data Transfer Object (DTO) that represents the structure of a chat message
//    received from RabbitMQ.
    private String senderId;
    private String senderName;
    private String roomId;
    private String content;
}