package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.GameSlotResponse;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<GameSlotResponse>> getSignups(@PathVariable Long gameId) {
        List<GameSlot> slots = gameSlotService.getSignupsForGame(gameId);
        List<GameSlotResponse> response = slots.stream()
                .map(slot -> new GameSlotResponse(
                        slot.getId(),
                        slot.getPlayer().getName(),
                        slot.getPlayer().getEmail(),
                        slot.getStatus(),
                        slot.getSignedUpAt()
                )).toList();
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
        boolean isOrganiser = currentPlayer.hasRole(PlayerRole.ORGANIZER);
        boolean isOwnSignup = currentPlayer.getId().equals(playerId);

        if(!isAdmin && !isOrganiser && isOwnSignup){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        gameSlotService.removeSignup(gameId, playerId);
        return ResponseEntity.noContent().build();
    }

    record SignupRequest(Long gameId){}
}
