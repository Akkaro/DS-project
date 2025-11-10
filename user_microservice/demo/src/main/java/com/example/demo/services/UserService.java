package com.example.demo.services;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(userOptional.get());
    }

    public UUID insert(UserDetailsDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            LOGGER.error("Username {} already exists", userDTO.getUsername());
            throw new CustomException(
                    "Username already exists",
                    HttpStatus.CONFLICT,
                    User.class.getSimpleName(),
                    List.of("Username " + userDTO.getUsername() + " is already taken"));
        }

        User user = UserBuilder.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());
        return user.getId();
    }

    public void update(UUID id, UserDetailsDTO userDTO) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }

        User user = userOptional.get();

        if (!user.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsername(userDTO.getUsername())) {
            LOGGER.error("Username {} already exists", userDTO.getUsername());
            throw new CustomException(
                    "Username already exists",
                    HttpStatus.CONFLICT,
                    User.class.getSimpleName(),
                    List.of("Username " + userDTO.getUsername() + " is already taken"));
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(user);
        LOGGER.debug("User with id {} was updated in db", id);
    }

    public void delete(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        userRepository.deleteById(id);
        LOGGER.debug("User with id {} was deleted from db", id);
    }

    public List<UserDTO> findUsersByRole(Role role) {
        List<User> userList = userRepository.findByRole(role);
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with username {} was not found in db", username);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with username: " + username);
        }
        return UserBuilder.toUserDetailsDTO(userOptional.get());
    }
}