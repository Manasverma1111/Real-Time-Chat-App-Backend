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

//	Message is a JPA entity that represents a message in the messaging service.
	@Id
	@GeneratedValue
	private UUID id;

	private UUID senderId;

//	 The senderName field is added to the Message entity to store the name of the sender of the message.
	private String senderName;

// FIELD FOR AVATAR URL
	@Column(columnDefinition = "TEXT")
	private String avatarUrl;

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

	/*
	 NEW FIELD FOR REACTIONS
	 Map of emoji to set of user IDs who reacted with that emoji
	*/

	@ElementCollection
	@CollectionTable(name = "message_reactions", joinColumns = @JoinColumn(name = "message_id"))
	@MapKeyColumn(name = "emoji")
	@Column(name = "user_id")
	private Map<String, Set<UUID>> reactions = new HashMap<>();
}
