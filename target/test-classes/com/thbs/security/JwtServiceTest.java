package com.thbs.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thbs.security.config.JwtService;
import com.thbs.security.user.Role;
import com.thbs.security.user.User;
import com.thbs.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .employeeId(12345)
                .businessUnit("IT")
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Test
    public void testGenerateToken() {
        // When
        String token = jwtService.generateToken(user);

        // Then
        assertNotNull(token);
        assertTrue(token.startsWith("eyJhbGciOiJIUzI1NiJ9")); // JWT token starts with this prefix
    }

    @Test
    public void testIsTokenValid_ValidToken() {
        // Given
        String token = jwtService.generateToken(user);

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertTrue(isValid);
    }

    @Test
    public void testIsTokenValid_ExpiredToken() throws InterruptedException {
        // Given
        String token = jwtService.generateToken(user);

        // Wait for token to expire
        Thread.sleep(1000); // Adjust the sleep time to match your token expiration time

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertFalse(isValid);
    }

    @Test
    public void testIsTokenValid_InvalidToken() {
        // Given
        String token = "invalidToken";

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertFalse(isValid);
    }
}
