package com.microservices.notificationservice.service;

import com.microservices.notificationservice.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

	Notification createNotification(UUID userId, String type, String message);

	List<Notification> getUserNotifications(UUID userId);

	Notification markAsRead(UUID notificationId);
}