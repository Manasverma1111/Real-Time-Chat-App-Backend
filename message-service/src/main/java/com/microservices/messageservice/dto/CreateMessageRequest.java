package com.microservices.messageservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMessageRequest {

//	CreateMessageRequest is a data transfer object (DTO)
//	that represents the request payload for creating a new message in the messaging service.
	@NotNull
	private UUID roomId;

	@NotBlank
	private String content;

//	 Save username for proper message display after refresh
//	The senderName field is added to the CreateMessageRequest class to store the name of the sender of the message.

	private String senderName;
	private String avatarUrl;
}
