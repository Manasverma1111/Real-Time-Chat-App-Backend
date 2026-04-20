package com.microservices.authservice.dto;

import com.microservices.authservice.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSearchResponse {
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private UserStatus status;
}