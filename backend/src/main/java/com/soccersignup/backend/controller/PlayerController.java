package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.dto.UpdateRolesRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.soccersignup.backend.dto.PlayerRequest;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.ok(PlayerResponse.from(playerService.updatePlayer(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deactivatePlayer(@PathVariable Long id) {
        playerService.deactivatePlayer(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateRoles(@PathVariable Long id, @RequestBody UpdateRolesRequest request) {
        return ResponseEntity.ok(playerService.updateRoles(id, request.roles()));
    }
}
