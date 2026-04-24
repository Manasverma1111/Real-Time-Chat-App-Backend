package com.microservices.roomservice.service;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.entity.Room;

import java.util.List;
import java.util.UUID;

public interface RoomService {

	Room createRoom(UUID creatorId, CreateRoomRequest request);

	List<Room> getUserRooms(UUID userId);
}