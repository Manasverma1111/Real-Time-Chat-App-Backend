package com.microservices.messageservice.repository;

import com.microservices.messageservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
	List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId);
}