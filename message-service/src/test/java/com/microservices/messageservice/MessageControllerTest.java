package com.microservices.messageservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.messageservice.controller.MessageController;
import com.microservices.messageservice.dto.CreateMessageRequest;
import com.microservices.messageservice.entity.Message;
import com.microservices.messageservice.security.JwtService;
import com.microservices.messageservice.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import(com.microservices.messageservice.config.SecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should send message successfully")
    void shouldSendMessageSuccessfully() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        CreateMessageRequest request =
                new CreateMessageRequest();

        request.setRoomId(roomId);
        request.setContent("Hello");
        request.setSenderName("Manas");

        Message response = new Message();

        response.setId(UUID.randomUUID());
        response.setSenderId(userId);
        response.setSenderName("Manas");
        response.setRoomId(roomId);
        response.setContent("Hello");
        response.setCreatedAt(LocalDateTime.now());

        when(jwtService.extractUserId(anyString()))
                .thenReturn(userId.toString());

        when(messageService.sendMessage(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/messages")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .value("Hello"))
                .andExpect(jsonPath("$.senderName")
                        .value("Manas"));
    }

    @Test
    @DisplayName("Should return bad request for invalid payload")
    void shouldReturnBadRequestForInvalidPayload() throws Exception {

        CreateMessageRequest request =
                new CreateMessageRequest();

        request.setContent("");

        mockMvc.perform(post("/messages")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fetch messages by room")
    void shouldFetchMessagesByRoom() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        Message message = new Message();

        message.setId(UUID.randomUUID());
        message.setContent("Test Message");
        message.setSenderName("Manas");

        when(jwtService.extractUserId(anyString()))
                .thenReturn(userId.toString());

        when(messageService.getMessagesByRoom(
                eq(roomId),
                eq(userId)
        )).thenReturn(List.of(message));

        mockMvc.perform(get("/messages")
                        .param("roomId", roomId.toString())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content")
                        .value("Test Message"));
    }

    @Test
    @DisplayName("Should mark messages as seen")
    void shouldMarkMessagesAsSeen() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        when(jwtService.extractUserId(anyString()))
                .thenReturn(userId.toString());

        mockMvc.perform(
                        put("/messages/{roomId}/seen", roomId)
                                .header("Authorization", "Bearer token")
                )
                .andExpect(status().isOk());

        verify(messageService, times(1))
                .markMessagesAsSeen(roomId, userId);
    }

    @Test
    @DisplayName("Should delete message for current user")
    void shouldDeleteMessageForCurrentUser() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        when(jwtService.extractUserId(anyString()))
                .thenReturn(userId.toString());

        mockMvc.perform(
                        put("/messages/{messageId}/delete/me", messageId)
                                .header("Authorization", "Bearer token")
                )
                .andExpect(status().isOk());

        verify(messageService, times(1))
                .deleteMessageForMe(messageId, userId);
    }

    @Test
    @DisplayName("Should react to message")
    void shouldReactToMessage() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        when(jwtService.extractUserId(anyString()))
                .thenReturn(userId.toString());

        mockMvc.perform(
                        put("/messages/{messageId}/react", messageId)
                                .param("emoji", "🔥")
                                .header("Authorization", "Bearer token")
                )
                .andExpect(status().isOk());

        verify(messageService, times(1))
                .toggleReaction(
                        messageId,
                        userId,
                        "🔥"
                );
    }

    @Test
    @DisplayName("Should return unauthorized when Authorization header missing")
    void shouldReturnUnauthorizedWhenAuthorizationMissing() throws Exception {

        mockMvc.perform(get("/messages"))
                .andExpect(status().is4xxClientError());
    }
}