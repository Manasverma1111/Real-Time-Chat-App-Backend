package com.microservices.presenceservice.service;


import com.microservices.presenceservice.dto.PresenceResponse;

import java.util.UUID;

public interface PresenceService {

//    PresenceService is an interface that defines the contract for managing user presence information,
//    including methods to mark a user as online, mark a user as offline, and retrieve the presence information of a user.
    PresenceResponse markOnline(UUID userId);
    PresenceResponse markOffline(UUID userId);
    PresenceResponse getPresence(UUID userId);
}