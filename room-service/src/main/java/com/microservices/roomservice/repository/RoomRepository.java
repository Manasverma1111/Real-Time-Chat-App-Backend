package com.microservices.roomservice.repository;

import com.microservices.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    /*
     Used for Explore Groups page
    */
    List<Room> findByTypeAndVisibility(
            String type,
            String visibility
    );
}