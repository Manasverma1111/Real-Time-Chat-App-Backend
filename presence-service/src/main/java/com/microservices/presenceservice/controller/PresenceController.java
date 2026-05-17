package com.microservices.presenceservice.controller;

import com.microservices.presenceservice.dto.PresenceResponse;
import com.microservices.presenceservice.service.PresenceService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/presence")
public class PresenceController {

//	PresenceController is a REST controller that handles HTTP requests related to user presence.
//	It defines endpoints for marking a user as online or offline and retrieving a user's presence status.
//	The controller uses the PresenceService to perform the necessary operations
//	and returns a PresenceResponse object as the response for each endpoint.
	private final PresenceService presenceService;

//	The constructor of the PresenceController class takes a PresenceService as a parameter and assigns it to the presenceService field.
	public PresenceController(PresenceService presenceService) {
		this.presenceService = presenceService;
	}

//	markOnline() method is mapped to the POST request at the "/online/{userId}" endpoint.
	@PostMapping("/online/{userId}")
	public PresenceResponse markOnline(@PathVariable UUID userId) {
		return presenceService.markOnline(userId);
	}

//	markOffline() method is mapped to the POST request at the "/offline/{userId}" endpoint.
	@PostMapping("/offline/{userId}")
	public PresenceResponse markOffline(@PathVariable UUID userId) {
		return presenceService.markOffline(userId);
	}

//	getPresence() method is mapped to the GET request at the "/{userId}" endpoint.
	@GetMapping("/{userId}")
	public PresenceResponse getPresence(@PathVariable UUID userId) {
		return presenceService.getPresence(userId);
	}
}