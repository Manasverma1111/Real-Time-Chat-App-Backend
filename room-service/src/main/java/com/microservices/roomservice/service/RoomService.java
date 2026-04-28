package com.microservices.roomservice.service;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.dto.RoomMemberResponse;
import com.microservices.roomservice.entity.Room;

import java.util.List;
import java.util.UUID;

public interface RoomService {

	Room createRoom(UUID creatorId, CreateRoomRequest request);

	List<Room> getUserRooms(UUID userId);

	List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId);

	void addMember(UUID roomId, UUID requesterId, UUID memberId);

	void removeMember(UUID roomId, UUID requesterId, UUID memberId);

	void leaveRoom(UUID roomId, UUID userId);

	void deleteRoom(UUID roomId, UUID requesterId);
}

//package com.microservices.roomservice.service;
//
//import com.microservices.roomservice.dto.CreateRoomRequest;
//import com.microservices.roomservice.entity.RoomMember;
//import com.microservices.roomservice.entity.Room;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface RoomService {
//
//	Room createRoom(UUID creatorId, CreateRoomRequest request);
//
//	List<Room> getUserRooms(UUID userId);
//
//	List<RoomMember> getRoomMembers(UUID roomId, UUID userId);
//
//	void addMember(UUID roomId, UUID requesterId, UUID memberId);
//
//	void removeMember(UUID roomId, UUID requesterId, UUID memberId);
//
//	void leaveRoom(UUID roomId, UUID userId);
//
//	void deleteRoom(UUID roomId, UUID requesterId);
//}
