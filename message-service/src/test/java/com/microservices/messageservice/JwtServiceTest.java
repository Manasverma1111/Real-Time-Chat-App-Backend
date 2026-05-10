package com.microservices.messageservice;

import com.microservices.messageservice.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService();

    private static final String SECRET_KEY =
            "connecthubsupersecretkeyconnecthubsupersecretkey1234567890abcd";

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Should extract userId successfully")
    void shouldExtractUserIdSuccessfully() {

        String userId =
                "123e4567-e89b-12d3-a456-426614174000";

        String token = Jwts.builder()
                .claim("userId", userId)
                .setSubject("test-user")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 100000)
                )
                .signWith(
                        getSignInKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();

        String extractedUserId =
                jwtService.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void shouldThrowExceptionForInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        assertThrows(Exception.class, () ->
                jwtService.extractUserId(invalidToken)
        );
    }

    @Test
    @DisplayName("Should throw exception for expired token")
    void shouldThrowExceptionForExpiredToken() {

        String token = Jwts.builder()
                .claim(
                        "userId",
                        "123e4567-e89b-12d3-a456-426614174000"
                )
                .setSubject("expired-user")
                .setIssuedAt(
                        new Date(System.currentTimeMillis() - 200000)
                )
                .setExpiration(
                        new Date(System.currentTimeMillis() - 100000)
                )
                .signWith(
                        getSignInKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();

        assertThrows(Exception.class, () ->
                jwtService.extractUserId(token)
        );
    }

    @Test
    @DisplayName("Should throw exception when userId claim missing")
    void shouldThrowExceptionWhenUserIdMissing() {

        String token = Jwts.builder()
                .setSubject("test-user")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 100000)
                )
                .signWith(
                        getSignInKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();

        String extractedUserId =
                jwtService.extractUserId(token);

        assertNull(extractedUserId);
    }
}