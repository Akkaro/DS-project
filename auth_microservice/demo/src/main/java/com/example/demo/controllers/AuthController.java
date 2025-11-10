package main.java.com.example.demo.controllers;

import com.example.demo.dtos.AuthResponseDTO;
import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.dtos.TokenRefreshRequestDTO;
import com.example.demo.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest)
                .then(Mono.just(ResponseEntity.status(201).<Void>build()));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        return authService.refreshToken(refreshRequest.getRefreshToken())
                .map(ResponseEntity::ok);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        return authService.logout(refreshRequest.getRefreshToken())
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}