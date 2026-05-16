package com.microservices.messageservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMessageRequest {

	@NotNull
	private UUID roomId;

	@NotBlank
	private String content;

	/*
	 FINAL FIX:
	 Save username for proper message display after refresh
	*/
	private String senderName;
	private String avatarUrl;
}
