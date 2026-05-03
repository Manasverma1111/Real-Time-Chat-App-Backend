package com.microservices.messageservice.service.impl;

import com.microservices.messageservice.client.NotificationProducer;
import com.microservices.messageservice.dto.ChatMessage;
import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.repository.MessageRepository;
import com.microservices.messageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

	private final MessageRepository messageRepository;

	@Autowired
	private NotificationProducer notificationProducer;

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

		// ✅ Save message first (core functionality)
		Message savedMessage = messageRepository.save(message);

    /*
     ✅ NON-BREAKING RabbitMQ trigger
     - happens AFTER save
     - wrapped in try/catch inside producer
     - does NOT affect chat flow
    */
		// ✅ FIX: convert entity → DTO
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setSenderId(savedMessage.getSenderId().toString()); // ✅ FIX
		chatMessage.setSenderName(savedMessage.getSenderName());
		chatMessage.setRoomId(savedMessage.getRoomId().toString());     // ✅ FIX
		chatMessage.setContent(savedMessage.getContent());

		// 🔥 DEBUG LOGS (ADD HERE)
		System.out.println("🔥 BEFORE SEND");
		notificationProducer.send(chatMessage);
		System.out.println("🔥 AFTER SEND");


		return savedMessage;
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
