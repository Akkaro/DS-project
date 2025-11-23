package com.example.demo.config;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    
    // MUST MATCH THE UUID IN AUTH SERVICE
    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public UserDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                ADMIN_ID,
                "admin",
                "System Admin",
                "admin@ems.com",
                Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println("CREATED DEFAULT ADMIN PROFILE IN USER-DB");
        }
    }
}