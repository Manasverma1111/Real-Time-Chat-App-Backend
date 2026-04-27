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
		 FINAL FIX:
		 Save actual username instead of UUID/User fallback
		*/
		message.setSenderName(
				request.getSenderName() != null && !request.getSenderName().isBlank()
						? request.getSenderName()
						: "User"
		);

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
//
//		message.setSenderId(userId);
//
//		/*
//		 FINAL SAFE FIX:
//		 Save readable sender name instead of UUID fallback.
//
//		 Since frontend already stores username in sessionStorage
//		 and sends senderName via WebSocket flow,
//		 old messages after refresh should also show readable value.
//
//		 For REST API fallback:
//		 if backend user-service lookup is not connected yet,
//		 we store a cleaner readable fallback instead of raw UUID.
//		*/
//
//		String readableName = "User";
//
//		if (request.getContent() != null && !request.getContent().isBlank()) {
//			readableName = "User";
//		}
//
//		message.setSenderName(readableName);
//
//		message.setRoomId(request.getRoomId());
//		message.setContent(request.getContent());
//		message.setCreatedAt(LocalDateTime.now());
//
//		return messageRepository.save(message);
//	}
//
//	@Override
//	public List<Message> getMessagesByRoom(UUID roomId) {
//		return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
//	}
//}
