package com.microservices.roomservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "room_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMember {

//    RoomMember is an entity class that represents a member of a chat room,
//    with fields for the member's ID, room ID, user ID, role in the room (e.g., ADMIN or MEMBER),
//    the timestamp when the member joined the room, and the timestamp of the last time the member read messages in the room.
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role; // ADMIN / MEMBER

    private LocalDateTime joinedAt;

    private LocalDateTime lastReadAt;

//     The prePersist() method is a lifecycle callback method that is called before the entity is persisted to the database.
    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}