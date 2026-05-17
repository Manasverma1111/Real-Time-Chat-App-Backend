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

//    Room is an entity class that represents a chat room in the application,
//    with fields for the room's ID, name, type, visibility, description, avatar URL, creator's ID,
//    member count, online count, admin name, creation timestamp, and update timestamp.
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

//   TRANSIENT FIELDS FOR FRONTEND DISPLAY
// These fields are not persisted in the database but are used to provide additional information
// for frontend display purposes, such as the number of members in the room,
// the number of online members, and the name of the room's administrator.
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

//     The prePersist() method is annotated with @PrePersist,
//     which means it will be called before the entity is persisted to the database.
    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.visibility == null || this.visibility.isBlank()) {
            this.visibility = "PRIVATE";
        }
    }

//    The preUpdate() method is annotated with @PreUpdate,
//    which means it will be called before the entity is updated in the database.
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}