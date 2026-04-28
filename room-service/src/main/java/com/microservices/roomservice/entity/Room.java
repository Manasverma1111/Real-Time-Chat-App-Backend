package com.microservices.roomservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue
    private UUID roomId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // GROUP or DM

    @Column(nullable = false)
    private UUID createdBy;

    @Transient
    private Integer memberCount;

    @Transient
    private Integer onlineCount;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}