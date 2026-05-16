//package com.microservices.messageservice;
//
//import com.microservices.messageservice.entity.Message;
//import com.microservices.messageservice.repository.MessageRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//class MessageRepositoryTest {
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Test
//    @DisplayName("Should save message successfully")
//    void shouldSaveMessageSuccessfully() {
//
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//
//        Message message = new Message();
//        message.setSenderId(senderId);
//        message.setSenderName("Manas");
//        message.setRoomId(roomId);
//        message.setContent("Hello World");
//        message.setCreatedAt(LocalDateTime.now());
//
//        Message savedMessage = messageRepository.save(message);
//
//        assertNotNull(savedMessage.getId());
//        assertEquals("Hello World", savedMessage.getContent());
//        assertEquals("Manas", savedMessage.getSenderName());
//        assertFalse(savedMessage.getSeen());
//    }
//
//    @Test
//    @DisplayName("Should fetch messages by roomId ordered by createdAt")
//    void shouldFetchMessagesByRoomIdOrderedByCreatedAt() {
//
//        UUID roomId = UUID.randomUUID();
//
//        Message firstMessage = new Message();
//        firstMessage.setSenderId(UUID.randomUUID());
//        firstMessage.setSenderName("User1");
//        firstMessage.setRoomId(roomId);
//        firstMessage.setContent("First");
//        firstMessage.setCreatedAt(LocalDateTime.now().minusMinutes(5));
//
//        Message secondMessage = new Message();
//        secondMessage.setSenderId(UUID.randomUUID());
//        secondMessage.setSenderName("User2");
//        secondMessage.setRoomId(roomId);
//        secondMessage.setContent("Second");
//        secondMessage.setCreatedAt(LocalDateTime.now());
//
//        messageRepository.save(firstMessage);
//        messageRepository.save(secondMessage);
//
//        List<Message> messages =
//                messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
//
//        assertEquals(2, messages.size());
//        assertEquals("First", messages.get(0).getContent());
//        assertEquals("Second", messages.get(1).getContent());
//    }
//
//    @Test
//    @DisplayName("Should persist reactions correctly")
//    void shouldPersistReactionsCorrectly() {
//
//        UUID userId = UUID.randomUUID();
//
//        Message message = new Message();
//        message.setSenderId(UUID.randomUUID());
//        message.setSenderName("Tester");
//        message.setRoomId(UUID.randomUUID());
//        message.setContent("Reacted Message");
//        message.setCreatedAt(LocalDateTime.now());
//
//        message.getReactions().put("🔥", Set.of(userId));
//
//        Message saved = messageRepository.save(message);
//
//        Message fetched =
//                messageRepository.findById(saved.getId()).orElse(null);
//
//        assertNotNull(fetched);
//        assertTrue(fetched.getReactions().containsKey("🔥"));
//        assertEquals(1, fetched.getReactions().get("🔥").size());
//    }
//
//    @Test
//    @DisplayName("Should persist deletedForUsers correctly")
//    void shouldPersistDeletedForUsersCorrectly() {
//
//        UUID deletedUser = UUID.randomUUID();
//
//        Message message = new Message();
//        message.setSenderId(UUID.randomUUID());
//        message.setSenderName("Delete Test");
//        message.setRoomId(UUID.randomUUID());
//        message.setContent("Delete For Me");
//        message.setCreatedAt(LocalDateTime.now());
//
//        message.getDeletedForUsers().add(deletedUser);
//
//        Message saved = messageRepository.save(message);
//
//        Message fetched =
//                messageRepository.findById(saved.getId()).orElse(null);
//
//        assertNotNull(fetched);
//        assertTrue(
//                fetched.getDeletedForUsers().contains(deletedUser)
//        );
//    }
//}