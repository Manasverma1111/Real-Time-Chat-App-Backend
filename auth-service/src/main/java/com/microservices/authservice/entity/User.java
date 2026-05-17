package com.microservices.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

//    @Id
//    @GeneratedValue
//    private UUID userId;

//    Using UUID as the primary key for the User entity,
//    with a generation strategy of UUID and a column definition that specifies it as a CHAR(36) type
//    to ensure proper storage and retrieval of UUID values in the database.

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    private String avatarUrl;

    @Column(length = 250)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime lastSeenAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

//    Add a role field to manage ADMIN/USER permissions and access levels
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GlobalRole role;

//    The prePersist method is a lifecycle callback that is executed before the entity is persisted to the database.
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = UserStatus.OFFLINE;
        }
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.role == null) {
            this.role = GlobalRole.USER;
        }
    }
}