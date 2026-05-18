package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.dto.GameResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.service.GameService;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameRequest request) {
        Game game = new Game();
        game.setGameDate(request.gameDate());
        game.setKickOffTime(request.kickOffTime());
        game.setLocation(request.location());
        game.setMaxPlayers(request.maxPlayers());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GameResponse.from(gameService.createGame(game)));
    }

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }
}
