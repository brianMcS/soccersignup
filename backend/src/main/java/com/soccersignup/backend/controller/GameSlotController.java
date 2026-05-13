package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.GameSlotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<GameSlotResponse> signup(@RequestBody SignupRequest request) {
        GameSlot saved = gameSlotService.addSignup(request.gameId(), request.playerId());
        GameSlotResponse response = new GameSlotResponse(
                saved.getId(),
                saved.getPlayer().getName(),
                saved.getPlayer().getEmail(),
                saved.getStatus(),
                saved.getSignedUpAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/gameslots/{gameId}/players/{playerId}
    @DeleteMapping("/{gameId}/players/{playerId}")
    public ResponseEntity<Void> removeSignup(@PathVariable Long gameId, @PathVariable Long playerId) {
        gameSlotService.removeSignup(gameId, playerId);
        return ResponseEntity.noContent().build();
    }

    record SignupRequest(Long gameId, Long playerId){}
}
