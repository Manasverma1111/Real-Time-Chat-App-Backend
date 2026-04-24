package com.microservices.authservice.controller;

import com.microservices.authservice.dto.*;
import com.microservices.authservice.security.CustomUserDetails;
import com.microservices.authservice.security.JwtService;
import com.microservices.authservice.service.AuthService;
import com.microservices.authservice.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final AuthService authService;
	private final TokenBlacklistService tokenBlacklistService;
	private final JwtService jwtService;

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/logout")
	public Map<String, String> logout(HttpServletRequest request) {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			long expiry = jwtService.getRemainingValidity(token);

			if (expiry > 0) {
				tokenBlacklistService.blacklistToken(token, expiry);
			}
		}

		return Map.of("message", "Logout successful");
	}

	@GetMapping("/validate")
	public Map<String, Boolean> validateToken(@RequestParam String token) {
		return Map.of("valid", authService.validateToken(token));
	}

	@GetMapping("/me")
	public UserProfileResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return authService.getCurrentUser(userDetails.getUsername());
	}

	@PutMapping("/profile")
	public UserProfileResponse updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
											 @Valid @RequestBody UpdateProfileRequest request) {
		return authService.updateProfile(userDetails.getUsername(), request);
	}

	@PutMapping("/password")
	public Map<String, String> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(userDetails.getUsername(), request);
		return Map.of("message", "Password changed successfully");
	}

	@GetMapping("/search")
	public List<UserSearchResponse> searchUsers(@RequestParam String keyword) {
		return authService.searchUsers(keyword);
	}

	@PutMapping("/status")
	public UserProfileResponse updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UpdateStatusRequest request) {
		return authService.updateStatus(userDetails.getUsername(), request);
	}
}