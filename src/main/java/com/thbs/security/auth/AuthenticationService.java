package com.thbs.security.auth;

import com.thbs.security.config.JwtService;
import com.thbs.security.token.Token;
import com.thbs.security.token.TokenRepository;
import com.thbs.security.token.TokenType;
import com.thbs.security.user.Role;
import com.thbs.security.user.User;
import com.thbs.security.user.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Method to handle user registration
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if a user with the given email already exists
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with the given email already exists");
        }

        // Create a new user entity based on the registration request
        Role role = request.getRole() != null ? request.getRole() : Role.USER;

        var user = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .businessUnit(request.getBusinessUnit())
            .employeeId(request.getEmployeeId())
            .build();

        // Save the user to the repository
        var savedUser = repository.save(user);

        // Generate JWT token for the user
        var jwtToken = jwtService.generateToken(user);

        // Save the user's token in the repository
        saveUserToken(savedUser, jwtToken);

        // Return the authentication response containing the token
        return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .build();
    }

    // Method to handle user authentication
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate user using Spring Security's authentication manager
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Retrieve user details from the repository
        var user = repository.findByEmail(request.getEmail())
            .orElseThrow();

        // Generate JWT token for the user
        var jwtToken = jwtService.generateToken(user);

        // Revoke all existing user tokens
        revokeAllUserTokens(user);

        // Save the user's new token in the repository
        saveUserToken(user, jwtToken);

        // Return the authentication response containing the token
        return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .build();
    }

    // Method to save user token in the repository
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
        tokenRepository.save(token);
    }

        // Scheduled task to delete expired or revoked tokens
@Scheduled(fixedRate = 3600000) // Run every hour
public void deleteExpiredOrRevokedTokens() {
    List<Token> tokensToDelete = tokenRepository.findAll()
        .stream()
        .filter(token -> token.isExpired() || token.isRevoked())
        .collect(Collectors.toList());

    tokenRepository.deleteAll(tokensToDelete);
}

    // Method to revoke all existing user tokens
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
