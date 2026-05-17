package com.microservices.notificationservice.service.impl;

import com.microservices.notificationservice.entity.Notification;
import com.microservices.notificationservice.repository.NotificationRepository;
import com.microservices.notificationservice.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

//	NotificationServiceImpl is a service implementation class
//	that provides the business logic for managing notifications in the notification service.
	private final NotificationRepository repository;

//	The constructor of the NotificationServiceImpl class takes a NotificationRepository
//	as a parameter and assigns it to the repository field.
	public NotificationServiceImpl(NotificationRepository repository) {
		this.repository = repository;
	}

//	createNotification() method is responsible for creating a new notification
//	based on the provided parameters such as user ID, room ID, type, and message.
//	It constructs a Notification object, sets its properties, and saves it to the database using the repository.
	@Override
	public Notification createNotification(
			UUID userId,
			UUID roomId,
			String type,
			String message
	) {
		Notification notification = new Notification();
		notification.setUserId(userId);

        /*
         SAVE roomId so frontend can group
         unread counts per room
        */
		notification.setRoomId(roomId);

		notification.setType(type);
		notification.setMessage(message);
		notification.setRead(false);
		notification.setCreatedAt(LocalDateTime.now());

		return repository.save(notification);
	}

//	getUserNotifications() method retrieves a list of notifications for a specific user based on the user ID provided as a parameter.
//	It uses the repository to query the database and returns the notifications ordered by creation date in descending order.
	@Override
	public List<Notification> getUserNotifications(UUID userId) {
		return repository.findByUserIdOrderByCreatedAtDesc(userId);
	}

//	markAsRead() method marks a specific notification as read based on the notification ID provided as a parameter.
	@Override
	public Notification markAsRead(UUID notificationId) {
		Notification notification =
				repository.findById(notificationId).orElseThrow();
		notification.setRead(true);
		return repository.save(notification);
	}

//	markRoomNotificationsAsRead() method marks all notifications related to a specific chat room as read for a given user.
	@Override
	public void markRoomNotificationsAsRead(UUID roomId, UUID userId) {
		repository.markRoomNotificationsAsRead(roomId, userId);
	}
}