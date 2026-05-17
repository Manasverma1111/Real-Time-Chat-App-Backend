package com.microservices.authservice.dto;

import com.microservices.authservice.entity.AuthProvider;
import com.microservices.authservice.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {

//	This DTO encapsulates the response data for user profile information,
	private UUID userId;
	private String username;
	private String email;
	private String fullName;
	private String avatarUrl;
	private String bio;
	private UserStatus status;
	private AuthProvider provider;
	private Boolean isActive;
	private LocalDateTime lastSeenAt;
	private LocalDateTime createdAt;
	private String role;
}