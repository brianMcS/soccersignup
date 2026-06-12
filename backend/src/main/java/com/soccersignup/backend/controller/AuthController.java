package com.soccersignup.backend.controller;

import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.dto.AuthResponse;
import com.soccersignup.backend.dto.LoginRequest;
import com.soccersignup.backend.dto.RegistrationRequest;
import com.soccersignup.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.soccersignup.backend.model.Player;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentPlayer(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Player player)) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        return ResponseEntity.ok(PlayerResponse.from(player));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return ResponseEntity.ok("Logged out successfully");
    }
}
