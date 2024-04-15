package com.thbs.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.thbs.security.auth.AuthenticationResponse;
import com.thbs.security.auth.AuthenticationService;
import com.thbs.security.auth.RegisterRequest;
import com.thbs.security.config.JwtService;
import com.thbs.security.token.TokenRepository;
import com.thbs.security.user.Role;
import com.thbs.security.user.User;
import com.thbs.security.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister_Success() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .firstname("John")
            .lastname("Doe")
            .email("john.doe@example.com")
            .password("password")
            .role(Role.USER)
            .businessUnit("Test")
            .employeeId(12345)
            .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        // When
        AuthenticationResponse response = authenticationService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("jwtToken", response.getAccessToken());
    }

    @Test
    public void testRegister_UserAlreadyExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .firstname("John")
            .lastname("Doe")
            .email("john.doe@example.com")
            .password("password")
            .role(Role.USER)
            .businessUnit("Test")
            .employeeId(12345)
            .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(request));
    }
}
