package com.soccersignup.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.repository.GameRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(Game game) {
        // Optional: check if game for this date already exists
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
}
