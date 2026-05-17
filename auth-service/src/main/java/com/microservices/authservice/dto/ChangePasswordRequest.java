package com.microservices.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

//    This DTO represents the request payload for changing a user's password,
    @NotBlank(message = "Current password")
    private String currentPassword;

    @NotBlank(message = "New password")
    private String newPassword;
}