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

	private final RoomService roomService;
	private final JwtService jwtService;

	private UUID extractUserId(String authHeader) {
		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);
		return UUID.fromString(userId);
	}

	@PostMapping
	public Room createRoom(
			@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody CreateRoomRequest request
	) {
		return roomService.createRoom(extractUserId(authHeader), request);
	}

	@GetMapping
	public List<Room> getUserRooms(
			@RequestHeader("Authorization") String authHeader
	) {
		return roomService.getUserRooms(extractUserId(authHeader));
	}

	@GetMapping("/{roomId}/members")
	public List<RoomMemberResponse> getRoomMembers(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		return roomService.getRoomMembers(roomId, extractUserId(authHeader));
	}

	@PostMapping("/{roomId}/members/{memberId}")
	public void addMember(
			@PathVariable UUID roomId,
			@PathVariable UUID memberId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.addMember(roomId, extractUserId(authHeader), memberId);
	}

	@DeleteMapping("/{roomId}/members/{memberId}")
	public void removeMember(
			@PathVariable UUID roomId,
			@PathVariable UUID memberId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.removeMember(roomId, extractUserId(authHeader), memberId);
	}

	@DeleteMapping("/{roomId}/leave")
	public void leaveRoom(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		roomService.leaveRoom(roomId, extractUserId(authHeader));
	}

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

}