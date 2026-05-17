package com.microservices.notificationservice.repository;

import com.microservices.notificationservice.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

//	findByUserIdOrderByCreatedAtDesc() method retrieves a list of notifications
//	for a specific user based on the user ID provided as a parameter.
	List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

	/*
	 Mark all notifications of a room as read
	 for the current user.
	*/
	@Modifying
	@Transactional
	@Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.roomId = :roomId
        AND n.userId = :userId
        AND n.isRead = false
    """)

//   markRoomNotificationsAsRead() method updates the isRead field to true for all notifications
//   that belong to a specific chat room and are associated with a given user ID,
//   effectively marking them as read.
	void markRoomNotificationsAsRead(UUID roomId, UUID userId);
}