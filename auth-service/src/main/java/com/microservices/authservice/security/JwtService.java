package com.microservices.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

//	This class is responsible for generating and validating JSON Web Tokens (JWTs) for authentication purposes.
	@Value("${jwt.expiration}")
	private long jwtExpiration;

	private static final String SECRET_KEY = "connecthubsupersecretkeyconnecthubsupersecretkey1234567890abcd";

//	The generateToken() method creates a JWT token containing the user's email, user ID, and role as claims.
	public String generateToken(UUID userId, String email, String role) {
		return Jwts.builder().setSubject(email).claim("userId", userId.toString()).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
	}

//	The extractEmail() method retrieves the email (subject) from the JWT token,
//	which can be used for authentication and authorization purposes.
	public String extractEmail(String token) {
		return extractAllClaims(token).getSubject();
	}

//	The extractUserId() method retrieves the user ID from the JWT token,
	public boolean isTokenValid(String token) {
		try {
			extractAllClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

//	The isTokenValid() method checks if the JWT token is valid by attempting to extract claims from it.
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

//	The getSignInKey() method returns the signing key used to sign the JWT token,
	private Key getSignInKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
	}

//	The getRemainingValidity() method calculates the remaining validity of the JWT token
//	by comparing the expiration time with the current time.
	public long getRemainingValidity(String token) {
		Date expiration = extractAllClaims(token).getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}
}