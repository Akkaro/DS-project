package com.example.demo.controllers;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.entities.Role;
import com.example.demo.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "User Management", description = "Endpoints for managing user accounts (Admin Only).")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "List of all users")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.findUsers());
    }

    @Operation(summary = "Get users by role", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "List of users matching the role")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.findUsersByRole(role));
    }

    @Operation(summary = "Create a new user", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "409", description = "Username already exists")
    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserDetailsDTO userDTO) {
        UUID id = userService.insert(userDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Get user by ID", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable UUID id) {
        UserDetailsDTO user = userService.findUserById(id);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update a user", description = "Requires ADMIN role. Omit 'password' to keep it unchanged.")
    @ApiResponse(responseCode = "204", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDetailsDTO userDTO) {
        userService.update(id, userDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a user", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by username", description = "Public endpoint for the auth-service to find user details during login.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth", scopes = {})
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDetailsDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findUserByUsername(username));
    }
}