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

    /*
     ROOM TYPE:
     GROUP / DM
    */
    @Column(nullable = false)
    private String type;

    /*
     GROUP VISIBILITY:
     PUBLIC / PRIVATE
    */
    @Column(nullable = false)
    @Builder.Default
    private String visibility = "PRIVATE";

    /*
     GROUP DESCRIPTION
    */
    @Column(length = 300)
    private String description;

    /*
     GROUP PROFILE IMAGE
    */
    private String avatarUrl;

    @Column(nullable = false)
    private UUID createdBy;

    @Transient
    private Integer memberCount;

    @Transient
    private Integer onlineCount;

    /*
     FRONTEND DISPLAY ONLY
    */
    @Transient
    private String adminName;

    private LocalDateTime createdAt;

    /*
     TRACK ROOM UPDATES
    */
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.visibility == null || this.visibility.isBlank()) {
            this.visibility = "PRIVATE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}