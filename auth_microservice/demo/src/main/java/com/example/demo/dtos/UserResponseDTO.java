package com.example.demo.dtos;

import lombok.Data;

import java.util.UUID;

/**
 * A simple DTO to represent the user data we expect
 * to get back from the user-service.
 * This prevents a hard dependency on the user-service's classes.
 */
@Data
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String password;
    private String name;
    private String role;
}