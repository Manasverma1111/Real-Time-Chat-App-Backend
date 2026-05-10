package com.microservices.messageservice;

import com.microservices.messageservice.config.AuthChannelInterceptor;
import com.microservices.messageservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageChannel messageChannel;

    private AuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new AuthChannelInterceptor(jwtService);
    }

    // ✅ always read user from the RETURNED message's wrapped accessor
    private StompHeaderAccessor wrap(Message<?> result) {
        return StompHeaderAccessor.wrap(result);
    }

    @Test
    @DisplayName("Should authenticate websocket user successfully")
    void shouldAuthenticateWebSocketUserSuccessfully() {

        String token = "valid-token";
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        when(jwtService.extractUserId(token)).thenReturn(userId);

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeaders(accessor)
                .build();

        // ✅ check the RETURNED message, not the original
        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = wrap(result);

        Principal user = resultAccessor.getUser();
        assertNotNull(user, "User principal should not be null after authentication");
        assertEquals(userId, user.getName());
        assertEquals(userId, resultAccessor.getSessionAttributes().get("userId"));
    }

    @Test
    @DisplayName("Should ignore missing Authorization header")
    void shouldIgnoreMissingAuthorizationHeader() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeaders(accessor)
                .build();

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = wrap(result);

        assertNull(resultAccessor.getUser());
        verify(jwtService, never()).extractUserId(anyString());
    }

    @Test
    @DisplayName("Should restore websocket user from session")
    void shouldRestoreWebSocketUserFromSession() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);

        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", "restored-user");
        accessor.setSessionAttributes(sessionAttributes);

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeaders(accessor)
                .build();

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = wrap(result);

        assertNotNull(resultAccessor.getUser(), "User should be restored from session");
        assertEquals("restored-user", resultAccessor.getUser().getName());
    }

    @Test
    @DisplayName("Should handle invalid bearer token format")
    void shouldHandleInvalidBearerTokenFormat() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "InvalidToken");
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeaders(accessor)
                .build();

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = wrap(result);

        assertNull(resultAccessor.getUser());
        verify(jwtService, never()).extractUserId(anyString());
    }
}