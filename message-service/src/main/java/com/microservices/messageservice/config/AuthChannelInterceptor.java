package com.microservices.messageservice.config;

import com.microservices.messageservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

//	AuthChannelInterceptor is a Spring component that implements the ChannelInterceptor
//	interface to intercept WebSocket messages and perform authentication based on JWT tokens.
	private final JwtService jwtService;

//	The preSend method is overridden to intercept messages before they are sent to the message channel.
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		// ✅ wrap() to read headers — then we rebuild the message at the end
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		accessor.setLeaveMutable(true);

//		The method checks if the STOMP command is CONNECT, which indicates a new WebSocket connection.
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			List<String> headers = accessor.getNativeHeader("Authorization");

			if (headers != null && !headers.isEmpty()) {
				String authHeader = headers.get(0);

				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					String token = authHeader.substring(7);
					String userId = jwtService.extractUserId(token);

					accessor.setUser(new StompPrincipal(userId));

					Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
					if (sessionAttributes != null) {
						sessionAttributes.put("userId", userId);
					}

					System.out.println("WebSocket user set = " + userId);
				}
			}

//			If the command is not CONNECT, it checks if the user is already set in the accessor.
//			If not, it attempts to restore the user from the session attributes,
//			allowing for session persistence across WebSocket connections.
		} else {
			if (accessor.getUser() == null) {
				Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
				if (sessionAttributes != null) {
					Object userId = sessionAttributes.get("userId");
					if (userId != null) {
						accessor.setUser(new StompPrincipal(userId.toString()));
						System.out.println("WebSocket user restored = " + userId);
					}
				}
			}
		}

		// always return a NEW message built from the mutated accessor
		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	}

//	The StompPrincipal class is a simple implementation of the Principal interface
//	that holds the user's name (or ID) for authentication purposes in WebSocket sessions.
	private static class StompPrincipal implements Principal {
		private final String name;

//		The constructor of the StompPrincipal class takes a name (or user ID) as a parameter and assigns it to the name field.
		public StompPrincipal(String name) {
			this.name = name;
		}

//		The getName method is overridden to return the name (or user ID) of the principal,
//		which is used for authentication in WebSocket sessions.
		@Override
		public String getName() {
			return name;
		}
	}
}