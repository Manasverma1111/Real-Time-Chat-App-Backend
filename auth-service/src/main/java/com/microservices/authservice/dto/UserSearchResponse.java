package com.microservices.authservice.dto;

import com.microservices.authservice.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSearchResponse {

//    This DTO represents the response data for user search results,
//    containing basic user information such as ID, username, full name, avatar URL, status, bio, email, and role.
//    It is designed to provide enough information for displaying user profiles in search results or profile viewer modals.
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private UserStatus status;

    /*
    Added so profile viewer modal can display
    bio, email and role when clicking user avatars
   */
    private String bio;
    private String email;
    private String role;
}