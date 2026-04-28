package com.microservices.messageservice.service.impl;

import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.repository.MessageRepository;
import com.microservices.messageservice.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

	private final MessageRepository messageRepository;

	public MessageServiceImpl(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	@Override
	public Message sendMessage(UUID userId, CreateMessageRequest request) {

		Message message = new Message();

		message.setSenderId(userId);

		message.setSenderName(
				request.getSenderName() != null && !request.getSenderName().isBlank()
						? request.getSenderName()
						: "User"
		);

		message.setRoomId(request.getRoomId());
		message.setContent(request.getContent());
		message.setCreatedAt(LocalDateTime.now());

		/*
		 NEW
		*/
		message.setSeen(false);

		return messageRepository.save(message);
	}

	@Override
	public List<Message> getMessagesByRoom(UUID roomId) {
		return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
	}

	@Override
	public void markMessagesAsSeen(UUID roomId, UUID currentUserId) {

		List<Message> messages =
				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

		for (Message message : messages) {

			/*
			 only messages from OTHER users
			*/
			if (!message.getSenderId().equals(currentUserId)) {
				message.setSeen(true);
			}
		}

		messageRepository.saveAll(messages);
	}
}
