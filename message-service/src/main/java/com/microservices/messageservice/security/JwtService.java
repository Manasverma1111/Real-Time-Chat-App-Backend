package com.microservices.messageservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

//	SECRET_KEY is a constant string that serves as the secret key for signing and verifying JWT tokens.
	private static final String SECRET_KEY = "connecthubsupersecretkeyconnecthubsupersecretkey1234567890abcd";

//	extractUserId() method takes a JWT token as input, extracts the claims from the token,
//	and retrieves the "userId" claim as a String.
	public String extractUserId(String token) {
		return extractAllClaims(token).get("userId", String.class);
	}

//	extractAllClaims() method uses the Jwts.parserBuilder() to create a JWT parser,
//	sets the signing key using getSignInKey(),
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

//	getSignInKey() method converts the SECRET_KEY string into a byte array using UTF-8 encoding
//	and creates a Key object using the Keys.hmacShaKeyFor() method from the jjwt library.
	private Key getSignInKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
	}
}