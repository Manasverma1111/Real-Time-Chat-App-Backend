package com.microservices.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {

//    This DTO encapsulates the response data for authentication operations,
    private String token;
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String role;
}