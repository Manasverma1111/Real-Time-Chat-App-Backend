package com.microservices.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue
	private UUID id;

	private UUID userId;

	private String type;

	private String message;

	@Column(name = "is_read")
	private boolean isRead;

	private LocalDateTime createdAt;
}