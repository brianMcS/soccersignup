package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.GameSlotResponse;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.service.GameSlotService;

@RestController
@RequestMapping("/api/gameslots")
public class GameSlotController {

    private final GameSlotService gameSlotService;

    public GameSlotController(GameSlotService gameSlotService) {
        this.gameSlotService = gameSlotService;
    }

    // GET /api/gameslots/{gameId}
    @GetMapping("/{gameId}")
    public ResponseEntity<List<GameSlotResponse>> getSignups(
            @PathVariable Long gameId,
            Authentication authentication) {
        Player currentPlayer = (Player) authentication.getPrincipal();
        List<GameSlot> slots = gameSlotService.getSignupsForGame(gameId);
        List<GameSlotResponse> response = slots.stream()
                .map(slot -> GameSlotResponse.forViewer(slot, currentPlayer))
                .toList();
        return ResponseEntity.ok(response);
    }

    // POST /api/gameslots
    // Body: { "gameId": 1, "playerId": 3 }
    @PostMapping
    public ResponseEntity<GameSlotResponse> signup(
            @RequestBody SignupRequest request,
            Authentication authentication) {

        Player currentPlayer = (Player) authentication.getPrincipal();
        GameSlot saved = gameSlotService.addSignup(request.gameId(), currentPlayer.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GameSlotResponse.from(saved));
    }

    // DELETE /api/gameslots/{gameId}/players/{playerId}
    @DeleteMapping("/{gameId}/players/{playerId}")
    public ResponseEntity<Void> removeSignup(
            @PathVariable Long gameId,
            @PathVariable Long playerId,
            Authentication authentication) {
        Player currentPlayer = (Player) authentication.getPrincipal();

        boolean isAdmin = currentPlayer.hasRole(PlayerRole.ADMIN);
        boolean isOrganiser = currentPlayer.hasRole(PlayerRole.ORGANISER);
        boolean isOwnSignup = currentPlayer.getId().equals(playerId);

        if (!isAdmin && !isOrganiser && !isOwnSignup) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        gameSlotService.removeSignup(gameId, playerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{gameId}/players/{playerId}/pay")
    public ResponseEntity<GameSlotResponse> reportPayment(
            @PathVariable Long gameId,
            @PathVariable Long playerId,
            Authentication authentication) {
        Player currentPlayer = (Player) authentication.getPrincipal();
        if (!currentPlayer.getId().equals(playerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(GameSlotResponse.from(
                gameSlotService.reportPayment(gameId, playerId)));
    }

    @PatchMapping("/{gameId}/players/{playerId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<GameSlotResponse> confirmPayment(
            @PathVariable Long gameId,
            @PathVariable Long playerId,
            Authentication authentication) {
        Player currentPlayer = (Player) authentication.getPrincipal();
        return ResponseEntity.ok(GameSlotResponse.from(
                gameSlotService.confirmPayment(gameId, playerId, currentPlayer)));
    }

    @PatchMapping("/{gameId}/players/{playerId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<GameSlotResponse> rejectPayment(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(GameSlotResponse.from(
                gameSlotService.rejectPayment(gameId, playerId)));
    }

    record SignupRequest(Long gameId){}
}
