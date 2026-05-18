package com.microservices.authservice.controller;

import com.microservices.authservice.dto.*;
import com.microservices.authservice.entity.UserStatus;
import com.microservices.authservice.security.CustomUserDetails;
import com.microservices.authservice.security.JwtService;
import com.microservices.authservice.service.AuthService;
import com.microservices.authservice.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final AuthService authService;
	private final TokenBlacklistService tokenBlacklistService;
	private final JwtService jwtService;

//	Endpoints for user registration, login, logout, token validation, profile management,
//	user search, and admin operations
	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

//	Handles user login by validating credentials and returning an authentication response
//	containing a JWT token and user details.
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

//	Handles user logout by blacklisting the JWT token to prevent further use
//	and updating the user's status to offline.
@PostMapping("/logout")
public Map<String, String> logout(
		HttpServletRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
) {
	String authHeader = request.getHeader("Authorization");
	String token = null;

	if (authHeader != null && authHeader.startsWith("Bearer ")) {
		token = authHeader.substring(7);

		long expiry = jwtService.getRemainingValidity(token);
		if (expiry > 0) {
			tokenBlacklistService.blacklistToken(token, expiry);
		}
	}

	String email = null;

	if (userDetails != null) {
		email = userDetails.getUsername();
		System.out.println("✅ Logout via userDetails: " + email);
	} else if (token != null) {
		try {
			email = jwtService.extractEmail(token);
			System.out.println("✅ Logout via token extraction: " + email);
		} catch (Exception e) {
			System.err.println("❌ Token extraction failed: " + e.getMessage());
		}
	} else {
		System.err.println("❌ No token and no userDetails on logout");
	}

	if (email != null) {
		UpdateStatusRequest req = new UpdateStatusRequest();
		req.setStatus(UserStatus.OFFLINE);
		authService.updateStatus(email, req);
		System.out.println("✅ Status set to OFFLINE for: " + email);
	} else {
		System.err.println("❌ Could not determine user email — status NOT updated");
	}

	return Map.of("message", "Logout successful");
}
//	Validates the provided JWT token and returns a response indicating whether the token is valid or not.

	@GetMapping("/validate")
	public Map<String, Boolean> validateToken(@RequestParam String token) {
		return Map.of("valid", authService.validateToken(token));
	}

//	Retrieves the profile information of the currently authenticated user
//	based on their username extracted from the security context,
	@GetMapping("/me")
	public UserProfileResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return authService.getCurrentUser(userDetails.getUsername());
	}

//	Allows the authenticated user to update their profile information such as full name,
//	avatar URL, and bio by providing a request body with the updated details.
	@PutMapping("/profile")
	public UserProfileResponse updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
											 @Valid @RequestBody UpdateProfileRequest request) {
		return authService.updateProfile(userDetails.getUsername(), request);
	}

//	Enables the authenticated user to change their password
//	by providing the current password and the new password in the request body.
	@PutMapping("/password")
	public Map<String, String> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
											  @Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(userDetails.getUsername(), request);
		return Map.of("message", "Password changed successfully");
	}

//	Allows users to search for other users by providing a keyword,
	@GetMapping("/search")
	public List<UserSearchResponse> searchUsers(@RequestParam String keyword) {
		return authService.searchUsers(keyword);
	}

	/*
FETCH USER BY ID
USED BY ROOM SERVICE TO SHOW USERNAME IN MEMBERS MODAL
*/
	@GetMapping("/user/{userId}")
	public UserSearchResponse getUserById(
			@PathVariable UUID userId
	) {
		return authService.getUserById(userId);
	}

//	Allows the authenticated user to update their online status (e.g., online, offline, away)
	@PutMapping("/status")
	public UserProfileResponse updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
											@Valid @RequestBody UpdateStatusRequest request) {
		return authService.updateStatus(userDetails.getUsername(), request);
	}

//	Provides administrative endpoints for super admins to manage users,
//	including retrieving a list of all users and deleting specific users by their ID.
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@GetMapping("/super-admin/users")
	public List<UserProfileResponse> getAllUsers() {
		return authService.getAllUsersForAdmin(); // ✅ FIXED
	}

//	Allows a super admin to delete a user by their ID,
//	which involves removing the user from the database and returning a success message upon completion.
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@DeleteMapping("/super-admin/user/{userId}")
	public Map<String, String> deleteUser(@PathVariable UUID userId) {
		authService.deleteUser(userId);
		return Map.of("message", "User deleted successfully");
	}

}
