package com.microservices.authservice;

import com.microservices.authservice.dto.*;
import com.microservices.authservice.entity.*;
import com.microservices.authservice.repository.UserRepository;
import com.microservices.authservice.security.JwtService;
import com.microservices.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------------- REGISTER ----------------

    @Test
    void register_shouldCreateUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");
        request.setUsername("testuser");
        request.setPassword("password");
        request.setFullName("Test User");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        User savedUser = User.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .passwordHash("encoded")
                .role(GlobalRole.USER)
                .build();

        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("token", response.getToken());

        verify(userRepository).save(any());
    }

    @Test
    void register_shouldFail_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
    }

    // ---------------- LOGIN ----------------

    @Test
    void login_shouldReturnToken_whenValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("password");

        User user = User.builder()
                .userId(UUID.randomUUID())
                .email("test@mail.com")
                .passwordHash("encoded")
                .provider(AuthProvider.LOCAL)
                .role(GlobalRole.USER)
                .build();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(true);

        when(jwtService.generateToken(any(), any(), any()))
                .thenReturn("token");

        AuthResponse response = authService.login(request);

        assertEquals("token", response.getToken());
        assertEquals(UserStatus.ONLINE, user.getStatus());

        verify(userRepository).save(user);
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@mail.com");

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_shouldFail_whenWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("wrong");

        User user = User.builder()
                .email("test@mail.com")
                .passwordHash("encoded")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_shouldFail_whenGoogleUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");

        User user = User.builder()
                .email("test@mail.com")
                .provider(AuthProvider.GOOGLE)
                .build();

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    // ---------------- CHANGE PASSWORD ----------------

    @Test
    void changePassword_shouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");

        User user = User.builder()
                .email("test@mail.com")
                .passwordHash("encoded")
                .build();

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("old", "encoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("new"))
                .thenReturn("newEncoded");

        authService.changePassword("test@mail.com", request);

        assertEquals("newEncoded", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldFail_whenWrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");

        User user = User.builder()
                .passwordHash("encoded")
                .build();

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.changePassword("test@mail.com", request));
    }

    // ---------------- VALIDATE TOKEN ----------------

    @Test
    void validateToken_shouldReturnTrue() {
        when(jwtService.isTokenValid("token")).thenReturn(true);

        assertTrue(authService.validateToken("token"));
    }

    // ---------------- SEARCH USERS ----------------

    @Test
    void searchUsers_shouldReturnList() {
        User user = User.builder()
                .userId(UUID.randomUUID())
                .username("john")
                .fullName("John Doe")
                .build();

        when(userRepository.findByUsernameContainingIgnoreCase("jo"))
                .thenReturn(List.of(user));

        List<UserSearchResponse> result = authService.searchUsers("jo");

        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());
    }

    // ---------------- DELETE USER ----------------

    @Test
    void deleteUser_shouldDeleteUser() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.of(user));

        authService.deleteUser(userId);

        verify(userRepository).delete(user);
    }
}