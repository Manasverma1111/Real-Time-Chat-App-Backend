package com.microservices.messageservice.service;

import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
	Message sendMessage(UUID userId, CreateMessageRequest request);

	List<Message> getMessagesByRoom(
			UUID roomId,
			UUID currentUserId,
			int page,
			int size
	);
	void markMessagesAsSeen(UUID roomId, UUID currentUserId);

	void deleteMessageForMe(UUID messageId, UUID userId);

	void toggleReaction(UUID messageId, UUID userId, String emoji);
}