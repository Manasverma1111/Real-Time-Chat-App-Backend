package com.microservices.presenceservice.controller;

import com.microservices.presenceservice.dto.PresenceResponse;
import com.microservices.presenceservice.service.PresenceService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/presence")
public class PresenceController {

	private final PresenceService presenceService;

	public PresenceController(PresenceService presenceService) {
		this.presenceService = presenceService;
	}

	@PostMapping("/online/{userId}")
	public PresenceResponse markOnline(@PathVariable UUID userId) {
		return presenceService.markOnline(userId);
	}

	@PostMapping("/offline/{userId}")
	public PresenceResponse markOffline(@PathVariable UUID userId) {
		return presenceService.markOffline(userId);
	}

	@GetMapping("/{userId}")
	public PresenceResponse getPresence(@PathVariable UUID userId) {
		return presenceService.getPresence(userId);
	}
}