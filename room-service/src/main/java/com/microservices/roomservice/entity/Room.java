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

     IMPORTANT:
     Default PRIVATE to preserve old behavior
    */
    @Column(nullable = false)
    @Builder.Default
    private String visibility = "PRIVATE";

    /*
     OPTIONAL GROUP DESCRIPTION
    */
    @Column(length = 300)
    private String description;

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

        /*
         Safety fallback for old room creation flow
        */
        if (this.visibility == null || this.visibility.isBlank()) {
            this.visibility = "PRIVATE";
        }
    }
}