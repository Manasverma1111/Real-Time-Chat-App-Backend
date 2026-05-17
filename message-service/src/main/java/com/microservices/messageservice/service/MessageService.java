package com.microservices.messageservice.service;

import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

//	MessageService is an interface that defines the contract for the message-related operations in the messaging service.

//	sendMessage method is responsible for creating a new message,
//	saving it to the database, and sending notifications to other members of the chat room.
//	It takes a userId and a CreateMessageRequest as parameters and returns the saved Message object.
	Message sendMessage(UUID userId, CreateMessageRequest request);

// getMessagesByRoom method retrieves a paginated list of messages for a specific chat room,
// given the room ID, the current user ID, and pagination parameters (page and size).
	List<Message> getMessagesByRoom(
			UUID roomId,
			UUID currentUserId,
			int page,
			int size
	);

//	markMessagesAsSeen method marks all messages in a specific chat room as seen by the current user.
	void markMessagesAsSeen(UUID roomId, UUID currentUserId);

//	deleteMessageForEveryone method deletes a message for all users in the chat room.
	void deleteMessageForMe(UUID messageId, UUID userId);

//	deleteMessageForEveryone method deletes a message for all users in the chat room.
	void toggleReaction(UUID messageId, UUID userId, String emoji);
}