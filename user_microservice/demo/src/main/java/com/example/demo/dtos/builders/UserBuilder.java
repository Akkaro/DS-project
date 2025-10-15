package com.example.demo.dtos.builders;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.entities.User;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        // Note: We don't include the actual password in the DTO for security
        return new UserDetailsDTO(
                user.getId(),
                user.getUsername(),
                null, // Password is not returned
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        return new User(
                userDetailsDTO.getUsername(),
                userDetailsDTO.getPassword(), // This will be encoded in the service layer
                userDetailsDTO.getEmail(),
                userDetailsDTO.getName(),
                userDetailsDTO.getRole()
        );
    }
}