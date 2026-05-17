package com.microservices.messageservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TypingEvent {

//	TypingEvent is a simple data transfer object (DTO) that represents a typing event in the messaging service.
//	It contains fields for the room ID where the typing event occurred, the username of the person who is typing,
//	and a boolean flag indicating whether the user is currently typing or not.
	private UUID roomId;
	private String userName;
	private boolean typing;

}
