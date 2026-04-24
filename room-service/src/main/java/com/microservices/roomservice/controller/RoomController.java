package com.microservices.roomservice.controller;

import com.microservices.roomservice.dto.CreateRoomRequest;
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

	@PostMapping
	public Room createRoom(@RequestHeader("Authorization") String authHeader,
						   @Valid @RequestBody CreateRoomRequest request) {

		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		return roomService.createRoom(UUID.fromString(userId), request);
	}

	@GetMapping
	public List<Room> getUserRooms(@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		return roomService.getUserRooms(UUID.fromString(userId));
	}
}