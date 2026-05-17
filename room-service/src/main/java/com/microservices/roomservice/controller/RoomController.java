package com.microservices.roomservice.controller;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.dto.RoomMemberResponse;
import com.microservices.roomservice.entity.Room;
import com.microservices.roomservice.security.JwtService;
import com.microservices.roomservice.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

//	RoomController is a REST controller that handles HTTP requests related to room management,
//	including creating rooms, managing room members, and retrieving room information.
//	It uses the RoomService to perform business logic and the JwtService to extract user information from the authorization header.
	private final RoomService roomService;
	private final JwtService jwtService;

//	extractUserId() method is a helper method that extracts the user ID from the authorization header using the JwtService.
	private UUID extractUserId(String authHeader) {

		try {

			String token = authHeader.substring(7);

			String userId =
					jwtService.extractUserId(token);

			return UUID.fromString(userId);

		} catch (Exception e) {

		/*
		 INTERNAL SERVICE FALLBACK
		 used by notification flow
		*/
			return UUID.randomUUID();
		}
	}

//	POST /rooms: Creates a new room using the provided CreateRoomRequest and returns the created Room object.
	@PostMapping
	public Room createRoom(
			@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody CreateRoomRequest request
	) {
		return roomService.createRoom(extractUserId(authHeader), request);
	}

//	GET /rooms: Retrieves a list of rooms that the authenticated user is a member of.
	@GetMapping
	public List<Room> getUserRooms(
			@RequestHeader("Authorization") String authHeader
	) {
		return roomService.getUserRooms(extractUserId(authHeader));
	}

//	GET /rooms/{roomId}/members: Retrieves a list of members in the specified room.
	@GetMapping("/{roomId}/members")
	public List<RoomMemberResponse> getRoomMembers(
			@PathVariable UUID roomId,
			@RequestHeader(value = "Authorization", required = false)
			String authHeader
	) {

    /*
     INTERNAL SERVICE CALL (no auth header):
     pass null so service skips membership check.
    */
		if (authHeader == null || authHeader.isBlank()) {
			return roomService.getRoomMembers(roomId, null);
		}

    /*
     NORMAL USER FLOW: extract real userId.
    */
		return roomService.getRoomMembers(
				roomId,
				extractUserId(authHeader)
		);
	}

//	POST /rooms/{roomId}/members/{memberId}: Adds a member to the specified room.
	@PostMapping("/{roomId}/members/{memberId}")
	public void addMember(
			@PathVariable UUID roomId,
			@PathVariable UUID memberId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.addMember(roomId, extractUserId(authHeader), memberId);
	}

//	DELETE /rooms/{roomId}/members/{memberId}: Removes a member from the specified room.
	@DeleteMapping("/{roomId}/members/{memberId}")
	public void removeMember(
			@PathVariable UUID roomId,
			@PathVariable UUID memberId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.removeMember(roomId, extractUserId(authHeader), memberId);
	}

//	DELETE /rooms/{roomId}/leave: Allows the authenticated user to leave the specified room.
	@DeleteMapping("/{roomId}/leave")
	public void leaveRoom(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.leaveRoom(roomId, extractUserId(authHeader));
	}

//	DELETE /rooms/{roomId}: Deletes the specified room.
	@DeleteMapping("/{roomId}")
	public void deleteRoom(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.deleteRoom(roomId, extractUserId(authHeader));
	}

	/*
 GET ALL PUBLIC GROUPS
*/
//	GET /rooms/public: Retrieves a list of all public groups that the authenticated user can join.
	@GetMapping("/public")
	public List<Room> getPublicGroups(
			@RequestHeader("Authorization") String authHeader
	) {
		return roomService.getPublicGroups(
				extractUserId(authHeader)
		);
	}

	/*
     JOIN PUBLIC GROUP
    */
//	POST /rooms/{roomId}/join: Allows the authenticated user to join a public group specified by roomId.
	@PostMapping("/{roomId}/join")
	public void joinPublicGroup(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.joinPublicGroup(
				roomId,
				extractUserId(authHeader)
		);
	}

	/*
 GET ROOM DETAILS
*/
//	GET /rooms/{roomId}: Retrieves the details of a specific room, including its members and other relevant information.
	@GetMapping("/{roomId}")
	public Room getRoomDetails(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		return roomService.getRoomDetails(
				roomId,
				extractUserId(authHeader)
		);
	}

	/*
     UPDATE ROOM INFO
    */
//	PUT /rooms/{roomId}: Updates the information of a specific room, such as its name or description,
//	using the provided Room object in the request body.
	@PutMapping("/{roomId}")
	public Room updateRoom(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader,
			@RequestBody Room request
	) {
		return roomService.updateRoom(
				roomId,
				extractUserId(authHeader),
				request
		);
	}

	/*
     UPDATE GROUP AVATAR
    */
//	PUT /rooms/{roomId}/avatar: Updates the avatar of a specific room using the provided avatarUrl as a request parameter.
	@PutMapping("/{roomId}/avatar")
	public Room updateRoomAvatar(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader,
			@RequestParam String avatarUrl
	) {
		return roomService.updateRoomAvatar(
				roomId,
				extractUserId(authHeader),
				avatarUrl
		);
	}

}