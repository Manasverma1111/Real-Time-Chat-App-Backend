package com.microservices.presenceservice.service;


import com.microservices.presenceservice.dto.PresenceResponse;

import java.util.UUID;

public interface PresenceService {
    PresenceResponse markOnline(UUID userId);
    PresenceResponse markOffline(UUID userId);
    PresenceResponse getPresence(UUID userId);
}