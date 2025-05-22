package com.soccersignup.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.service.GameSlotService;

@RestController
@RequestMapping("/api/gameslots")
public class GameSlotController {

    private final GameSlotService gameSlotService;

    public GameSlotController(GameSlotService gameSlotService) {
        this.gameSlotService = gameSlotService;
    }

    // ✅ Get current week's signup list
    @GetMapping
    public ResponseEntity<List<GameSlot>> getCurrentWeekSignups(
            @RequestParam(name = "date") String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return ResponseEntity.ok(gameSlotService.getSignupsForWeek(date));
    }

    // ✅ Add a signup
    @PostMapping
    public ResponseEntity<GameSlot> signup(@RequestBody GameSlot gameSlot) {
        GameSlot saved = gameSlotService.addSignup(gameSlot);
        return ResponseEntity.ok(saved);
    }

    // ✅ Remove a signup
    @DeleteMapping
    public ResponseEntity<Void> removeSignup(
            @RequestParam Player player,
            @RequestParam String date) {
        LocalDate gameDate = LocalDate.parse(date);
        gameSlotService.removeSignup(player, gameDate);
        return ResponseEntity.noContent().build();
    }
}
