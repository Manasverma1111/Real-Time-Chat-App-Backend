package com.microservices.messageservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "messages")
public class Message {

	@Id
	@GeneratedValue
	private UUID id;

	private UUID senderId;

	// NEW FIELD
	private String senderName;

	private UUID roomId;
	private String content;
	private LocalDateTime createdAt;

	/*
	 NEW:
	 message seen status
	 false = single tick
	 true = double tick
	*/
	private Boolean seen = false;
}
