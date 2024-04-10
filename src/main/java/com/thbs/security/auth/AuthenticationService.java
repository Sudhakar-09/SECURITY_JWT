package com.thbs.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thbs.security.config.JwtService;
import com.thbs.security.token.Token;
import com.thbs.security.token.TokenRepository;
import com.thbs.security.token.TokenType;
import com.thbs.security.user.User;
import com.thbs.security.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
    // Create a new user entity based on the registration request
    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .build();
    // Save the user to the repository
    var savedUser = repository.save(user);
    // Generate JWT token and refresh token for the user
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    // Save the user's token in the repository
    saveUserToken(savedUser, jwtToken);
    // Return the authentication response containing the tokens
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
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
    // Generate JWT token and refresh token for the user
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    // Revoke all existing user tokens
    revokeAllUserTokens(user);
    // Save the user's new token in the repository
    saveUserToken(user, jwtToken);
    // Return the authentication response containing the tokens
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
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

  // Method to revoke all existing user tokens
  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  // Method to handle token refreshing
  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        // Prepare authentication response containing new access token and refresh token
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        // Write the response to the output stream
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
