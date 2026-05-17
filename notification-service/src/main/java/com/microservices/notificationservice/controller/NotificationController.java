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

//	NotificationController is a REST controller that handles HTTP requests related to notifications in the notification service.
	private final NotificationService service;

//	The constructor of the NotificationController class takes a NotificationService
//	as a parameter and assigns it to the service field.
	public NotificationController(NotificationService service) {
		this.service = service;
	}

//	create() method is responsible for creating a new notification based on the data received in the request body.
	@PostMapping
	public Notification create(@RequestBody NotificationRequest request) {
		return service.createNotification(
				request.getUserId(),
				request.getRoomId(),   // now passed through
				request.getType(),
				request.getMessage()
		);
	}

//	getUserNotifications() method retrieves a list of notifications
//	for a specific user based on the user ID provided in the path variable.
	@GetMapping("/{userId}")
	public List<Notification> getUserNotifications(@PathVariable UUID userId) {
		return service.getUserNotifications(userId);
	}

//	markAsRead() method marks a specific notification as read based on the notification ID provided in the path variable.
	@PutMapping("/{id}/read")
	public Notification markAsRead(@PathVariable UUID id) {
		return service.markAsRead(id);
	}

//	markRoomNotificationsAsRead() method marks all notifications related to a specific chat room as read for a given user.
	@PutMapping("/room/{roomId}/read")
	public void markRoomNotificationsAsRead(
			@PathVariable UUID roomId,
			@RequestParam UUID userId
	) {
		service.markRoomNotificationsAsRead(roomId, userId);
	}
}