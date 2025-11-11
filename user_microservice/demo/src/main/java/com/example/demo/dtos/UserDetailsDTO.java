package com.example.demo.dtos;

import com.example.demo.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.UUID;

public class UserDetailsDTO {

    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, message = "Username must be at least 3 characters")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
    private Role role;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(String username, String password, String email, String name, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public UserDetailsDTO(UUID id, String username, String password, String email, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsDTO that = (UserDetailsDTO) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(name, that.name) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, name, role);
    }
}