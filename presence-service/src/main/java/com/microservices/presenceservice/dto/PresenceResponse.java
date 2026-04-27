package com.microservices.presenceservice.dto;

import com.microservices.presenceservice.entity.PresenceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PresenceResponse {
    private UUID userId;
    private PresenceStatus status;
    private LocalDateTime lastSeen;
}