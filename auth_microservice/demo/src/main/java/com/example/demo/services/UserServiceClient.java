package com.example.demo.services;

import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.dtos.UserResponseDTO;
import com.example.demo.handlers.exceptions.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This service is responsible for all communication
 * with the external user-microservice.
 */
@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private final WebClient webClient;

    @Value("${app.user-service.url}")
    private String userServiceUrl;

    public UserServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls the user-service to find a user by their username.
     * We need this to get the user's ID, role, and hashed password for login.
     */
    public Mono<UserResponseDTO> findUserByUsername(String username) {
        String url = userServiceUrl + "/users/username/" + username;
        log.debug("Calling user-service: GET {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> Mono.error(new CustomException(
                                "User not found", HttpStatus.NOT_FOUND, "user", List.of("Username " + username + " not found"))
                        )
                )
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            log.error("Error from user-service: {} - {}", clientResponse.statusCode(), body);
                            return Mono.error(new CustomException(
                                    "Error finding user", HttpStatus.INTERNAL_SERVER_ERROR, "user-service", List.of(body)));
                        })
                )
                .bodyToMono(UserResponseDTO.class) // <-- This was 'Void.class' before, which was wrong.
                .doOnError(e -> log.error("findUserByUsername failed", e));
    }

    /**
     * Calls the user-service to create a new user.
     */
    public Mono<Void> registerUser(RegisterRequestDTO registerRequest) {
        String url = userServiceUrl + "/users";
        log.debug("Calling user-service: POST {}", url);

        return webClient.post()
                .uri(url)
                .bodyValue(registerRequest)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals,
                        clientResponse -> Mono.error(new CustomException(
                                "Username already exists", HttpStatus.CONFLICT, "user", List.of("Username " + registerRequest.getUsername() + " is already taken"))
                        )
                )
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            log.error("Error from user-service: {} - {}", clientResponse.statusCode(), body);
                            return Mono.error(new CustomException(
                                    "User registration failed", HttpStatus.INTERNAL_SERVER_ERROR, "user-service", List.of(body)));
                        })
                )
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("registerUser failed", e));
    }
}