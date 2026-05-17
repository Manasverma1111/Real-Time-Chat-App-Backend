package com.microservices.roomservice.service;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.dto.RoomMemberResponse;
import com.microservices.roomservice.entity.Room;

import java.util.List;
import java.util.UUID;

public interface RoomService {

//   createRoom is a method that takes a creator's user ID and a CreateRoomRequest object as parameters,
//	and returns a Room object representing the newly created chat room.
//	This method is responsible for handling the logic of creating a new chat room based on the provided request data.
	Room createRoom(UUID creatorId, CreateRoomRequest request);

//	getUserRooms is a method that takes a user's ID as a parameter
//	and returns a list of Room objects representing the chat rooms that the user is a member of.
	List<Room> getUserRooms(UUID userId);

//	getRoomMembers is a method that takes a room ID and a user ID as parameters,
//	and returns a list of RoomMemberResponse objects representing the members of the specified chat room.
	List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId);

//	addMember is a method that takes a room ID, a requester ID, and a member ID as parameters,
//	and adds the specified member to the chat room if the requester has the necessary permissions to do so.
	void addMember(UUID roomId, UUID requesterId, UUID memberId);

//	removeMember is a method that takes a room ID, a requester ID, and a member ID as parameters,
//	and removes the specified member from the chat room if the requester has the necessary permissions to do so.
	void removeMember(UUID roomId, UUID requesterId, UUID memberId);

//	leaveRoom is a method that takes a room ID and a user ID as parameters,
//	and allows the specified user to leave the chat room, removing their membership from the room.
	void leaveRoom(UUID roomId, UUID userId);

//	deleteRoom is a method that takes a room ID and a requester ID as parameters,
//	and deletes the specified chat room if the requester has the necessary permissions to do so,
//	removing all associated memberships and data related to the room.
	void deleteRoom(UUID roomId, UUID requesterId);

//	getPublicGroups is a method that takes a user's ID as a parameter
//	and returns a list of Room objects representing the public chat rooms that the user can join.
	List<Room> getPublicGroups(UUID currentUserId);

//	joinPublicGroup is a method that takes a room ID and a user ID as parameters,
//	and allows the specified user to join the public chat room identified by the room ID,
//	adding them as a member of the room if they are not already a member.
	void joinPublicGroup(UUID roomId, UUID userId);

//	getRoomDetails is a method that takes a room ID and a user ID as parameters,
//	and returns a Room object representing the details of the specified chat room if the user is a member of the room,
//	otherwise it may throw an exception or return null depending on the implementation,
//	ensuring that only members can access the room details.
	Room getRoomDetails(UUID roomId, UUID userId);

//	updateRoom is a method that takes a room ID, a user ID,
//	and a Room object containing the updated room details as parameters,
//	and updates the specified chat room with the new details if the user has the necessary permissions to do so,
//	returning the updated Room object after the changes have been applied.
	Room updateRoom(
			UUID roomId,
			UUID userId,
			Room request
	);

//	updateRoomAvatar is a method that takes a room ID, a user ID, and a string representing the new avatar URL as parameters,
//	and updates the avatar of the specified chat room with the new URL if the user has the necessary permissions to do so,
//	returning the updated Room object after the avatar change has been applied.
	Room updateRoomAvatar(
			UUID roomId,
			UUID userId,
			String avatarUrl
	);
}
