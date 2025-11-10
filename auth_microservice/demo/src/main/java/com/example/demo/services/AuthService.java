package com.example.demo.services;

import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.dtos.UserResponseDTO;
import com.example.demo.entities.RefreshToken;
import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.TokenRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserServiceClient userServiceClient,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userServiceClient = userServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Registers a new user by calling the user-service.
     */
    public Mono<Void> register(RegisterRequestDTO registerRequest) {
        // We just pass the request to the user-service client
        return userServiceClient.registerUser(registerRequest);
    }

    /**
     * Logs in a user.
     * 1. Finds the user by username (via user-service).
     * 2. Validates the password.
     * 3. Generates an access token and a refresh token.
     */
    public Mono<AuthResponseDTO> login(LoginRequestDTO loginRequest) {
        log.debug("Attempting login for user: {}", loginRequest.getUsername());
        
        return userServiceClient.findUserByUsername(loginRequest.getUsername())
                .flatMap(user -> {
                    log.debug("Found user: {}", user.getUsername());
                    
                    // 2. Validate password
                    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        log.warn("Invalid password for user: {}", loginRequest.getUsername());
                        return Mono.error(new CustomException(
                                "Invalid credentials", HttpStatus.UNAUTHORIZED, "login", List.of("Invalid username or password")
                        ));
                    }
                    
                    log.debug("Password validated for user: {}", user.getUsername());
                    
                    // 3. Generate tokens
                    String accessToken = jwtService.generateToken(user.getUsername(), user.getId(), user.getRole());
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getUsername());
                    
                    return Mono.just(new AuthResponseDTO(accessToken, refreshToken.getToken()));
                });
    }

    /**
     * Creates a new access token using a valid refresh token.
     */
    public Mono<AuthResponseDTO> refreshToken(String requestRefreshToken) {
        return Mono.fromCallable(() -> {
            // Find and verify the refresh token in the database
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in DB"));

            refreshTokenService.verifyExpiration(refreshToken);

            // If valid, generate a new access token
            String accessToken = jwtService.generateToken(
                    refreshToken.getUsername(),
                    refreshToken.getUserId(),
                    "ROLE_UNKNOWN" // We lose the role info, this is a tradeoff. Or we could re-fetch user.
            );

            return new AuthResponseDTO(accessToken, requestRefreshToken);
        });
    }

    /**
     * Logs out a user by deleting their refresh token.
     */
    public Mono<Void> logout(String requestRefreshToken) {
        return Mono.fromRunnable(() -> {
            refreshTokenService.findByToken(requestRefreshToken).ifPresent(refreshToken -> {
                refreshTokenService.deleteByUserId(refreshToken.getUserId());
            });
        });
    }
}