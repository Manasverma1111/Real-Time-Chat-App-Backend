package com.microservices.presenceservice.repository;

import com.microservices.presenceservice.entity.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPresenceRepository extends JpaRepository<UserPresence, UUID> {
}