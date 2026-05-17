package com.microservices.authservice.dto;

import com.microservices.authservice.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

//    This DTO represents the request payload for updating a user's status,
//    containing a single field for the new status with a validation constraint to ensure it is not null.
    @NotNull(message = "Status is required")
    private UserStatus status;
}