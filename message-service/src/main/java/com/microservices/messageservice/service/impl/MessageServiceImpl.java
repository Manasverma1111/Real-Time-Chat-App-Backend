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

		/*
		 TEMP SAFE FIX:
		 Until auth-service user profile API is connected,
		 we use UUID fallback.
		 Frontend will send senderName through websocket.
		*/
		message.setSenderName(userId.toString());

		message.setRoomId(request.getRoomId());
		message.setContent(request.getContent());
		message.setCreatedAt(LocalDateTime.now());

		return messageRepository.save(message);
	}

	@Override
	public List<Message> getMessagesByRoom(UUID roomId) {
		return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
	}
}


//package com.microservices.messageservice.service.impl;
//
//import com.microservices.messageservice.dto.CreateMessageRequest;
//import com.microservices.messageservice.entity.Message;
//import com.microservices.messageservice.repository.MessageRepository;
//import com.microservices.messageservice.service.MessageService;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class MessageServiceImpl implements MessageService {
//
//	private final MessageRepository messageRepository;
//
//	public MessageServiceImpl(MessageRepository messageRepository) {
//		this.messageRepository = messageRepository;
//	}
//
//	@Override
//	public Message sendMessage(UUID userId, CreateMessageRequest request) {
//		Message message = new Message();
//		message.setSenderId(userId);
//		message.setRoomId(request.getRoomId());
//		message.setContent(request.getContent());
//		message.setCreatedAt(LocalDateTime.now());
//		return messageRepository.save(message);
//	}
//
//	@Override
//	public List<Message> getMessagesByRoom(UUID roomId) {
//		return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
//	}
//}