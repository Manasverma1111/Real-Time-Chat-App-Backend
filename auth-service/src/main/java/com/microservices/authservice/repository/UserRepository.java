package com.microservices.authservice.repository;

import com.microservices.authservice.entity.User;
import com.microservices.authservice.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

//    This interface extends JpaRepository, providing CRUD operations for the User entity.

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(UUID userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByStatus(UserStatus status);

    void deleteByUserId(UUID userId);

    List<User> findByUsernameContainingIgnoreCase(String username);
}