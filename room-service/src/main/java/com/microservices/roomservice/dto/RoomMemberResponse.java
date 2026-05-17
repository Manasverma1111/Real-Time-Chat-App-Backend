package com.microservices.roomservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoomMemberResponse {

//    RoomMemberResponse is a DTO that represents the response for a room member,
//    including the user's ID, username, and role in the room.
    private UUID userId;
    private String username;
    private String role;
}