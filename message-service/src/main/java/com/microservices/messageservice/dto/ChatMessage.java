package com.microservices.messageservice.dto;

import lombok.Data;

@Data
public class ChatMessage {

	private String content;
	private String senderId;
	private String roomId;
	private String senderName;
	private String avatarUrl;
}
