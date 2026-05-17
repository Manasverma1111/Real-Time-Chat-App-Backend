package com.microservices.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

//	Notification is a JPA entity that represents a notification in the notification service.
	@Id
	@GeneratedValue
	private UUID id;

	private UUID userId;

	/*
     ROOM this notification belongs to.
     Used by frontend to show per-room unread badge.
    */
	private UUID roomId;

	private String type;

	private String message;

	@Column(name = "is_read")
	private boolean isRead;

	private LocalDateTime createdAt;
}