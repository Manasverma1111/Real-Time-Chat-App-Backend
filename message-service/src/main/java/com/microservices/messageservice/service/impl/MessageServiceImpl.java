package com.microservices.messageservice.service.impl;

import com.microservices.messageservice.client.NotificationProducer;
import com.microservices.messageservice.dto.ChatMessage;
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

	private final NotificationProducer notificationProducer;

	public MessageServiceImpl(
			MessageRepository messageRepository,
			NotificationProducer notificationProducer
	) {
		this.messageRepository = messageRepository;
		this.notificationProducer = notificationProducer;
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
	public List<Message> getMessagesByRoom(UUID roomId, UUID currentUserId) {

		List<Message> messages =
				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

		return messages.stream()
				.filter(message -> {
					if (message.getDeletedForUsers() == null) return true;

					return !message.getDeletedForUsers().contains(currentUserId);
				})
				.toList();
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

	@Override
	public void deleteMessageForMe(UUID messageId, UUID userId) {

		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

		message.getDeletedForUsers().add(userId);

		messageRepository.save(message);
	}

	@Override
	public void toggleReaction(UUID messageId, UUID userId, String emoji) {

		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

		if (message.getReactions() == null) {
			message.setReactions(new java.util.HashMap<>());
		}

    /*
     CASE 1:
     User already reacted with SAME emoji
     -> remove reaction completely
    */
		if (message.getReactions().containsKey(emoji)
				&& message.getReactions().get(emoji).contains(userId)) {

			message.getReactions().get(emoji).remove(userId);

			if (message.getReactions().get(emoji).isEmpty()) {
				message.getReactions().remove(emoji);
			}

		} else {

        /*
         CASE 2:
         Remove old reactions first
        */
			for (var entry : message.getReactions().entrySet()) {
				entry.getValue().remove(userId);
			}

        /*
         CLEAN EMPTY REACTIONS
        */
			message.getReactions().entrySet()
					.removeIf(e -> e.getValue().isEmpty());

        /*
         ADD NEW REACTION
        */
			java.util.Set<UUID> users =
					message.getReactions()
							.getOrDefault(
									emoji,
									new java.util.HashSet<>()
							);

			users.add(userId);

			message.getReactions().put(emoji, users);
		}

		messageRepository.save(message);
	}
}
