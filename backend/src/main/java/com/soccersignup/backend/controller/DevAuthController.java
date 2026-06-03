package com.soccersignup.backend.controller;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.dto.DevLoginRequest;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile({"dev", "local"})
public class DevAuthController {

    private final PlayerRepository playerRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public DevAuthController(PlayerRepository playerRepository, JwtTokenProvider jwtTokenProvider ) {
        this.playerRepository = playerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DevLoginRequest request){
        Player player = playerRepository
                .findByEmail(request.email())
                .orElseThrow();

        String token = jwtTokenProvider.generateToken(player);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "token", token,
                        "player", Map.of(
                                "id", player.getId(),
                                "email", player.getEmail(),
                                "name", player.getName(),
                                "roles", player.getRoles()
                                        .stream()
                                        .map(Enum::toString)
                                        .toList()
                        )
                )
        );
    }

    @GetMapping("/players")
    public ResponseEntity<?> getPlayers() {

        return ResponseEntity.ok(
                playerRepository.findAll()
                        .stream()
                        .map(player -> Map.of(
                                "id", player.getId(),
                                "email", player.getEmail(),
                                "name", player.getName()
                        ))
                        .toList()
        );
    }
}
