package com.microservices.messageservice.dto;

import lombok.Data;

@Data
public class ChatMessage {

//	ChatMessage is a simple data transfer object (DTO) that represents a chat message in the messaging service.
//	It contains fields for the content of the message, the sender's ID, the room ID where the message was sent,
//	the sender's name, and the sender's avatar URL.
//	The @Data annotation from Lombok generates getters, setters, and other utility methods for this class.

	private String content;
	private String senderId;
	private String roomId;
	private String senderName;
	private String avatarUrl;
}
