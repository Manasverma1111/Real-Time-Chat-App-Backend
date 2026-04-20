package com.microservices.authservice.controller;

import com.microservices.authservice.dto.*;
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

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
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