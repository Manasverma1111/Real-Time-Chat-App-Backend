//package com.microservices.authservice;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.microservices.authservice.controller.AuthResource;
//import com.microservices.authservice.dto.*;
//import com.microservices.authservice.security.CustomUserDetailsService;
//import com.microservices.authservice.security.JwtAuthenticationFilter;
//import com.microservices.authservice.security.JwtService;
//import com.microservices.authservice.service.AuthService;
//import com.microservices.authservice.service.TokenBlacklistService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthResource.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(AuthResourceTest.TestConfig.class)
//class AuthResourceTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private TokenBlacklistService tokenBlacklistService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // ================= TEST CONFIG =================
//
//    @TestConfiguration
//    static class TestConfig {
//
//        @Bean
//        public AuthService authService() {
//            return Mockito.mock(AuthService.class);
//        }
//
//        @Bean
//        public JwtService jwtService() {
//            return Mockito.mock(JwtService.class);
//        }
//
//        @Bean
//        public TokenBlacklistService tokenBlacklistService() {
//            return Mockito.mock(TokenBlacklistService.class);
//        }
//
//        // 🔥 CRITICAL FIXES (missing in your file)
//
//        @Bean
//        public CustomUserDetailsService customUserDetailsService() {
//            return Mockito.mock(CustomUserDetailsService.class);
//        }
//
//        @Bean
//        public JwtAuthenticationFilter jwtAuthenticationFilter() {
//            return Mockito.mock(JwtAuthenticationFilter.class);
//        }
//    }
//
//    // ---------------- REGISTER ----------------
//
//    @Test
//    void register_shouldReturnAuthResponse() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setEmail("test@mail.com");
//        request.setUsername("testuser");
//        request.setPassword("123");
//
//        AuthResponse response = AuthResponse.builder()
//                .token("token")
//                .username("testuser")
//                .build();
//
//        Mockito.when(authService.register(any())).thenReturn(response);
//
//        mockMvc.perform(post("/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value("token"));
//    }
//
//    // ---------------- LOGIN ----------------
//
//    @Test
//    void login_shouldReturnToken() throws Exception {
//        LoginRequest request = new LoginRequest();
//        request.setEmail("test@mail.com");
//        request.setPassword("123");
//
//        AuthResponse response = AuthResponse.builder()
//                .token("token")
//                .build();
//
//        Mockito.when(authService.login(any())).thenReturn(response);
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value("token"));
//    }
//
//    // ---------------- VALIDATE TOKEN ----------------
//
//    @Test
//    void validateToken_shouldReturnTrue() throws Exception {
//        Mockito.when(authService.validateToken("abc")).thenReturn(true);
//
//        mockMvc.perform(get("/auth/validate")
//                        .param("token", "abc"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.valid").value(true));
//    }
//
//    // ---------------- SEARCH USERS ----------------
//
//    @Test
//    void searchUsers_shouldReturnList() throws Exception {
//        UserSearchResponse user = UserSearchResponse.builder()
//                .username("john")
//                .build();
//
//        Mockito.when(authService.searchUsers("jo"))
//                .thenReturn(List.of(user));
//
//        mockMvc.perform(get("/auth/search")
//                        .param("keyword", "jo"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].username").value("john"));
//    }
//
//    // ---------------- GET USER BY ID ----------------
//
//    @Test
//    void getUserById_shouldReturnUser() throws Exception {
//        UUID userId = UUID.randomUUID();
//
//        UserSearchResponse user = UserSearchResponse.builder()
//                .userId(userId)
//                .username("john")
//                .build();
//
//        Mockito.when(authService.getUserById(userId)).thenReturn(user);
//
//        mockMvc.perform(get("/auth/user/" + userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.username").value("john"));
//    }
//
//    // ---------------- LOGOUT ----------------
//
//    @Test
//    void logout_shouldBlacklistToken() throws Exception {
//
//        Mockito.when(jwtService.getRemainingValidity(any()))
//                .thenReturn(1000L);
//
//        mockMvc.perform(post("/auth/logout")
//                        .header("Authorization", "Bearer token"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Logout successful"));
//
//        Mockito.verify(tokenBlacklistService)
//                .blacklistToken(eq("token"), anyLong());
//    }
//
//    // ---------------- CHANGE PASSWORD ----------------
//
//    @Test
//    void changePassword_shouldReturnSuccessMessage() throws Exception {
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("old");
//        request.setNewPassword("new");
//
//        mockMvc.perform(put("/auth/password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Password changed successfully"));
//    }
//
//    // ---------------- DELETE USER ----------------
//
//    @Test
//    void deleteUser_shouldReturnSuccess() throws Exception {
//        UUID userId = UUID.randomUUID();
//
//        mockMvc.perform(delete("/auth/super-admin/user/" + userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("User deleted successfully"));
//    }
//}