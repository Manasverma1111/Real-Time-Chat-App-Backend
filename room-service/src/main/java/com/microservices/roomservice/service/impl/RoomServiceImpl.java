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

//	RoomServiceImpl is a service class that implements the RoomService interface,
//	providing the business logic for managing chat rooms and their members.
	private final RoomRepository roomRepository;
	private final RoomMemberRepository roomMemberRepository;
	private final RestTemplate restTemplate;

//	createRoom() method is responsible for creating a new chat room based on the provided CreateRoomRequest,
//	saving it to the database, and adding the creator as an admin member of the room.
//	It also adds any additional members specified in the request.
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
//				description is optional, so it can be null
				.description(request.getDescription())
				.createdBy(creatorId)
				.build();

		Room savedRoom = roomRepository.save(room);

		RoomMember creator = RoomMember.builder()
				.roomId(savedRoom.getRoomId())
				.userId(creatorId)
				.role("ADMIN")
				.build();

//		save creator as admin member
		roomMemberRepository.save(creator);

//		add additional members if provided
		if (request.getMemberIds() != null) {
			for (UUID memberId : request.getMemberIds()) {

				if (memberId.equals(creatorId)) {
					continue;
				}

//				prevent duplicate members
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

//	getUserRooms() method retrieves a list of rooms that the specified user is a member of,
//	including additional information such as member count and online member count by making calls to the presence service.
	@Override
	public List<Room> getUserRooms(UUID userId) {

		List<RoomMember> memberships =
				roomMemberRepository.findByUserId(userId);

		return memberships.stream()
				.map(membership -> {
					Room room = roomRepository
							.findById(membership.getRoomId())
							.orElse(null);

//					if room is deleted but membership still exists, skip it
					if (room == null) {
						return null;
					}

//					fetch members to calculate member count and online count
					List<RoomMember> members =
							roomMemberRepository.findByRoomId(room.getRoomId());

					// MEMBER COUNT
					room.setMemberCount(members.size());

					// ONLINE COUNT FROM PRESENCE SERVICE
					int onlineCount = 0;

					for (RoomMember member : members) {

//						presence service is best effort - if it fails,
//						we just show 0 online members instead of breaking the whole room loading
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

//	getPublicGroups() method retrieves a list of public group rooms that the specified user is not a member of,
//	including member count and online member count for each room by making calls to the presence service.
	@Override
	public List<Room> getPublicGroups(UUID currentUserId) {

		List<Room> publicGroups =
				roomRepository.findByTypeAndVisibility(
						"GROUP",
						"PUBLIC"
				);

//		filter out rooms where user is already a member, then fetch member count and online count for each room
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

//					fetch online count from presence service for each member
					for (RoomMember member : members) {

//						presence service is the best effort - if it fails,
//						we just show 0 online members instead of breaking the whole room loading
						try {

							String url =
									"http://localhost:8084/presence/" + member.getUserId();

							Map<String, Object> response =
									restTemplate.getForObject(url, Map.class);

//							check if user is online based on presence service response
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

//	joinPublicGroup() method allows a user to join a public group room by adding them as a member
//	if they are not already a member, and performs checks to ensure that only public groups
//	can be joined and that the user is not already a member of the room.
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

//	getRoomMembers() method retrieves a list of members for a specified room,
//	including their usernames by making calls to the user service,
//	and performs membership validation to ensure that only members of the room can access the member list.
@Override
public List<RoomMemberResponse> getRoomMembers(UUID roomId, UUID userId) {

	if (userId != null) {
		validateMembership(roomId, userId);
	}

	List<RoomMember> members = roomMemberRepository.findByRoomId(roomId);

	return members.stream()
			.map(member -> {
				String url =
						"http://localhost:8081/auth/user/" + member.getUserId();

				String username = "Deleted User";

				try {
					Map<String, Object> user =
							restTemplate.getForObject(url, Map.class);
					if (user != null) {
						username = String.valueOf(user.get("username"));
					}
				} catch (Exception e) {
                    /*
                     User was deleted from auth-service
                     but still exists as a room member.
                     Show fallback name instead of crashing.
                    */
					System.err.println(
							"⚠️ Could not fetch user " + member.getUserId()
									+ ": " + e.getMessage()
					);
				}

				return RoomMemberResponse.builder()
						.userId(member.getUserId())
						.username(username)
						.role(member.getRole())
						.build();
			})
			.toList();
}

//	addMember() method allows an admin member to add a new member to a specified room,
//	performing checks to ensure that only admins can add members and that the member being added is not already a member of the room.
	@Override
	public void addMember(UUID roomId, UUID requesterId, UUID memberId) {
		validateAdmin(roomId, requesterId);

//		prevent adding duplicate members
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

//	removeMember() method allows an admin member to remove a member from a specified room,
//	performing checks to ensure that only admins can remove members and that the room creator cannot be removed from the room.
	@Override
//	Transactional is used here to ensure that the member removal operation is atomic,
//	meaning that if any part of the operation fails (e.g., if the room is not found or if the requester is not an admin),
//	the entire operation will be rolled back and no changes will be made to the database.
	@Transactional
	public void removeMember(UUID roomId, UUID requesterId, UUID memberId) {
		validateAdmin(roomId, requesterId);

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

//		prevent removing room creator
		if (room.getCreatedBy().equals(memberId)) {
			throw new RuntimeException("Cannot remove room creator");
		}

		roomMemberRepository.deleteByRoomIdAndUserId(roomId, memberId);
	}

//	leaveRoom() method allows a member to leave a specified room,
//	performing checks to ensure that the room creator cannot leave the room and that the member is removed from the room's membership list.
	@Override
	@Transactional
	public void leaveRoom(UUID roomId, UUID userId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

//		prevent room creator from leaving - they must delete the room instead
		if (room.getCreatedBy().equals(userId)) {
			throw new RuntimeException(
					"Admin cannot leave room. Delete room instead."
			);
		}

		roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
	}

//	deleteRoom() method allows an admin member to delete a specified room,
//	performing checks to ensure that only the room creator can delete the room
//	and that all members of the room are removed from the membership list when the room is deleted.
	@Override
	@Transactional
	public void deleteRoom(UUID roomId, UUID requesterId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Room not found"));

//		only room creator (admin) can delete the room
		if (!room.getCreatedBy().equals(requesterId)) {
			throw new RuntimeException("Only admin can delete room");
		}

		roomMemberRepository.deleteByRoomId(roomId);
		roomRepository.deleteById(roomId);
	}

//	getRoomDetails() method retrieves detailed information about a specified room,
//	including member count, online member count, and the name of the room's administrator
//	by making calls to the presence service and user service,
	@Override
	public Room getRoomDetails(UUID roomId, UUID userId) {

		validateMembership(roomId, userId);

//		fetch room details
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() ->
						new RuntimeException("Room not found"));

		List<RoomMember> members =
				roomMemberRepository.findByRoomId(roomId);

		room.setMemberCount(members.size());

		int onlineCount = 0;

//		fetch online count from presence service for each member
		for (RoomMember member : members) {

			try {

				String url =
						"http://localhost:8084/presence/" + member.getUserId();

				Map<String, Object> response =
						restTemplate.getForObject(url, Map.class);

//				check if user is online based on presence service response
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

		/*
		 FETCH ADMIN NAME
		*/
//		since only one admin per room, we can just find the first member with role ADMIN
		try {

			String url =
					"http://localhost:8081/auth/user/" + room.getCreatedBy();

			Map<String, Object> user =
					restTemplate.getForObject(url, Map.class);

			if (user != null) {
				room.setAdminName(
						String.valueOf(user.get("username"))
				);
			}

		} catch (Exception ignored) {
			room.setAdminName("Unknown");
		}

		return room;
	}

//	updateRoom() method allows an admin member to update the details of a specified room,
//	performing checks to ensure that only admins can update the room and that only non-null
//	and non-blank fields are updated to prevent accidental overwriting of existing room details with null or blank values.
	@Override
	public Room updateRoom(
			UUID roomId,
			UUID userId,
			Room request
	) {

    /*
     ONLY ADMIN CAN EDIT GROUP
    */
//    validateMembership(roomId, userId);
		validateAdmin(roomId, userId);

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() ->
						new RuntimeException("Room not found"));

    /*
     SAFE NON-BREAKING UPDATES
    */
		if (request.getName() != null &&
				!request.getName().isBlank()) {

			room.setName(request.getName());
		}

    /*
     UPDATE DESCRIPTION ONLY IF PROVIDED
    */
		if (request.getDescription() != null) {

			room.setDescription(request.getDescription());
		}

    /*
     UPDATE VISIBILITY ONLY IF PROVIDED
    */
		if (request.getVisibility() != null &&
				!request.getVisibility().isBlank()) {

			room.setVisibility(request.getVisibility());
		}

		return roomRepository.save(room);
	}

//	updateRoomAvatar() method allows an admin member to update the avatar URL of a specified room,
//	performing checks to ensure that only admins can update the room avatar
//	and that the avatar URL is updated correctly in the database.
	@Override
	public Room updateRoomAvatar(
			UUID roomId,
			UUID userId,
			String avatarUrl
	) {

		validateAdmin(roomId, userId);

//		we can reuse the same validation logic as updateRoom since it's also an admin-only update
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() ->
						new RuntimeException("Room not found"));

		room.setAvatarUrl(avatarUrl);

		return roomRepository.save(room);
	}

//	validateMembership() method checks if a specified user is a member of a specified room,
//	throwing an exception if the user is not a member to prevent unauthorized access to room details and member lists.
	private void validateMembership(UUID roomId, UUID userId) {
		if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new RuntimeException("Access denied");
		}
	}

//	validateAdmin() method checks if a specified user is an admin member of a specified room,
// throwing an exception if the user is not an admin to prevent unauthorized access to admin-only operations
// such as adding/removing members, updating room details, and deleting the room.
	private void validateAdmin(UUID roomId, UUID userId) {
		RoomMember member = roomMemberRepository
				.findByRoomIdAndUserId(roomId, userId)
				.orElseThrow(() -> new RuntimeException("Access denied"));

		if (!"ADMIN".equals(member.getRole())) {
			throw new RuntimeException("Only admin allowed");
		}
	}
}
