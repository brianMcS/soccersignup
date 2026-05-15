package com.soccersignup.backend.controller;

import com.soccersignup.backend.dto.PlayerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.soccersignup.backend.security.JwtTokenProvider;
import com.soccersignup.backend.model.Player;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentPlayer(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Player player)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        return ResponseEntity.ok(PlayerResponse.from(player));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return ResponseEntity.ok("Logged out successfully");
    }
}
