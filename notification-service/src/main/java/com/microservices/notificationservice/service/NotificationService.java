package com.microservices.notificationservice.service;

import com.microservices.notificationservice.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

//	NotificationService is an interface that defines the contract for the notification service,
//	specifying the methods that must be implemented to handle notification-related operations.

	Notification createNotification(UUID userId, UUID roomId, String type, String message);

//	getUserNotifications() method retrieves a list of notifications for a specific user based on the user ID provided as a parameter.
	List<Notification> getUserNotifications(UUID userId);

//	markAsRead() method marks a specific notification as read based on the notification ID provided as a parameter.
	Notification markAsRead(UUID notificationId);

//	markRoomNotificationsAsRead() method marks all notifications related to a specific chat room as read for a given user.
	void markRoomNotificationsAsRead(UUID roomId, UUID userId);
}