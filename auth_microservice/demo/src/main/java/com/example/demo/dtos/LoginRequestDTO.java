package main.java.com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Auto-generates getters, setters, equals, hashCode, and toString
public class LoginRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}