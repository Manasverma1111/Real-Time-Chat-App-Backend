package com.microservices.messageservice.dto;

import lombok.Data;

@Data
public class ChatMessage {

	private String content;
	private String senderId;
	private String roomId;

	// NEW FIELD
	private String senderName;
}

//package com.microservices.messageservice.dto;
//
//import lombok.Data;
//
//@Data
//public class ChatMessage {
//	private String content;
//	private String senderId;
//	private String roomId;
//}