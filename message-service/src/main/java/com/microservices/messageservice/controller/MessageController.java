package com.microservices.messageservice.controller;

import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.security.JwtService;
import com.microservices.messageservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;
	private final JwtService jwtService;

	@PostMapping
	public Message sendMessage(@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody CreateMessageRequest request) {

		String token = authHeader.substring(7);
		String userId = jwtService.extractUserId(token);

		return messageService.sendMessage(UUID.fromString(userId), request);
	}

	@GetMapping
	public List<Message> getMessagesByRoom(@RequestParam UUID roomId) {
		return messageService.getMessagesByRoom(roomId);
	}
}