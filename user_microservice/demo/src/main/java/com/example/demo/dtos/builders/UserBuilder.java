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
        return new UserDetailsDTO(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        return new User(
                userDetailsDTO.getUsername(),
                userDetailsDTO.getPassword(),
                userDetailsDTO.getEmail(),
                userDetailsDTO.getName(),
                userDetailsDTO.getRole()
        );
    }
}