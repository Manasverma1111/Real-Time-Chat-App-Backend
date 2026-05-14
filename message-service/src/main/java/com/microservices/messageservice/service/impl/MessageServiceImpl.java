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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

	private final MessageRepository messageRepository;
	private final NotificationProducer notificationProducer;
	private final RestTemplate restTemplate;

	/*
     WEBSOCKET PUSH
     Used to broadcast real-time notifications
     to /topic/notifications/{userId}
    */
	private final SimpMessagingTemplate messagingTemplate;

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
		message.setRoomId(request.getRoomId());
		message.setContent(request.getContent());
		message.setCreatedAt(LocalDateTime.now());
		message.setSeen(false);

        /*
         SAVE MESSAGE FIRST
        */
		Message savedMessage = messageRepository.save(message);

        /*
         ==========================================
         WHATSAPP-LIKE NOTIFICATION FLOW
         - notify OTHER users only
         - sender should NOT get notification
         - NEVER break chat flow
         ==========================================
        */
		try {

            /*
             FETCH ROOM MEMBERS
             No Authorization header = internal call.
             room-service skips membership check.
            */
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

			if (members != null) {

				for (Object obj : members) {

					Map<?, ?> member = (Map<?, ?>) obj;

					String memberUserId =
							String.valueOf(member.get("userId"));

                    /*
                     SKIP SENDER
                    */
					if (memberUserId.equals(
							savedMessage.getSenderId().toString())) {
						continue;
					}

                    /*
                     CREATE NOTIFICATION PAYLOAD
                    */
					ChatMessage notificationPayload = new ChatMessage();

                    /*
                     senderId field stores TARGET USER ID
                    */
					notificationPayload.setSenderId(memberUserId);
					notificationPayload.setSenderName(savedMessage.getSenderName());
					notificationPayload.setRoomId(
							savedMessage.getRoomId().toString());
					notificationPayload.setContent(savedMessage.getContent());

                    /*
                     STEP 1: SEND TO RABBITMQ
                     (notification-service will persist to DB)
                    */
					notificationProducer.send(notificationPayload);

                    /*
                     STEP 2: PUSH REAL-TIME VIA WEBSOCKET
                     notification-service only saves to DB —
                     it has no WebSocket broker.
                     We push here from message-service
                     which owns the WebSocket broker.

                     Frontend subscribes to:
                     /topic/notifications/{userId}
                    */
					Map<String, Object> wsPayload = Map.of(
							"message", savedMessage.getSenderName()
									+ ": " + savedMessage.getContent(),
							"senderName", savedMessage.getSenderName(),
							"roomId", savedMessage.getRoomId().toString(),
							"type", "CHAT",
							"read", false,
							"createdAt", savedMessage.getCreatedAt().toString()
					);

					messagingTemplate.convertAndSend(
							"/topic/notifications/" + memberUserId,
							wsPayload
					);

					System.out.println(
							"✅ Notification sent to: " + memberUserId);
				}
			}

		} catch (Exception e) {
            /*
             NEVER BREAK CHAT FLOW
            */
			System.err.println("❌ Notification send failed");
			e.printStackTrace();
		}

		return savedMessage;
	}

	@Override
	public List<Message> getMessagesByRoom(
			UUID roomId,
			UUID currentUserId
	) {
		List<Message> messages =
				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

		return messages.stream()
				.filter(message -> {
					if (message.getDeletedForUsers() == null) {
						return true;
					}
					return !message.getDeletedForUsers().contains(currentUserId);
				})
				.toList();
	}

	@Override
	public void markMessagesAsSeen(UUID roomId, UUID currentUserId) {
		List<Message> messages =
				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

		for (Message message : messages) {
			if (!message.getSenderId().equals(currentUserId)) {
				message.setSeen(true);
			}
		}

		messageRepository.saveAll(messages);
	}

	@Override
	public void deleteMessageForMe(UUID messageId, UUID userId) {
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() ->
						new RuntimeException("Message not found"));

		message.getDeletedForUsers().add(userId);
		messageRepository.save(message);
	}

	@Override
	public void toggleReaction(UUID messageId, UUID userId, String emoji) {
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() ->
						new RuntimeException("Message not found"));

		if (message.getReactions() == null) {
			message.setReactions(new java.util.HashMap<>());
		}

        /*
         CASE 1: same emoji already exists → remove reaction
        */
		if (
				message.getReactions().containsKey(emoji) &&
						message.getReactions().get(emoji).contains(userId)
		) {
			message.getReactions().get(emoji).remove(userId);

			if (message.getReactions().get(emoji).isEmpty()) {
				message.getReactions().remove(emoji);
			}

		} else {

            /*
             REMOVE OLD REACTIONS
            */
			for (var entry : message.getReactions().entrySet()) {
				entry.getValue().remove(userId);
			}

            /*
             CLEAN EMPTY REACTIONS
            */
			message.getReactions()
					.entrySet()
					.removeIf(e -> e.getValue().isEmpty());

            /*
             ADD NEW REACTION
            */
			java.util.Set<UUID> users =
					message.getReactions()
							.getOrDefault(emoji, new java.util.HashSet<>());

			users.add(userId);
			message.getReactions().put(emoji, users);
		}

		messageRepository.save(message);
	}
}



//package com.microservices.messageservice.service.impl;
//
//import com.microservices.messageservice.client.NotificationProducer;
//import com.microservices.messageservice.dto.ChatMessage;
//import com.microservices.messageservice.dto.CreateMessageRequest;
//import com.microservices.messageservice.entity.Message;
//import com.microservices.messageservice.repository.MessageRepository;
//import com.microservices.messageservice.service.MessageService;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class MessageServiceImpl implements MessageService {
//
//	private final MessageRepository messageRepository;
//
//	private final NotificationProducer notificationProducer;
//
//	/*
//	 REAL-TIME ROOM MEMBER FETCH
//	*/
//	private final RestTemplate restTemplate;
//
//	public MessageServiceImpl(
//			MessageRepository messageRepository,
//			NotificationProducer notificationProducer,
//			RestTemplate restTemplate
//	) {
//		this.messageRepository = messageRepository;
//		this.notificationProducer = notificationProducer;
//		this.restTemplate = restTemplate;
//	}
//
//	@Override
//	public Message sendMessage(UUID userId, CreateMessageRequest request) {
//
//		Message message = new Message();
//
//		message.setSenderId(userId);
//
//		message.setSenderName(
//				request.getSenderName() != null &&
//						!request.getSenderName().isBlank()
//						? request.getSenderName()
//						: "User"
//		);
//
//		message.setRoomId(request.getRoomId());
//
//		message.setContent(request.getContent());
//
//		message.setCreatedAt(LocalDateTime.now());
//
//		/*
//		 NEW
//		*/
//		message.setSeen(false);
//
//		/*
//		 SAVE MESSAGE FIRST
//		*/
//		Message savedMessage =
//				messageRepository.save(message);
//
//		/*
//		 ==========================================
//		 WHATSAPP-LIKE NOTIFICATION FLOW
//		 ==========================================
//
//		 - notify OTHER users only
//		 - sender should NOT get notification
//		 - NEVER break chat flow
//		*/
//		try {
//
//			/*
//			 FETCH ROOM MEMBERS
//			 No Authorization header = internal call.
//			 room-service will skip membership check.
//			*/
//			String url =
//					"http://localhost:8082/rooms/"
//							+ savedMessage.getRoomId()
//							+ "/members";
//
//			ResponseEntity<List> response =
//					restTemplate.exchange(
//							url,
//							HttpMethod.GET,
//							null,
//							List.class
//					);
//
//			List<?> members = response.getBody();
//
//			if (members != null) {
//
//				for (Object obj : members) {
//
//					Map<?, ?> member =
//							(Map<?, ?>) obj;
//
//					String memberUserId =
//							String.valueOf(
//									member.get("userId")
//							);
//
//					/*
//					 SKIP SENDER
//					*/
//					if (
//							memberUserId.equals(
//									savedMessage
//											.getSenderId()
//											.toString()
//							)
//					) {
//						continue;
//					}
//
//					/*
//					 CREATE NOTIFICATION PAYLOAD
//					*/
//					ChatMessage notificationPayload =
//							new ChatMessage();
//
//					/*
//					 IMPORTANT:
//					 senderId field now stores
//					 TARGET USER ID
//					*/
//					notificationPayload.setSenderId(
//							memberUserId
//					);
//
//					notificationPayload.setSenderName(
//							savedMessage.getSenderName()
//					);
//
//					notificationPayload.setRoomId(
//							savedMessage
//									.getRoomId()
//									.toString()
//					);
//
//					notificationPayload.setContent(
//							savedMessage.getContent()
//					);
//
//					/*
//					 SEND TO RABBITMQ
//					*/
//					notificationProducer.send(
//							notificationPayload
//					);
//
//					System.out.println(
//							"✅ Notification sent to: "
//									+ memberUserId
//					);
//				}
//			}
//
//		} catch (Exception e) {
//
//			/*
//			 NEVER BREAK CHAT FLOW
//			*/
//			System.err.println(
//					"❌ Notification send failed"
//			);
//
//			e.printStackTrace();
//		}
//
//		return savedMessage;
//	}
//
//	@Override
//	public List<Message> getMessagesByRoom(
//			UUID roomId,
//			UUID currentUserId
//	) {
//
//		List<Message> messages =
//				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
//
//		return messages.stream()
//				.filter(message -> {
//
//					if (message.getDeletedForUsers() == null) {
//						return true;
//					}
//
//					return !message
//							.getDeletedForUsers()
//							.contains(currentUserId);
//				})
//				.toList();
//	}
//
//	@Override
//	public void markMessagesAsSeen(
//			UUID roomId,
//			UUID currentUserId
//	) {
//
//		List<Message> messages =
//				messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
//
//		for (Message message : messages) {
//
//			/*
//			 ONLY MESSAGES
//			 FROM OTHER USERS
//			*/
//			if (
//					!message.getSenderId()
//							.equals(currentUserId)
//			) {
//				message.setSeen(true);
//			}
//		}
//
//		messageRepository.saveAll(messages);
//	}
//
//	@Override
//	public void deleteMessageForMe(
//			UUID messageId,
//			UUID userId
//	) {
//
//		Message message =
//				messageRepository.findById(messageId)
//						.orElseThrow(() ->
//								new RuntimeException(
//										"Message not found"
//								)
//						);
//
//		message.getDeletedForUsers().add(userId);
//
//		messageRepository.save(message);
//	}
//
//	@Override
//	public void toggleReaction(
//			UUID messageId,
//			UUID userId,
//			String emoji
//	) {
//
//		Message message =
//				messageRepository.findById(messageId)
//						.orElseThrow(() ->
//								new RuntimeException(
//										"Message not found"
//								)
//						);
//
//		if (message.getReactions() == null) {
//			message.setReactions(
//					new java.util.HashMap<>()
//			);
//		}
//
//		/*
//		 CASE 1:
//		 same emoji already exists
//		 -> remove reaction
//		*/
//		if (
//				message.getReactions().containsKey(emoji)
//						&&
//						message.getReactions()
//								.get(emoji)
//								.contains(userId)
//		) {
//
//			message.getReactions()
//					.get(emoji)
//					.remove(userId);
//
//			if (
//					message.getReactions()
//							.get(emoji)
//							.isEmpty()
//			) {
//				message.getReactions()
//						.remove(emoji);
//			}
//
//		} else {
//
//			/*
//			 REMOVE OLD REACTIONS
//			*/
//			for (var entry :
//					message.getReactions().entrySet()) {
//
//				entry.getValue().remove(userId);
//			}
//
//			/*
//			 CLEAN EMPTY REACTIONS
//			*/
//			message.getReactions()
//					.entrySet()
//					.removeIf(e ->
//							e.getValue().isEmpty()
//					);
//
//			/*
//			 ADD NEW REACTION
//			*/
//			java.util.Set<UUID> users =
//					message.getReactions()
//							.getOrDefault(
//									emoji,
//									new java.util.HashSet<>()
//							);
//
//			users.add(userId);
//
//			message.getReactions()
//					.put(emoji, users);
//		}
//
//		messageRepository.save(message);
//	}
//}