package com.microservices.roomservice.repository;

import com.microservices.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
}