package com.microservices.authservice.service;

import com.microservices.authservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	boolean validateToken(String token);

	UserProfileResponse getCurrentUser(String email);

	UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

	void changePassword(String email, ChangePasswordRequest request);

	List<UserSearchResponse> searchUsers(String keyword);

	UserProfileResponse updateStatus(String email, UpdateStatusRequest request);

	UserSearchResponse getUserById(UUID userId);

	void deleteUser(UUID userId);

}