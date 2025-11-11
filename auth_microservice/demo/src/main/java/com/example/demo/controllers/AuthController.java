package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.dtos.TokenRefreshRequestDTO;
import com.example.demo.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, token refresh, and logout.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user",
               description = "Registers a new user. This is proxied to the user-service, which requires an ADMIN token.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Forbidden (if the auth-service's call to user-service is rejected)")
    @ApiResponse(responseCode = "409", description = "Username already exists")
    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest)
                .then(Mono.just(ResponseEntity.status(201).<Void>build()));
    }

    @Operation(summary = "Log in to get tokens", description = "Authenticates a user and returns a JWT access token and a refresh token.")
    @ApiResponse(responseCode = "200", description = "Successful login")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Refresh an access token", description = "Uses a valid refresh token to issue a new access token.")
    @ApiResponse(responseCode = "200", description = "Token refreshed")
    @ApiResponse(responseCode = "403", description = "Invalid or expired refresh token")
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        return authService.refreshToken(refreshRequest.getRefreshToken())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Log out", description = "Invalidates the user's refresh token.")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        return authService.logout(refreshRequest.getRefreshToken())
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}