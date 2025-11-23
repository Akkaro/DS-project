package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.entities.RefreshToken;
import com.example.demo.entities.UserCredential;
import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.TokenRefreshException;
import com.example.demo.repositories.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserCredentialRepository userCredentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       RabbitTemplate rabbitTemplate) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Mono<Void> register(RegisterRequestDTO registerRequest) {
        return Mono.fromRunnable(() -> {
            // 1. Validation
            if (userCredentialRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                throw new CustomException("Username exists", HttpStatus.CONFLICT, "auth", List.of("Username already taken"));
            }

            // 2. Create ID locally
            UUID userId = UUID.randomUUID();

            // 3. Save Credentials + Role locally (Auth DB)
            UserCredential credential = new UserCredential(
                    userId,
                    registerRequest.getUsername(),
                    passwordEncoder.encode(registerRequest.getPassword()),
                    registerRequest.getRole() // Storing role locally for decoupled login
            );
            userCredentialRepository.save(credential);

            // 4. Publish "create_user" Event to RabbitMQ
            // Consumed by User-Service (to create profile) and Device-Service
            Map<String, Object> syncMsg = new HashMap<>();
            syncMsg.put("action", "create_user");
            syncMsg.put("userId", userId.toString());
            syncMsg.put("username", registerRequest.getUsername());
            syncMsg.put("name", registerRequest.getName());
            syncMsg.put("email", registerRequest.getEmail());
            syncMsg.put("role", registerRequest.getRole());

            try {
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "", syncMsg);
                log.info("Published create_user event for user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to publish sync message", e);
            }
        });
    }

    public Mono<AuthResponseDTO> login(LoginRequestDTO loginRequest) {
        // 1. Local Credential Check
        return Mono.justOrEmpty(userCredentialRepository.findByUsername(loginRequest.getUsername()))
                .switchIfEmpty(Mono.error(new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED, "login", List.of("User not found"))))
                .flatMap(creds -> {
                    // 2. Validate Password
                    if (!passwordEncoder.matches(loginRequest.getPassword(), creds.getPassword())) {
                        return Mono.error(new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED, "login", List.of("Invalid password")));
                    }

                    // 3. Generate Token using LOCAL role (No call to User Service)
                    String accessToken = jwtService.generateToken(creds.getUsername(), creds.getId(), creds.getRole());
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(creds.getId(), creds.getUsername());
                    
                    return Mono.just(new AuthResponseDTO(accessToken, refreshToken.getToken()));
                });
    }

    public Mono<AuthResponseDTO> refreshToken(String requestRefreshToken) {
        return Mono.fromCallable(() -> {
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));

            refreshTokenService.verifyExpiration(refreshToken);

            // Retrieve the user credentials to get the role again
            UserCredential creds = userCredentialRepository.findById(refreshToken.getUserId())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND, "auth", List.of("User for token does not exist")));

            String accessToken = jwtService.generateToken(
                    creds.getUsername(),
                    creds.getId(),
                    creds.getRole()
            );

            return new AuthResponseDTO(accessToken, requestRefreshToken);
        });
    }

    public Mono<Void> logout(String requestRefreshToken) {
        return Mono.fromRunnable(() -> {
            refreshTokenService.findByToken(requestRefreshToken).ifPresent(refreshToken -> {
                refreshTokenService.deleteByUserId(refreshToken.getUserId());
            });
        });
    }
}