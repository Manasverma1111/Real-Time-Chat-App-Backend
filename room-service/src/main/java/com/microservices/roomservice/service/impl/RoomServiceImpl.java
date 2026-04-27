package com.microservices.roomservice.service.impl;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.entity.Room;
import com.microservices.roomservice.entity.RoomMember;
import com.microservices.roomservice.repository.RoomMemberRepository;
import com.microservices.roomservice.repository.RoomRepository;
import com.microservices.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final RoomMemberRepository roomMemberRepository;

	@Override
	public Room createRoom(UUID creatorId, CreateRoomRequest request) {

		Room room = Room.builder().name(request.getName()).type(request.getType()).createdBy(creatorId).build();

		Room savedRoom = roomRepository.save(room);

		// Add creator as ADMIN
		RoomMember creator = RoomMember.builder().roomId(savedRoom.getRoomId()).userId(creatorId).role("ADMIN").build();

		roomMemberRepository.save(creator);

		// Add other members
		if (request.getMemberIds() != null) {
			for (UUID memberId : request.getMemberIds()) {
				RoomMember member = RoomMember.builder().roomId(savedRoom.getRoomId()).userId(memberId).role("MEMBER")
						.build();

				roomMemberRepository.save(member);
			}
		}

		return savedRoom;
	}

	@Override
	public List<Room> getUserRooms(UUID userId) {
		List<RoomMember> memberships = roomMemberRepository.findByUserId(userId);

		return memberships.stream()
				.map(m -> roomRepository.findById(m.getRoomId()).orElse(null))
				.filter(Objects::nonNull)
				.toList();	}
}