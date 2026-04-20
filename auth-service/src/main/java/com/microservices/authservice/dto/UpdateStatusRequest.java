package com.microservices.authservice.dto;

import com.microservices.authservice.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}