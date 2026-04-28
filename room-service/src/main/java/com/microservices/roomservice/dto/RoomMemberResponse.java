package com.microservices.roomservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoomMemberResponse {

    private UUID userId;
    private String username;
    private String role;
}