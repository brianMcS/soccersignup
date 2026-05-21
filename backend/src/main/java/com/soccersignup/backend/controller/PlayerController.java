package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.dto.UpdateRolesRequest;
import com.soccersignup.backend.model.PlayerRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    //@PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest request) {
        Player player = new Player(request.name(), request.email(), request.phone());
        Player saved = playerService.savePlayer(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlayerResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request,
            Authentication authentication) {

        Player currentPlayer = (Player) authentication.getPrincipal();
        boolean isAdmin = currentPlayer.hasRole(PlayerRole.ADMIN);
        boolean isOwnPlayer = currentPlayer.getId().equals(id);

        if(!isAdmin && !isOwnPlayer) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
