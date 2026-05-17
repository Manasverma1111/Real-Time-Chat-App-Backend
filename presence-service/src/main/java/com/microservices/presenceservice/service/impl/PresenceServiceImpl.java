package com.microservices.presenceservice.service.impl;

import com.microservices.presenceservice.dto.PresenceResponse;
import com.microservices.presenceservice.entity.PresenceStatus;
import com.microservices.presenceservice.entity.UserPresence;
import com.microservices.presenceservice.repository.UserPresenceRepository;
import com.microservices.presenceservice.service.PresenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PresenceServiceImpl implements PresenceService {

//	PresenceServiceImpl is a service implementation class that provides methods to manage user presence information,
//	including marking users as online or offline and retrieving their presence status.
//	It interacts with the UserPresenceRepository to perform database operations.
	private final UserPresenceRepository userPresenceRepository;

//	The constructor of the PresenceServiceImpl class takes a UserPresenceRepository as a parameter
//	and assigns it to the userPresenceRepository field.
	public PresenceServiceImpl(UserPresenceRepository userPresenceRepository) {
		this.userPresenceRepository = userPresenceRepository;
	}

//	markOnline() method retrieves the UserPresence for the given userId from the repository.
	@Override
	public PresenceResponse markOnline(UUID userId) {
		UserPresence presence = userPresenceRepository.findById(userId).orElseGet(() -> {
			UserPresence userPresence = new UserPresence();
			userPresence.setUserId(userId);
			return userPresence;
		});

//		If the UserPresence does not exist, a new instance is created with the provided userId.
		presence.setStatus(PresenceStatus.ONLINE);
		presence.setLastSeen(LocalDateTime.now());

		UserPresence saved = userPresenceRepository.save(presence);

		return new PresenceResponse(saved.getUserId(), saved.getStatus(), saved.getLastSeen());
	}

//	markOffline() method performs a similar operation to markOnline(),
//	but it sets the presence status to OFFLINE and updates the last seen time.
	@Override
	public PresenceResponse markOffline(UUID userId) {
		UserPresence presence = userPresenceRepository.findById(userId).orElseGet(() -> {
			UserPresence userPresence = new UserPresence();
			userPresence.setUserId(userId);
			return userPresence;
		});

//		If the UserPresence does not exist, a new instance is created with the provided userId.
		presence.setStatus(PresenceStatus.OFFLINE);
		presence.setLastSeen(LocalDateTime.now());

		UserPresence saved = userPresenceRepository.save(presence);

		return new PresenceResponse(saved.getUserId(), saved.getStatus(), saved.getLastSeen());
	}

//	getPresence() method retrieves the UserPresence for the given userId from the repository.
	@Override
	public PresenceResponse getPresence(UUID userId) {
		UserPresence presence = userPresenceRepository.findById(userId).orElseGet(() -> {
			UserPresence userPresence = new UserPresence();
			userPresence.setUserId(userId);
			userPresence.setStatus(PresenceStatus.OFFLINE);
			userPresence.setLastSeen(null);
			return userPresence;
		});

//		If the UserPresence does not exist, a new instance is created with the provided userId,
//		default status of OFFLINE, and null last seen time.
		return new PresenceResponse(presence.getUserId(), presence.getStatus(), presence.getLastSeen());
	}
}