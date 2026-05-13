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
				.visibility(
						request.getVisibility() != null &&
								!request.getVisibility().isBlank()
								? request.getVisibility()
								: "PRIVATE"
				)
				.description(request.getDescription())
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

				if (memberId.equals(creatorId)) {
					continue;
				}

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

		List<RoomMember> memberships =
				roomMemberRepository.findByUserId(userId);

		return memberships.stream()
				.map(membership -> {
					Room room = roomRepository
							.findById(membership.getRoomId())
							.orElse(null);

					if (room == null) {
						return null;
					}

					List<RoomMember> members =
							roomMemberRepository.findByRoomId(room.getRoomId());

					// MEMBER COUNT
					room.setMemberCount(members.size());

					// ONLINE COUNT FROM PRESENCE SERVICE
					int onlineCount = 0;

					for (RoomMember member : members) {
						try {
							String url =
									"http://localhost:8084/presence/" + member.getUserId();

							Map<String, Object> response =
									restTemplate.getForObject(url, Map.class);

							if (response != null
									&& "ONLINE".equals(
									String.valueOf(response.get("status")))) {
								onlineCount++;
							}

						} catch (Exception ignored) {
							// if presence service fails,
							// do not break room loading
						}
					}

					room.setOnlineCount(onlineCount);

					return room;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	@Override
	public List<Room> getPublicGroups(UUID currentUserId) {

		List<Room> publicGroups =
				roomRepository.findByTypeAndVisibility(
						"GROUP",
						"PUBLIC"
				);

		return publicGroups.stream()
				.filter(room ->
						!roomMemberRepository.existsByRoomIdAndUserId(
								room.getRoomId(),
								currentUserId
						)
				)
				.map(room -> {

					List<RoomMember> members =
							roomMemberRepository.findByRoomId(room.getRoomId());

					room.setMemberCount(members.size());

					int onlineCount = 0;

					for (RoomMember member : members) {

						try {

							String url =
									"http://localhost:8084/presence/" + member.getUserId();

							Map<String, Object> response =
									restTemplate.getForObject(url, Map.class);

							if (
									response != null &&
											"ONLINE".equals(
													String.valueOf(response.get("status"))
											)
							) {
								onlineCount++;
							}

						} catch (Exception ignored) {
						}
					}

					room.setOnlineCount(onlineCount);

					return room;
				})
				.toList();
	}

	@Override
	public void joinPublicGroup(UUID roomId, UUID userId) {

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() ->
						new RuntimeException("Room not found"));

    /*
     Only GROUP rooms are joinable
    */
		if (!"GROUP".equals(room.getType())) {
			throw new RuntimeException("Only groups are joinable");
		}

    /*
     Only PUBLIC groups are directly joinable
    */
		if (!"PUBLIC".equals(room.getVisibility())) {
			throw new RuntimeException("This group is private");
		}

    /*
     Prevent duplicate joins
    */
		if (roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
			return;
		}

		RoomMember member = RoomMember.builder()
				.roomId(roomId)
				.userId(userId)
				.role("MEMBER")
				.build();

		roomMemberRepository.save(member);
	}

	@Override
	public List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId) {
		validateMembership(roomId, userId);

		List<RoomMember> members =
				roomMemberRepository.findByRoomId(roomId);

		return members.stream()
				.map(member -> {
					String url =
							"http://localhost:8081/auth/user/" + member.getUserId();

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
			throw new RuntimeException(
					"Admin cannot leave room. Delete room instead."
			);
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
//import com.microservices.roomservice.dto.RoomMemberResponse;
//import com.microservices.roomservice.entity.Room;
//import com.microservices.roomservice.entity.RoomMember;
//import com.microservices.roomservice.repository.RoomMemberRepository;
//import com.microservices.roomservice.repository.RoomRepository;
//import com.microservices.roomservice.service.RoomService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class RoomServiceImpl implements RoomService {
//
//	private final RoomRepository roomRepository;
//	private final RoomMemberRepository roomMemberRepository;
//	private final RestTemplate restTemplate;
//
//	@Override
//	public Room createRoom(UUID creatorId, CreateRoomRequest request) {
//
//		Room room = Room.builder()
//				.name(request.getName())
//				.type(request.getType())
//				.createdBy(creatorId)
//				.build();
//
//		Room savedRoom = roomRepository.save(room);
//
//		RoomMember creator = RoomMember.builder()
//				.roomId(savedRoom.getRoomId())
//				.userId(creatorId)
//				.role("ADMIN")
//				.build();
//
//		roomMemberRepository.save(creator);
//
//		if (request.getMemberIds() != null) {
//			for (UUID memberId : request.getMemberIds()) {
//
//				if (memberId.equals(creatorId)) continue;
//
//				RoomMember member = RoomMember.builder()
//						.roomId(savedRoom.getRoomId())
//						.userId(memberId)
//						.role("MEMBER")
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
//				.map(membership -> {
//					Room room = roomRepository
//							.findById(membership.getRoomId())
//							.orElse(null);
//
//					if (room == null) {
//						return null;
//					}
//
//					/*
//					 NEW:
//					 Set member count + online count
//					 without changing DB structure
//					*/
//					int memberCount = roomMemberRepository
//							.findByRoomId(room.getRoomId())
//							.size();
//
//					room.setMemberCount(memberCount);
//
//					/*
//					 Presence service not connected yet,
//					 so keep online count as 0 for now
//					*/
//					room.setOnlineCount(0);
//
//					return room;
//				})
//				.filter(Objects::nonNull)
//				.toList();
//	}
//
//	@Override
//	public List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId) {
//		validateMembership(roomId, userId);
//
//		List<RoomMember> members = roomMemberRepository.findByRoomId(roomId);
//
//		return members.stream()
//				.map(member -> {
//					String url = "http://localhost:8081/auth/user/" + member.getUserId();
//
//					Map<String, Object> user =
//							restTemplate.getForObject(url, Map.class);
//
//					String username = user != null
//							? String.valueOf(user.get("username"))
//							: "Unknown User";
//
//					return RoomMemberResponse.builder()
//							.userId(member.getUserId())
//							.username(username)
//							.role(member.getRole())
//							.build();
//				})
//				.toList();
//	}
//
//	@Override
//	public void addMember(UUID roomId, UUID requesterId, UUID memberId) {
//		validateAdmin(roomId, requesterId);
//
//		if (roomMemberRepository.existsByRoomIdAndUserId(roomId, memberId)) {
//			return;
//		}
//
//		RoomMember member = RoomMember.builder()
//				.roomId(roomId)
//				.userId(memberId)
//				.role("MEMBER")
//				.build();
//
//		roomMemberRepository.save(member);
//	}
//
//	@Override
//	@Transactional
//	public void removeMember(UUID roomId, UUID requesterId, UUID memberId) {
//		validateAdmin(roomId, requesterId);
//
//		Room room = roomRepository.findById(roomId)
//				.orElseThrow(() -> new RuntimeException("Room not found"));
//
//		if (room.getCreatedBy().equals(memberId)) {
//			throw new RuntimeException("Cannot remove room creator");
//		}
//
//		roomMemberRepository.deleteByRoomIdAndUserId(roomId, memberId);
//	}
//
//	@Override
//	@Transactional
//	public void leaveRoom(UUID roomId, UUID userId) {
//		Room room = roomRepository.findById(roomId)
//				.orElseThrow(() -> new RuntimeException("Room not found"));
//
//		if (room.getCreatedBy().equals(userId)) {
//			throw new RuntimeException("Admin cannot leave room. Delete room instead.");
//		}
//
//		roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
//	}
//
//	@Override
//	@Transactional
//	public void deleteRoom(UUID roomId, UUID requesterId) {
//		Room room = roomRepository.findById(roomId)
//				.orElseThrow(() -> new RuntimeException("Room not found"));
//
//		if (!room.getCreatedBy().equals(requesterId)) {
//			throw new RuntimeException("Only admin can delete room");
//		}
//
//		roomMemberRepository.deleteByRoomId(roomId);
//		roomRepository.deleteById(roomId);
//	}
//
//	private void validateMembership(UUID roomId, UUID userId) {
//		if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
//			throw new RuntimeException("Access denied");
//		}
//	}
//
//	private void validateAdmin(UUID roomId, UUID userId) {
//		RoomMember member = roomMemberRepository
//				.findByRoomIdAndUserId(roomId, userId)
//				.orElseThrow(() -> new RuntimeException("Access denied"));
//
//		if (!"ADMIN".equals(member.getRole())) {
//			throw new RuntimeException("Only admin allowed");
//		}
//	}
//}
