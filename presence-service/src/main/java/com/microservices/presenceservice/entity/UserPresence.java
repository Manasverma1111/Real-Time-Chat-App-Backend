package com.microservices.presenceservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_presence")
public class UserPresence {

	@Id
	private UUID userId;

	@Enumerated(EnumType.STRING)
	private PresenceStatus status;

	private LocalDateTime lastSeen;
}