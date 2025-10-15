package com.example.demo.repositories;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find users by role
     */
    List<User> findByRole(Role role);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
}