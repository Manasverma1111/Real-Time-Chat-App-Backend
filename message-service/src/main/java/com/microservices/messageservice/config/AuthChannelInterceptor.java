package com.microservices.messageservice.config;

import com.microservices.messageservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtService jwtService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			List<String> headers = accessor.getNativeHeader("Authorization");
			System.out.println("CONNECT Authorization headers = " + headers);

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

		return message;
	}

	private static class StompPrincipal implements Principal {
		private final String name;

		public StompPrincipal(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}