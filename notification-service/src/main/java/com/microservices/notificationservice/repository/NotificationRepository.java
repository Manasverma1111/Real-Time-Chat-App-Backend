package com.microservices.notificationservice.repository;

import com.microservices.notificationservice.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

	/*
	 WHATSAPP-LIKE:
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
	void markRoomNotificationsAsRead(UUID roomId, UUID userId);
}