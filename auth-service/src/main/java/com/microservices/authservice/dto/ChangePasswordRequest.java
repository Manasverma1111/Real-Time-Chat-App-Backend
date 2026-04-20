package com.microservices.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password")
    private String currentPassword;

    @NotBlank(message = "New password")
    private String newPassword;
}