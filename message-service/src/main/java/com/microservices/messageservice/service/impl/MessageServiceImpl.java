package com.microservices.messageservice.service.impl;

import com.microservices.messageservice.client.NotificationProducer;
import com.microservices.messageservice.dto.ChatMessage;
import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.repository.MessageRepository;
import com.microservices.messageservice.service.MessageService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

//	MessageServiceImpl is a service class that implements the MessageService interface
//	and provides the business logic for handling messages in the messaging service.
	private final MessageRepository messageRepository;
	private final NotificationProducer notificationProducer;
	private final RestTemplate restTemplate;
	private final SimpMessagingTemplate messagingTemplate;

//	The constructor of the MessageServiceImpl class takes four parameters: a MessageRepository,
//	a NotificationProducer, a RestTemplate, and a SimpMessagingTemplate.
	public MessageServiceImpl(
			MessageRepository messageRepository,
			NotificationProducer notificationProducer,
			RestTemplate restTemplate,
			SimpMessagingTemplate messagingTemplate
	) {
		this.messageRepository = messageRepository;
		this.notificationProducer = notificationProducer;
		this.restTemplate = restTemplate;
		this.messagingTemplate = messagingTemplate;
	}

//	The sendMessage method is responsible for creating a new message, saving it to the database,
//	and sending notifications to other members of the chat room.
//	It takes a userId and a CreateMessageRequest as parameters and returns the saved Message object.
	@Override
	public Message sendMessage(UUID userId, CreateMessageRequest request) {

		Message message = new Message();
		message.setSenderId(userId);
		message.setSenderName(
				request.getSenderName() != null &&
						!request.getSenderName().isBlank()
						? request.getSenderName()
						: "User"
		);
		message.setAvatarUrl(request.getAvatarUrl());
		message.setRoomId(request.getRoomId());
		message.setContent(request.getContent());
		message.setCreatedAt(LocalDateTime.now());
		message.setSeen(false);

		Message savedMessage = messageRepository.save(message);

//		After saving the message, the method attempts to send notifications to other members of the chat room.
		try {

			String url =
					"http://localhost:8082/rooms/"
							+ savedMessage.getRoomId()
							+ "/members";

			ResponseEntity<List> response =
					restTemplate.exchange(
							url,
							HttpMethod.GET,
							null,
							List.class
					);

			List<?> members = response.getBody();

//			The method iterates through the list of members and sends a notification to each member except the sender of the message.
			if (members != null) {

				for (Object obj : members) {

					Map<?, ?> member = (Map<?, ?>) obj;

					String memberUserId =
							String.valueOf(member.get("userId"));

					if (memberUserId.equals(
							savedMessage.getSenderId().toString())) {
						continue;
					}

//					the method creates a ChatMessage object as the payload for the notification,
					ChatMessage notificationPayload = new ChatMessage();
					notificationPayload.setSenderId(memberUserId);
					notificationPayload.setSenderName(savedMessage.getSenderName());
					notificationPayload.setRoomId(
							savedMessage.getRoomId().toString());
					notificationPayload.setContent(savedMessage.getContent());

                    /*
                     STEP 1: SEND TO RABBITMQ
                     notification-service persists to DB
                     and returns a real UUID id
                    */
					notificationProducer.send(notificationPayload);

                    /*
                     STEP 2: PUSH REAL-TIME VIA WEBSOCKET
                     Use HashMap (not Map.of) so null-safety
                     is guaranteed and id field is included.

                     NOTE: id here is a temporary client-side UUID.
                     Frontend re-fetches from DB on modal open
                     to get the real persisted UUID for markAsRead.
                    */
					Map<String, Object> wsPayload = new HashMap<>();
					wsPayload.put("id", UUID.randomUUID().toString());
					wsPayload.put("userId", memberUserId);
					wsPayload.put("roomId", savedMessage.getRoomId().toString());
					wsPayload.put("message", savedMessage.getSenderName()
							+ ": " + savedMessage.getContent());
					wsPayload.put("senderName", savedMessage.getSenderName());
					wsPayload.put("type", "CHAT");
					wsPayload.put("read", false);
					wsPayload.put("createdAt", savedMessage.getCreatedAt().toString());

					messagingTemplate.convertAndSend(
							"/topic/notifications/" + memberUserId,
							wsPayload
					);

					System.out.println("✅ Notification sent to: " + memberUserId);
				}
			}

		} catch (Exception e) {
			System.err.println("❌ Notification send failed");
			e.printStackTrace();
		}

		return savedMessage;
	}

//	The getMessagesByRoom method retrieves messages for a specific chat room,
//	applying pagination and filtering out messages that have been deleted for the current user.
	@Override
	public List<Message> getMessagesByRoom(
			UUID roomId,
			UUID currentUserId,
			int page,
			int size
	) {

//		page and size parameters are used to create a Pageable object,
//		which is then passed to the messageRepository to retrieve a paginated list of messages
//		for the specified roomId, ordered by creation time in descending order.
		Pageable pageable = PageRequest.of(page, size);

		Page<Message> messagesPage =
				messageRepository.findByRoomIdOrderByCreatedAtDesc(
						roomId,
						pageable
				);

//		The method then filters out messages that have been marked as deleted for the current user
//		by checking the deletedForUsers set of each message.
		List<Message> filteredMessages =
				new java.util.ArrayList<>(
						messagesPage.getContent()
								.stream()
								.filter(message -> {
									if (message.getDeletedForUsers() == null) {
										return true;
									}

									return !message.getDeletedForUsers()
											.contains(currentUserId);
								})
								.toList()
				);

	/*
	 IMPORTANT:
	 reverse back to ASC order
	 so frontend UI remains unchanged
	*/
		java.util.Collections.reverse(filteredMessages);

		return filteredMessages;
	}

//	The markMessagesAsSeen method is responsible for marking messages in a specific chat room as seen by the current user.
	@Override
	public void markMessagesAsSeen(UUID roomId, UUID currentUserId) {

		Pageable pageable = PageRequest.of(0, 200);

		Page<Message> messagesPage =
				messageRepository.findByRoomIdOrderByCreatedAtDesc(
						roomId,
						pageable
				);

		List<Message> messages = messagesPage.getContent();

//		The method iterates through the retrieved messages and checks if each message was sent by another user and has not been marked as seen.
		for (Message message : messages) {

			if (
					!message.getSenderId().equals(currentUserId)
							&& !Boolean.TRUE.equals(message.getSeen())
			) {
				message.setSeen(true);
			}
		}

		messageRepository.saveAll(messages);

	/*
	 ALSO mark notifications as read
	 in notification-service DB.
	*/
		try {

			String notificationUrl =
					"http://localhost:8085/notifications/room/"
							+ roomId
							+ "/read?userId="
							+ currentUserId;

			restTemplate.put(notificationUrl, null);

			System.out.println("✅ Notifications marked as read");

		} catch (Exception e) {

			System.err.println("❌ Failed to mark notifications as read");
			e.printStackTrace();
		}
	}

//	The deleteMessageForMe method allows a user to mark a specific message as deleted for themselves,
//	without affecting other users' visibility of the message.
	@Override
	public void deleteMessageForMe(UUID messageId, UUID userId) {
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

		message.getDeletedForUsers().add(userId);
		messageRepository.save(message);
	}

//	The toggleReaction method allows a user to add or remove a reaction (emoji) to a specific message.
	@Override
	public void toggleReaction(UUID messageId, UUID userId, String emoji) {
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

//		The method first checks if the reactions map of the message is null and initializes it if necessary.
		if (message.getReactions() == null) {
			message.setReactions(new java.util.HashMap<>());
		}

		if (
				message.getReactions().containsKey(emoji) &&
						message.getReactions().get(emoji).contains(userId)
		) {
			message.getReactions().get(emoji).remove(userId);

			if (message.getReactions().get(emoji).isEmpty()) {
				message.getReactions().remove(emoji);
			}

//			If the user has already reacted with the same emoji,
//			the method removes the user's reaction from that emoji.
		} else {

			for (var entry : message.getReactions().entrySet()) {
				entry.getValue().remove(userId);
			}

			message.getReactions()
					.entrySet()
					.removeIf(e -> e.getValue().isEmpty());

			java.util.Set<UUID> users =
					message.getReactions()
							.getOrDefault(emoji, new java.util.HashSet<>());

			users.add(userId);
			message.getReactions().put(emoji, users);
		}

		messageRepository.save(message);
	}
}