package com.microservices.presenceservice.controller;

import com.microservices.presenceservice.dto.PresenceResponse;
import com.microservices.presenceservice.service.PresenceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/presence")
public class PresenceController {

//	PresenceController is a REST controller that handles HTTP requests related to user presence.
//	It defines endpoints for marking a user as online or offline and retrieving a user's presence status.
//	The controller uses the PresenceService to perform the necessary operations
//	and returns a PresenceResponse object as the response for each endpoint.
	private final PresenceService presenceService;
	private final RestTemplate restTemplate;

//	The constructor of the PresenceController class takes a PresenceService as a parameter and assigns it to the presenceService field.
	public PresenceController(PresenceService presenceService, RestTemplate restTemplate) {
		this.presenceService = presenceService;
		this.restTemplate = restTemplate;
	}

//	markOnline() method is mapped to the POST request at the "/online/{userId}" endpoint.
	@PostMapping("/online/{userId}")
	public PresenceResponse markOnline(@PathVariable UUID userId) {
		PresenceResponse response = presenceService.markOnline(userId);

        /*
         NOTIFY MESSAGE-SERVICE TO BROADCAST
         PRESENCE UPDATE VIA WEBSOCKET
        */
		broadcastPresence(userId.toString(), "ONLINE");

		return response;
	}

//	markOffline() method is mapped to the POST request at the "/offline/{userId}" endpoint.
	@PostMapping("/offline/{userId}")
	public PresenceResponse markOffline(@PathVariable UUID userId) {
		PresenceResponse response = presenceService.markOffline(userId);

		broadcastPresence(userId.toString(), "OFFLINE");

		return response;
	}

//	getPresence() method is mapped to the GET request at the "/{userId}" endpoint.
	@GetMapping("/{userId}")
	public PresenceResponse getPresence(@PathVariable UUID userId) {
		return presenceService.getPresence(userId);
	}

	/*
     INTERNAL: fires and forgets to message-service.
     message-service owns the WebSocket broker
     and broadcasts to /topic/presence
    */
	private void broadcastPresence(String userId, String status) {
		try {
			restTemplate.postForObject(
					"http://localhost:8083/internal/presence",
					Map.of("userId", userId, "status", status),
					Void.class
			);
		} catch (Exception e) {
			System.err.println(
					"⚠️ Could not notify message-service of presence change: "
							+ e.getMessage()
			);
		}
	}
}