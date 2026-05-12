package com.microservices.messageservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
	/*
 SUPPORT:
 - long S3 URLs
 - signed URLs
 - large messages
 - future media metadata
*/
	@Column(columnDefinition = "TEXT")
	private String content;
	private LocalDateTime createdAt;

	/*
	 NEW:
	 message seen status
	 false = single tick
	 true = double tick
	*/
	private Boolean seen = false;

	/*
	 NEW FIELD FOR DELETE FOR ME
	*/
	@ElementCollection
	private java.util.Set<UUID> deletedForUsers = new java.util.HashSet<>();

	@ElementCollection
	@CollectionTable(name = "message_reactions", joinColumns = @JoinColumn(name = "message_id"))
	@MapKeyColumn(name = "emoji")
	@Column(name = "user_id")
	private Map<String, Set<UUID>> reactions = new HashMap<>();
}
