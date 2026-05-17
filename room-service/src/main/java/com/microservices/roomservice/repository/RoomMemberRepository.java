package com.microservices.roomservice.repository;

import com.microservices.roomservice.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {

//    RoomMemberRepository is a repository interface that extends JpaRepository to provide CRUD operations for RoomMember entities,
    List<RoomMember> findByUserId(UUID userId);

//     findByRoomId is a method that retrieves a list of RoomMember entities based on the room ID,
//     allowing you to get all members of a specific chat room.
    List<RoomMember> findByRoomId(UUID roomId);

//     findByRoomIdAndUserId is a method that retrieves an Optional containing a RoomMember entity based on both the room ID and user ID,
    Optional<RoomMember> findByRoomIdAndUserId(UUID roomId, UUID userId);

//    existsByRoomIdAndUserId is a method that checks if a RoomMember entity exists based on both the room ID and user ID,
    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

//    deleteByRoomIdAndUserId is a method that deletes a RoomMember entity based on both the room ID and user ID,
    void deleteByRoomIdAndUserId(UUID roomId, UUID userId);

//    deleteByRoomId is a method that deletes all RoomMember entities associated with a specific room ID,
//    which can be used when a room is deleted to remove all its members from the database.
    void deleteByRoomId(UUID roomId);
}
