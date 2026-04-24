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

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}