//package com.microservices.messageservice.service;
//
//import com.microservices.messageservice.client.NotificationProducer;
//import com.microservices.messageservice.dto.CreateMessageRequest;
//import com.microservices.messageservice.entity.Message;
//import com.microservices.messageservice.repository.MessageRepository;
//import com.microservices.messageservice.service.impl.MessageServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class MessageServiceImplTest {
//
//    @Mock
//    private MessageRepository messageRepository;
//
//    @Mock
//    private NotificationProducer notificationProducer;
//
//    /*
//     REQUIRED FOR NEW NOTIFICATION FLOW
//    */
//    @Mock
//    private RestTemplate restTemplate;
//
//    @InjectMocks
//    private MessageServiceImpl messageService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @DisplayName("Should send message successfully")
//    void shouldSendMessageSuccessfully() {
//
//        UUID userId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//
//        CreateMessageRequest request =
//                new CreateMessageRequest();
//
//        request.setRoomId(roomId);
//        request.setContent("Hello");
//        request.setSenderName("Manas");
//
//        Message savedMessage = new Message();
//
//        savedMessage.setId(UUID.randomUUID());
//        savedMessage.setSenderId(userId);
//        savedMessage.setSenderName("Manas");
//        savedMessage.setRoomId(roomId);
//        savedMessage.setContent("Hello");
//        savedMessage.setCreatedAt(LocalDateTime.now());
//        savedMessage.setSeen(false);
//
//        when(messageRepository.save(any(Message.class)))
//                .thenReturn(savedMessage);
//
//        /*
//         MOCK ROOM SERVICE MEMBER API
//        */
//        List<Map<String, Object>> mockMembers = new ArrayList<>();
//
//        Map<String, Object> member = new HashMap<>();
//        member.put("userId", UUID.randomUUID().toString());
//
//        mockMembers.add(member);
//
//        org.springframework.http.ResponseEntity<List> response =
//                org.springframework.http.ResponseEntity.ok(
//                        (List) mockMembers
//                );
//
//        when(restTemplate.exchange(
//                anyString(),
//                eq(org.springframework.http.HttpMethod.GET),
//                any(org.springframework.http.HttpEntity.class),
//                eq(List.class)
//        )).thenReturn(response);
//
//        Message result =
//                messageService.sendMessage(userId, request);
//
//        assertNotNull(result);
//        assertEquals("Hello", result.getContent());
//        assertEquals("Manas", result.getSenderName());
//        assertFalse(result.getSeen());
//
//        verify(messageRepository, times(1))
//                .save(any(Message.class));
//
//        verify(notificationProducer, atLeastOnce())
//                .send(any());
//    }
//
//    @Test
//    @DisplayName("Should use default sender name when blank")
//    void shouldUseDefaultSenderNameWhenBlank() {
//
//        UUID userId = UUID.randomUUID();
//
//        CreateMessageRequest request =
//                new CreateMessageRequest();
//
//        request.setRoomId(UUID.randomUUID());
//        request.setContent("Test");
//        request.setSenderName("");
//
//        when(messageRepository.save(any(Message.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        org.springframework.http.ResponseEntity<List> emptyResponse =
//                org.springframework.http.ResponseEntity.ok(
//                        new ArrayList<>()
//                );
//
//        when(restTemplate.exchange(
//                anyString(),
//                eq(org.springframework.http.HttpMethod.GET),
//                any(org.springframework.http.HttpEntity.class),
//                eq(List.class)
//        )).thenReturn(emptyResponse);
//
//        Message result =
//                messageService.sendMessage(userId, request);
//
//        assertEquals("User", result.getSenderName());
//    }
//
//    @Test
//    @DisplayName("Should fetch messages excluding deleted messages")
//    void shouldFetchMessagesExcludingDeletedMessages() {
//
//        UUID currentUserId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//
//        Message visibleMessage = new Message();
//        visibleMessage.setContent("Visible");
//
//        Message deletedMessage = new Message();
//        deletedMessage.setContent("Hidden");
//
//        deletedMessage.getDeletedForUsers()
//                .add(currentUserId);
//
//        List<Message> messages =
//                List.of(visibleMessage, deletedMessage);
//
//        when(messageRepository
//                .findByRoomIdOrderByCreatedAtAsc(roomId))
//                .thenReturn(messages);
//
//        List<Message> result =
//                messageService.getMessagesByRoom(
//                        roomId,
//                        currentUserId
//                );
//
//        assertEquals(1, result.size());
//        assertEquals("Visible", result.get(0).getContent());
//    }
//
//    @Test
//    @DisplayName("Should mark messages as seen")
//    void shouldMarkMessagesAsSeen() {
//
//        UUID currentUserId = UUID.randomUUID();
//        UUID otherUserId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//
//        Message ownMessage = new Message();
//        ownMessage.setSenderId(currentUserId);
//        ownMessage.setSeen(false);
//
//        Message otherMessage = new Message();
//        otherMessage.setSenderId(otherUserId);
//        otherMessage.setSeen(false);
//
//        List<Message> messages =
//                List.of(ownMessage, otherMessage);
//
//        when(messageRepository
//                .findByRoomIdOrderByCreatedAtAsc(roomId))
//                .thenReturn(messages);
//
//        messageService.markMessagesAsSeen(
//                roomId,
//                currentUserId
//        );
//
//        assertFalse(ownMessage.getSeen());
//        assertTrue(otherMessage.getSeen());
//
//        verify(messageRepository, times(1))
//                .saveAll(messages);
//    }
//
//    @Test
//    @DisplayName("Should delete message for current user")
//    void shouldDeleteMessageForCurrentUser() {
//
//        UUID messageId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        Message message = new Message();
//
//        when(messageRepository.findById(messageId))
//                .thenReturn(Optional.of(message));
//
//        messageService.deleteMessageForMe(
//                messageId,
//                userId
//        );
//
//        assertTrue(
//                message.getDeletedForUsers()
//                        .contains(userId)
//        );
//
//        verify(messageRepository, times(1))
//                .save(message);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when deleting non-existing message")
//    void shouldThrowExceptionWhenDeletingNonExistingMessage() {
//
//        UUID messageId = UUID.randomUUID();
//
//        when(messageRepository.findById(messageId))
//                .thenReturn(Optional.empty());
//
//        RuntimeException exception =
//                assertThrows(RuntimeException.class, () ->
//                        messageService.deleteMessageForMe(
//                                messageId,
//                                UUID.randomUUID()
//                        )
//                );
//
//        assertEquals(
//                "Message not found",
//                exception.getMessage()
//        );
//    }
//
//    @Test
//    @DisplayName("Should add reaction successfully")
//    void shouldAddReactionSuccessfully() {
//
//        UUID messageId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        Message message = new Message();
//
//        when(messageRepository.findById(messageId))
//                .thenReturn(Optional.of(message));
//
//        messageService.toggleReaction(
//                messageId,
//                userId,
//                "🔥"
//        );
//
//        assertTrue(
//                message.getReactions()
//                        .containsKey("🔥")
//        );
//
//        assertTrue(
//                message.getReactions()
//                        .get("🔥")
//                        .contains(userId)
//        );
//
//        verify(messageRepository, times(1))
//                .save(message);
//    }
//
//    @Test
//    @DisplayName("Should replace old reaction with new reaction")
//    void shouldReplaceOldReactionWithNewReaction() {
//
//        UUID messageId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        Message message = new Message();
//
//        message.getReactions()
//                .put("❤️", new HashSet<>(Set.of(userId)));
//
//        when(messageRepository.findById(messageId))
//                .thenReturn(Optional.of(message));
//
//        messageService.toggleReaction(
//                messageId,
//                userId,
//                "🔥"
//        );
//
//        assertFalse(
//                message.getReactions()
//                        .containsKey("❤️")
//        );
//
//        assertTrue(
//                message.getReactions()
//                        .containsKey("🔥")
//        );
//    }
//
//    @Test
//    @DisplayName("Should remove reaction when toggled again")
//    void shouldRemoveReactionWhenToggledAgain() {
//
//        UUID messageId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        Message message = new Message();
//
//        message.getReactions()
//                .put("🔥", new HashSet<>(Set.of(userId)));
//
//        when(messageRepository.findById(messageId))
//                .thenReturn(Optional.of(message));
//
//        messageService.toggleReaction(
//                messageId,
//                userId,
//                "🔥"
//        );
//
//        assertFalse(
//                message.getReactions()
//                        .containsKey("🔥")
//        );
//    }
//}