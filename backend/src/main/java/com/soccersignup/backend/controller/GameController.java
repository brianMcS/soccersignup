package com.soccersignup.backend.controller;

import java.util.List;

import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.dto.GameResponse;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.soccersignup.backend.model.Game;

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
    public ResponseEntity<List<GameResponse>> getAllGames(@RequestParam(required = false) GameStatus status) {
        List<Game> games = (status != null)
                ? gameService.getGamesByStatus(status)
                : gameService.getAllGames();
        return ResponseEntity.ok(games.stream().map(GameResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGameById(@RequestParam Long id) {
        return ResponseEntity.ok(GameResponse.from(gameService.getGameById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponse> updateGame(@PathVariable Long id, @Valid @RequestBody GameRequest request) {
        Game updates = new Game();
        updates.setGameDate(request.gameDate());
        updates.setKickOffTime(request.kickOffTime());
        updates.setLocation(request.location());
        updates.setMaxPlayers(request.maxPlayers());
        return ResponseEntity.ok(GameResponse.from(gameService.updateGame(id, updates)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<GameResponse> closeSignups(@PathVariable Long id) {
        return ResponseEntity.ok(GameResponse.from(gameService.closeSignups(id)));
    }
}
