package com.example.demo.config;

import com.example.demo.entities.UserCredential;
import com.example.demo.repositories.UserCredentialRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;

    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public AuthDataInitializer(UserCredentialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.findByUsername("admin").isEmpty()) {
            UserCredential admin = new UserCredential(
                    ADMIN_ID,
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ADMIN"
            );
            repository.save(admin);
            System.out.println("CREATED DEFAULT ADMIN CREDENTIALS IN AUTH-DB");
        }
    }
}