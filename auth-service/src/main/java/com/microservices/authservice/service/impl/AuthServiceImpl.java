package com.microservices.authservice.service.impl;

import com.microservices.authservice.dto.*;
import com.microservices.authservice.entity.AuthProvider;
import com.microservices.authservice.entity.User;
import com.microservices.authservice.entity.UserStatus;
import com.microservices.authservice.repository.UserRepository;
import com.microservices.authservice.security.JwtService;
import com.microservices.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

//	This class implements the AuthService interface,
//	providing concrete implementations for user authentication and profile management functionalities.
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	/*
     RestTemplate for calling presence-service
     when user logs in or out, so WebSocket
     broadcast fires and all UIs update in real time.
    */
	private final RestTemplate restTemplate;


	//	The register() method handles user registration by validating the uniqueness of email and username,
//	creating a new User entity, saving it to the database, and generating a JWT token for the newly registered user.
	@Override
	public AuthResponse register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}

		User user = User.builder()
				.fullName(request.getFullName())
				.username(request.getUsername())
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.status(UserStatus.OFFLINE)
				.provider(AuthProvider.LOCAL)
				.isActive(true)
				.build();

		User savedUser = userRepository.save(user);

		String token = jwtService.generateToken(
				savedUser.getUserId(),
				savedUser.getEmail(),
				savedUser.getRole().name()
		);

		return AuthResponse.builder()
				.token(token)
				.userId(savedUser.getUserId())
				.username(savedUser.getUsername())
				.email(savedUser.getEmail())
				.fullName(savedUser.getFullName())
				.role(savedUser.getRole().name())
				.build();
	}

//	The login() method handles user authentication by verifying the provided email and password against the stored user data.
//	It also includes a critical fix to mark the user as ONLINE upon successful login,
//	ensuring that the user's status is accurately reflected in the system.
	@Override
	public AuthResponse login(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() ->
						new BadCredentialsException("Invalid email or password")
				);

		// Prevent Google users from normal password login
		if (user.getProvider() == AuthProvider.GOOGLE) {
			throw new BadCredentialsException("Please login using Google");
		}

		// Correct BCrypt password verification
		if (!passwordEncoder.matches(
				request.getPassword(),
				user.getPasswordHash()
		)) {
			throw new BadCredentialsException("Invalid email or password");
		}

		// FIX: mark user ONLINE
		user.setStatus(UserStatus.ONLINE);
		userRepository.save(user);

		 /*
         NOTIFY PRESENCE-SERVICE ON LOGIN
         so WebSocket broadcast fires immediately
        */
		notifyPresence(user.getUserId(), "online");

		String token = jwtService.generateToken(
				user.getUserId(),
				user.getEmail(),
				user.getRole().name()
		);

		return AuthResponse.builder()
				.token(token)
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.fullName(user.getFullName())
				.avatarUrl(user.getAvatarUrl())
				.role(user.getRole().name())
				.build();
	}

//	The validateToken() method checks the validity of a given JWT token using the JwtService,
//	ensuring that only authenticated users can access protected resources in the application.
	@Override
	public boolean validateToken(String token) {
		return jwtService.isTokenValid(token);
	}

	@Override
	public UserProfileResponse getCurrentUser(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() ->
						new RuntimeException("User not found")
				);

		return UserProfileResponse.builder()
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.fullName(user.getFullName())
				.avatarUrl(user.getAvatarUrl())
				.bio(user.getBio())
				.status(user.getStatus())
				.provider(user.getProvider())
				.isActive(user.getIsActive())
				.lastSeenAt(user.getLastSeenAt())
				.createdAt(user.getCreatedAt())
				.role(user.getRole().name())
				.build();
	}

//	The updateProfile() method allows users to update their profile information such as full name, avatar URL, and bio.
//	It retrieves the user based on the provided email, updates the relevant fields, saves the changes to the database,
	@Override
	public UserProfileResponse updateProfile(
			String email,
			UpdateProfileRequest request
	) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() ->
						new RuntimeException("User not found")
				);

		user.setFullName(request.getFullName());
		user.setAvatarUrl(request.getAvatarUrl());
		user.setBio(request.getBio());

		User updatedUser = userRepository.save(user);

		return UserProfileResponse.builder()
				.userId(updatedUser.getUserId())
				.username(updatedUser.getUsername())
				.email(updatedUser.getEmail())
				.fullName(updatedUser.getFullName())
				.avatarUrl(updatedUser.getAvatarUrl())
				.bio(updatedUser.getBio())
				.status(updatedUser.getStatus())
				.provider(updatedUser.getProvider())
				.isActive(updatedUser.getIsActive())
				.lastSeenAt(updatedUser.getLastSeenAt())
				.createdAt(updatedUser.getCreatedAt())
				.role(updatedUser.getRole().name())
				.build();
	}

//	The changePassword() method enables users to change their password by verifying the current password and updating it with a new one.
//	It ensures that the current password provided by the user matches the stored password hash before allowing the update,
	@Override
	public void changePassword(
			String email,
			ChangePasswordRequest request
	) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() ->
						new RuntimeException("User not found")
				);

		if (!passwordEncoder.matches(
				request.getCurrentPassword(),
				user.getPasswordHash()
		)) {
			throw new BadCredentialsException("Current password is incorrect");
		}

		user.setPasswordHash(
				passwordEncoder.encode(request.getNewPassword())
		);

		userRepository.save(user);
	}

//	The searchUsers() method allows users to search for other users based on a keyword that matches their username.
//	It retrieves a list of users whose usernames contain the specified keyword (case-insensitive)
//	and maps them to a UserSearchResponse DTO for returning the search results.
	@Override
	public List<UserSearchResponse> searchUsers(String keyword) {

		return userRepository
				.findByUsernameContainingIgnoreCase(keyword)
				.stream()
				.map(user -> UserSearchResponse.builder()
						.userId(user.getUserId())
						.username(user.getUsername())
						.fullName(user.getFullName())
						.avatarUrl(user.getAvatarUrl())
						.status(user.getStatus())
						.build())
				.toList();
	}

//	The updateStatus() method allows users to update their online status (e.g., ONLINE, OFFLINE, AWAY).
//	It retrieves the user based on the provided email, updates the status field, saves the changes to the database,
//	and returns the updated user profile information in a UserProfileResponse DTO.
	@Override
	public UserProfileResponse updateStatus(
			String email,
			UpdateStatusRequest request
	) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() ->
						new RuntimeException("User not found")
				);

		user.setStatus(request.getStatus());

		User updatedUser = userRepository.save(user);

		/*
         CRITICAL FIX:
         Notify presence-service so it calls message-service
         which broadcasts via WebSocket to all frontends.
         Without this, logout never triggers a real-time update.
        */
		String endpoint = request.getStatus() == UserStatus.ONLINE
				? "online"
				: "offline";

		notifyPresence(updatedUser.getUserId(), endpoint);

		return UserProfileResponse.builder()
				.userId(updatedUser.getUserId())
				.username(updatedUser.getUsername())
				.email(updatedUser.getEmail())
				.fullName(updatedUser.getFullName())
				.avatarUrl(updatedUser.getAvatarUrl())
				.bio(updatedUser.getBio())
				.status(updatedUser.getStatus())
				.provider(updatedUser.getProvider())
				.isActive(updatedUser.getIsActive())
				.lastSeenAt(updatedUser.getLastSeenAt())
				.createdAt(updatedUser.getCreatedAt())
				.build();
	}

//	The getUserById() method retrieves a user's profile information based on their unique user ID (UUID).
//	It fetches the user from the database using the user ID
//	and returns the relevant profile information in a UserSearchResponse DTO.
	@Override
	public UserSearchResponse getUserById(UUID userId) {

		User user = userRepository.findByUserId(userId)
				.orElseThrow(() ->
						new RuntimeException("User not found")
				);

		return UserSearchResponse.builder()
				.userId(user.getUserId())
				.username(user.getUsername())
				.fullName(user.getFullName())
				.avatarUrl(user.getAvatarUrl())
				.status(user.getStatus())
				.bio(user.getBio())
				.email(user.getEmail())
				.role(user.getRole().name())
				.build();
	}

//	The deleteUser() method allows for the deletion of a user account based on their unique user ID (UUID).
//	It retrieves the user from the database using the user ID and deletes the user entity from the database.
	@Override
	public void deleteUser(UUID userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		userRepository.delete(user); // simple hard delete (safe for now)
	}

	// NEW: Used ONLY for SUPER_ADMIN dashboard (includes role)
	@Override
	public List<UserProfileResponse> getAllUsersForAdmin() {
		return userRepository.findAll()
				.stream()
				.map(user -> UserProfileResponse.builder()
						.userId(user.getUserId())
						.username(user.getUsername())
						.email(user.getEmail())
						.fullName(user.getFullName())
						.avatarUrl(user.getAvatarUrl())
						.bio(user.getBio())
						.status(user.getStatus())
						.provider(user.getProvider())
						.isActive(user.getIsActive())
						.lastSeenAt(user.getLastSeenAt())
						.createdAt(user.getCreatedAt())
						.role(user.getRole().name())
						.build())
				.toList();
	}


	/*
     INTERNAL HELPER
     Calls presence-service to mark user online/offline.
     Presence-service then calls message-service /internal/presence
     which broadcasts to /topic/presence via WebSocket.
     Fire-and-forget — never breaks auth flow.
    */
	private void notifyPresence(UUID userId, String endpoint) {
		try {
			restTemplate.postForObject(
					"http://localhost:8084/presence/" + endpoint + "/" + userId,
					null,
					Void.class
			);
		} catch (Exception e) {
			System.err.println(
					" Could not notify presence-service: " + e.getMessage()
			);
		}
	}


}
