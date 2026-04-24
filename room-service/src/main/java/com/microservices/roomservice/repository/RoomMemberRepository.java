package com.microservices.roomservice.repository;

import com.microservices.roomservice.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {

    List<RoomMember> findByUserId(UUID userId);

    List<RoomMember> findByRoomId(UUID roomId);
}