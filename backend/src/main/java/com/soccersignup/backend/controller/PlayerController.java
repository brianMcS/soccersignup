package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.PlayerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.soccersignup.backend.dto.PlayerRequest;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.PlayerService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<PlayerResponse> getAllPlayers() {
        return playerService.getAllPlayers()
                .stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id)
                .map(PlayerResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest request) {
        Player player = new Player(request.name(), request.email(), request.phone());
        Player saved = playerService.savePlayer(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlayerResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
        return playerService.getPlayerById(id)
                .map(existingPlayer -> {
                    existingPlayer.setName(request.name());
                    existingPlayer.setEmail(request.email());
                    existingPlayer.setPhone(request.phone());
                    Player updatedPlayer = playerService.savePlayer(existingPlayer);
                    return ResponseEntity.ok(updatedPlayer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
