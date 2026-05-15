package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.dto.NotificationRequest;
import com.microservices.notificationservice.entity.Notification;
import com.microservices.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService service;

	public NotificationController(NotificationService service) {
		this.service = service;
	}

	@PostMapping
	public Notification create(@RequestBody NotificationRequest request) {
		return service.createNotification(
				request.getUserId(),
				request.getRoomId(),   // now passed through
				request.getType(),
				request.getMessage()
		);
	}

	@GetMapping("/{userId}")
	public List<Notification> getUserNotifications(@PathVariable UUID userId) {
		return service.getUserNotifications(userId);
	}

	@PutMapping("/{id}/read")
	public Notification markAsRead(@PathVariable UUID id) {
		return service.markAsRead(id);
	}

	@PutMapping("/room/{roomId}/read")
	public void markRoomNotificationsAsRead(
			@PathVariable UUID roomId,
			@RequestParam UUID userId
	) {
		service.markRoomNotificationsAsRead(roomId, userId);
	}
}