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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

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
				.status(UserStatus.ONLINE)
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
				.build();
	}

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
				.build();
	}

	@Override
	public void deleteUser(UUID userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		userRepository.delete(user); // simple hard delete (safe for now)
	}

	// ✅ NEW: Used ONLY for SUPER_ADMIN dashboard (includes role)
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
						.role(user.getRole().name()) // ✅ CRITICAL FIX
						.build())
				.toList();
	}
}




//package com.microservices.authservice.service.impl;
//
//import com.microservices.authservice.dto.*;
//import com.microservices.authservice.entity.AuthProvider;
//import com.microservices.authservice.entity.User;
//import com.microservices.authservice.entity.UserStatus;
//import com.microservices.authservice.repository.UserRepository;
//import com.microservices.authservice.security.JwtService;
//import com.microservices.authservice.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AuthServiceImpl implements AuthService {
//
//	private final UserRepository userRepository;
//	private final PasswordEncoder passwordEncoder;
//	private final JwtService jwtService;
//
//	@Override
//	public AuthResponse register(RegisterRequest request) {
//
//		if (userRepository.existsByEmail(request.getEmail())) {
//			throw new RuntimeException("Email already exists");
//		}
//
//		if (userRepository.existsByUsername(request.getUsername())) {
//			throw new RuntimeException("Username already exists");
//		}
//
//		User user = User.builder().fullName(request.getFullName()).username(request.getUsername())
//				.email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
//				.status(UserStatus.ONLINE).provider(AuthProvider.LOCAL).isActive(true).build();
//
//		User savedUser = userRepository.save(user);
//
//		String token = jwtService.generateToken(savedUser.getUserId(), savedUser.getEmail());
//
//		return AuthResponse.builder().token(token).userId(savedUser.getUserId()).username(savedUser.getUsername())
//				.email(savedUser.getEmail()).fullName(savedUser.getFullName()).build();
//	}
//
////	@Override
////	public AuthResponse login(LoginRequest request) {
////
////		User user = userRepository.findByEmail(request.getEmail())
////				.orElseThrow(() -> new RuntimeException("Invalid email or password"));
////
////		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
////			throw new RuntimeException("Invalid email or password");
////		}
////
////		String token = jwtService.generateToken(user.getUserId(), user.getEmail());
////
////		return AuthResponse.builder().token(token).userId(user.getUserId()).username(user.getUsername())
////				.email(user.getEmail()).fullName(user.getFullName()).build();
////	}
//
//	@Override
//	public AuthResponse login(LoginRequest request) {
//
//		User user = userRepository.findByEmail(request.getEmail())
//				.orElseThrow(() -> new RuntimeException("Invalid email or password"));
//
//		// Prevent Google users from password login
//		if (user.getProvider() == AuthProvider.GOOGLE) {
//			throw new RuntimeException("Please login using Google");
//		}
//
//		// Correct BCrypt password verification
//		if (!passwordEncoder.matches(
//				request.getPassword(),
//				user.getPasswordHash()
//		)) {
//			throw new RuntimeException("Invalid email or password");
//		}
//
//		String token = jwtService.generateToken(
//				user.getUserId(),
//				user.getEmail()
//		);
//
//		return AuthResponse.builder()
//				.token(token)
//				.userId(user.getUserId())
//				.email(user.getEmail())
//				.username(user.getUsername())
//				.fullName(user.getFullName())
//				.build();
//	}
//
//	@Override
//	public boolean validateToken(String token) {
//		return jwtService.isTokenValid(token);
//	}
//
//	@Override
//	public UserProfileResponse getCurrentUser(String email) {
//		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
//
//		return UserProfileResponse.builder().userId(user.getUserId()).username(user.getUsername())
//				.email(user.getEmail()).fullName(user.getFullName()).avatarUrl(user.getAvatarUrl()).bio(user.getBio())
//				.status(user.getStatus()).provider(user.getProvider()).isActive(user.getIsActive())
//				.lastSeenAt(user.getLastSeenAt()).createdAt(user.getCreatedAt()).build();
//	}
//
//	@Override
//	public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
//		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
//
//		user.setFullName(request.getFullName());
//		user.setAvatarUrl(request.getAvatarUrl());
//		user.setBio(request.getBio());
//
//		User updatedUser = userRepository.save(user);
//
//		return UserProfileResponse.builder().userId(updatedUser.getUserId()).username(updatedUser.getUsername())
//				.email(updatedUser.getEmail()).fullName(updatedUser.getFullName()).avatarUrl(updatedUser.getAvatarUrl())
//				.bio(updatedUser.getBio()).status(updatedUser.getStatus()).provider(updatedUser.getProvider())
//				.isActive(updatedUser.getIsActive()).lastSeenAt(updatedUser.getLastSeenAt())
//				.createdAt(updatedUser.getCreatedAt()).build();
//	}
//
//	@Override
//	public void changePassword(String email, ChangePasswordRequest request) {
//		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
//
//		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
//			throw new RuntimeException("Current password is incorrect");
//		}
//
//		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
//		userRepository.save(user);
//	}
//
//	@Override
//	public List<UserSearchResponse> searchUsers(String keyword) {
//		return userRepository.findByUsernameContainingIgnoreCase(keyword).stream()
//				.map(user -> UserSearchResponse.builder().userId(user.getUserId()).username(user.getUsername())
//						.fullName(user.getFullName()).avatarUrl(user.getAvatarUrl()).status(user.getStatus()).build())
//				.toList();
//	}
//
//	@Override
//	public UserProfileResponse updateStatus(String email, UpdateStatusRequest request) {
//		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
//
//		user.setStatus(request.getStatus());
//		User updatedUser = userRepository.save(user);
//
//		return UserProfileResponse.builder().userId(updatedUser.getUserId()).username(updatedUser.getUsername())
//				.email(updatedUser.getEmail()).fullName(updatedUser.getFullName()).avatarUrl(updatedUser.getAvatarUrl())
//				.bio(updatedUser.getBio()).status(updatedUser.getStatus()).provider(updatedUser.getProvider())
//				.isActive(updatedUser.getIsActive()).lastSeenAt(updatedUser.getLastSeenAt())
//				.createdAt(updatedUser.getCreatedAt()).build();
//	}
//}