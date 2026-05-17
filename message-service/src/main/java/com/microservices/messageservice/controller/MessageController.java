package com.microservices.messageservice.controller;

import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.security.JwtService;
import com.microservices.messageservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

//	MessageController is a Spring REST controller that handles HTTP requests related to messages in a chat application.
	private final MessageService messageService;
	private final JwtService jwtService;

	/*
    WebSocket broker — used to push seen events
    to the sender in real time
   */
	private final SimpMessagingTemplate messagingTemplate;


	//	The sendMessage method is annotated with @PostMapping,
//	which means it will handle HTTP POST requests to the "/messages" endpoint.
	@PostMapping
	public Message sendMessage(@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody CreateMessageRequest request) {

		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		return messageService.sendMessage(UUID.fromString(userId), request);
	}

//	The getMessagesByRoom method is annotated with @GetMapping,
//	which means it will handle HTTP GET requests to the "/messages" endpoint.
	@GetMapping
	public List<Message> getMessagesByRoom(
			@RequestParam UUID roomId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestHeader("Authorization") String authHeader
	) {

//		The method takes several parameters: roomId (the ID of the chat room),
//		page and size for pagination, and an Authorization header containing a JWT token.
		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		return messageService.getMessagesByRoom(
				roomId,
				UUID.fromString(userId),
				page,
				size
		);
	}

//	The markMessagesSeen method is annotated with @PutMapping("/{roomId}/seen"),
//	which means it will handle HTTP PUT requests to the "/messages/{roomId}/seen" endpoint,
//	where {roomId} is a path variable representing the ID of the chat room.
	@PutMapping("/{roomId}/seen")
	public void markMessagesSeen(
			@PathVariable UUID roomId,
			@RequestHeader("Authorization") String authHeader
	) {
		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		messageService.markMessagesAsSeen(
				roomId,
				UUID.fromString(userId)
		);

		/*
         BROADCAST SEEN EVENT VIA WEBSOCKET
         Notifies all subscribers in the room that
         messages have been read by userId.
         Sender receives this and updates ✓ → ✓✓ in real time.
        */
		messagingTemplate.convertAndSend(
				"/topic/seen/" + roomId,
				Map.of(
						"roomId", roomId.toString(),
						"seenByUserId", userId
				)
		);

		System.out.println(
				"✅ Seen event broadcast → room: " + roomId
						+ " | seenBy: " + userId
		);
	}

//	The deleteMessageForMe method is annotated with @PutMapping("/{messageId}/delete/me"),
//	which means it will handle HTTP PUT requests to the "/messages/{messageId}/delete/me" endpoint,
//	where {messageId} is a path variable representing the ID of the message to be deleted for the user.
	@PutMapping("/{messageId}/delete/me")
	public void deleteMessageForMe(
			@PathVariable UUID messageId,
			@RequestHeader("Authorization") String authHeader
	) {
		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		messageService.deleteMessageForMe(
				messageId,
				UUID.fromString(userId)
		);
	}

//	The reactToMessage method is annotated with @PutMapping("/{messageId}/react"),
//	which means it will handle HTTP PUT requests to the "/messages/{messageId}/react" endpoint,
//	where {messageId} is a path variable representing the ID of the message to which the user wants to react.
	@PutMapping("/{messageId}/react")
	public void reactToMessage(
			@PathVariable UUID messageId,
			@RequestParam String emoji,
			@RequestHeader("Authorization") String authHeader
	) {
		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		messageService.toggleReaction(
				messageId,
				UUID.fromString(userId),
				emoji
		);
	}
}