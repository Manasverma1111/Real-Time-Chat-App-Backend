package com.microservices.roomservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

//	SECRET_KEY is a constant string that represents the secret key used for signing and verifying JWT tokens.
	private static final String SECRET_KEY = "connecthubsupersecretkeyconnecthubsupersecretkey1234567890abcd";

//	extractUserId is a method that takes a JWT token as input and extracts the user ID from the token's claims.
	public String extractUserId(String token) {
		return extractAllClaims(token).get("userId", String.class);
	}

//	extractUsername is a method that takes a JWT token as input and extracts the username from the token's claims.
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

//	getSignInKey is a method that returns the signing key used for JWT token verification,
//	which is derived from the SECRET_KEY constant.
	private Key getSignInKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
	}
}