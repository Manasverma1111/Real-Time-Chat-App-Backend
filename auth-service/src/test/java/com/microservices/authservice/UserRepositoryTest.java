package com.microservices.authservice;

import com.microservices.authservice.entity.AuthProvider;
import com.microservices.authservice.entity.GlobalRole;
import com.microservices.authservice.entity.User;
import com.microservices.authservice.entity.UserStatus;
import com.microservices.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // ---------------- HELPER ----------------

    private User createUser(String email, String username) {
        return User.builder()
                .email(email)
                .username(username)
                .fullName(username)           // ✅ FIX: full_name is NOT NULL in schema
                .passwordHash("password")
                .provider(AuthProvider.LOCAL)
                .status(UserStatus.OFFLINE)
                .isActive(true)
                .role(GlobalRole.USER)
                .build();
    }

    // ---------------- EXISTS BY EMAIL ----------------

    @Test
    void existsByEmail_shouldReturnTrue_whenUserExists() {
        userRepository.save(createUser("test@mail.com", "testuser"));
        assertTrue(userRepository.existsByEmail("test@mail.com"));
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserNotExists() {
        assertFalse(userRepository.existsByEmail("no@mail.com"));
    }

    // ---------------- EXISTS BY USERNAME ----------------

    @Test
    void existsByUsername_shouldReturnTrue() {
        userRepository.save(createUser("a@mail.com", "john"));
        assertTrue(userRepository.existsByUsername("john"));
    }

    @Test
    void existsByUsername_shouldReturnFalse() {
        assertFalse(userRepository.existsByUsername("unknown"));
    }

    // ---------------- FIND BY EMAIL ----------------

    @Test
    void findByEmail_shouldReturnUser() {
        userRepository.save(createUser("find@mail.com", "finduser"));

        Optional<User> result = userRepository.findByEmail("find@mail.com");

        assertTrue(result.isPresent());
        assertEquals("finduser", result.get().getUsername());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        assertTrue(userRepository.findByEmail("none@mail.com").isEmpty());
    }

    // ---------------- FIND BY USERNAME ----------------

    @Test
    void findByUsername_shouldReturnUser() {
        userRepository.save(createUser("user@mail.com", "john123"));

        Optional<User> result = userRepository.findByUsername("john123");

        assertTrue(result.isPresent());
        assertEquals("john123", result.get().getUsername());
    }

    // ---------------- FIND BY USER ID ----------------

    @Test
    void findByUserId_shouldReturnUser() {
        User saved = userRepository.save(createUser("id@mail.com", "iduser"));

        Optional<User> result = userRepository.findByUserId(saved.getUserId());

        assertTrue(result.isPresent());
        assertEquals("iduser", result.get().getUsername());
    }

    // ---------------- FIND BY STATUS ----------------

    @Test
    void findByStatus_shouldReturnMatchingUsers() {
        User u1 = createUser("a@mail.com", "user1");
        u1.setStatus(UserStatus.ONLINE);

        User u2 = createUser("b@mail.com", "user2");
        u2.setStatus(UserStatus.OFFLINE);

        userRepository.saveAll(List.of(u1, u2));

        List<User> onlineUsers = userRepository.findByStatus(UserStatus.ONLINE);

        assertEquals(1, onlineUsers.size());
        assertEquals("user1", onlineUsers.get(0).getUsername());
    }

    // ---------------- SEARCH USERS ----------------

    @Test
    void findByUsernameContainingIgnoreCase_shouldReturnMatches() {
        userRepository.save(createUser("a@mail.com", "john"));
        userRepository.save(createUser("b@mail.com", "johnny"));
        userRepository.save(createUser("c@mail.com", "alex"));

        List<User> result =
                userRepository.findByUsernameContainingIgnoreCase("john");

        assertEquals(2, result.size());
    }

    // ---------------- DELETE BY USER ID ----------------

    @Test
    void deleteByUserId_shouldDeleteUser() {
        User saved = userRepository.save(createUser("delete@mail.com", "deleteUser"));

        userRepository.deleteByUserId(saved.getUserId());

        Optional<User> result = userRepository.findByUserId(saved.getUserId());

        assertTrue(result.isEmpty());
    }
}