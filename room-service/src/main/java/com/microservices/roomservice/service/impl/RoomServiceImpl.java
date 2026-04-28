package com.microservices.roomservice.service.impl;

import com.microservices.roomservice.dto.CreateRoomRequest;
import com.microservices.roomservice.dto.RoomMemberResponse;
import com.microservices.roomservice.entity.Room;
import com.microservices.roomservice.entity.RoomMember;
import com.microservices.roomservice.repository.RoomMemberRepository;
import com.microservices.roomservice.repository.RoomRepository;
import com.microservices.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final RoomMemberRepository roomMemberRepository;
	private final RestTemplate restTemplate;

	@Override
	public Room createRoom(UUID creatorId, CreateRoomRequest request) {

		Room room = Room.builder()
				.name(request.getName())
				.type(request.getType())
				.createdBy(creatorId)
				.build();

		Room savedRoom = roomRepository.save(room);

		RoomMember creator = RoomMember.builder()
				.roomId(savedRoom.getRoomId())
				.userId(creatorId)
				.role("ADMIN")
				.build();

		roomMemberRepository.save(creator);

		if (request.getMemberIds() != null) {
			for (UUID memberId : request.getMemberIds()) {

				if (memberId.equals(creatorId)) continue;

				RoomMember member = RoomMember.builder()
						.roomId(savedRoom.getRoomId())
						.userId(memberId)
						.role("MEMBER")
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
				.toList();
	}

//	@Override
//	public List<RoomMember> getRoomMembers(UUID roomId, UUID userId) {
//		validateMembership(roomId, userId);
//		return roomMemberRepository.findByRoomId(roomId);
//	}

	@Override
	public List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId) {
		validateMembership(roomId, userId);

		List<RoomMember> members = roomMemberRepository.findByRoomId(roomId);

		return members.stream()
				.map(member -> {
					String url = "http://localhost:8081/auth/user/" + member.getUserId();

					Map<String, Object> user =
							restTemplate.getForObject(url, Map.class);

					String username = user != null
							? String.valueOf(user.get("username"))
							: "Unknown User";

					return RoomMemberResponse.builder()
							.userId(member.getUserId())
							.username(username)
							.role(member.getRole())
							.build();
				})
				.toList();
	}

	@Override
	public void addMember(UUID roomId, UUID requesterId, UUID memberId) {
		validateAdmin(roomId, requesterId);

		if (roomMemberRepository.existsByRoomIdAndUserId(roomId, memberId)) {
			return;
		}

		RoomMember member = RoomMember.builder()
				.roomId(roomId)
				.userId(memberId)
				.role("MEMBER")
				.build();

		roomMemberRepository.save(member);
	}

	@Override
	@Transactional
	public void removeMember(UUID roomId, UUID requesterId, UUID memberId) {
		validateAdmin(roomId, requesterId);

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

		if (room.getCreatedBy().equals(memberId)) {
			throw new RuntimeException("Cannot remove room creator");
		}

		roomMemberRepository.deleteByRoomIdAndUserId(roomId, memberId);
	}

	@Override
	@Transactional
	public void leaveRoom(UUID roomId, UUID userId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

		if (room.getCreatedBy().equals(userId)) {
			throw new RuntimeException("Admin cannot leave room. Delete room instead.");
		}

		roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
	}

	@Override
	@Transactional
	public void deleteRoom(UUID roomId, UUID requesterId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

		if (!room.getCreatedBy().equals(requesterId)) {
			throw new RuntimeException("Only admin can delete room");
		}

		roomMemberRepository.deleteByRoomId(roomId);
		roomRepository.deleteById(roomId);
	}

	private void validateMembership(UUID roomId, UUID userId) {
		if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new RuntimeException("Access denied");
		}
	}

	private void validateAdmin(UUID roomId, UUID userId) {
		RoomMember member = roomMemberRepository
				.findByRoomIdAndUserId(roomId, userId)
				.orElseThrow(() -> new RuntimeException("Access denied"));

		if (!"ADMIN".equals(member.getRole())) {
			throw new RuntimeException("Only admin allowed");
		}
	}
}


//package com.microservices.roomservice.service.impl;
//
//import com.microservices.roomservice.dto.CreateRoomRequest;
//import com.microservices.roomservice.entity.Room;
//import com.microservices.roomservice.entity.RoomMember;
//import com.microservices.roomservice.repository.RoomMemberRepository;
//import com.microservices.roomservice.repository.RoomRepository;
//import com.microservices.roomservice.service.RoomService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class RoomServiceImpl implements RoomService {
//
//	private final RoomRepository roomRepository;
//	private final RoomMemberRepository roomMemberRepository;
//
//	@Override
//	public Room createRoom(UUID creatorId, CreateRoomRequest request) {
//
//		Room room = Room.builder().name(request.getName()).type(request.getType()).createdBy(creatorId).build();
//
//		Room savedRoom = roomRepository.save(room);
//
//		// Add creator as ADMIN
//		RoomMember creator = RoomMember.builder().roomId(savedRoom.getRoomId()).userId(creatorId).role("ADMIN").build();
//
//		roomMemberRepository.save(creator);
//
//		// Add other members
//		if (request.getMemberIds() != null) {
//			for (UUID memberId : request.getMemberIds()) {
//				RoomMember member = RoomMember.builder().roomId(savedRoom.getRoomId()).userId(memberId).role("MEMBER")
//						.build();
//
//				roomMemberRepository.save(member);
//			}
//		}
//
//		return savedRoom;
//	}
//
//	@Override
//	public List<Room> getUserRooms(UUID userId) {
//		List<RoomMember> memberships = roomMemberRepository.findByUserId(userId);
//
//		return memberships.stream()
//				.map(m -> roomRepository.findById(m.getRoomId()).orElse(null))
//				.filter(Objects::nonNull)
//				.toList();	}
//}