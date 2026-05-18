package com.soccersignup.backend.service;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;

import java.util.List;

public interface GameService {
    Game createGame(Game game);
    List<Game> getAllGames();
    List<Game> getGamesByStatus(GameStatus status);
    Game getGameById(Long id);
    Game updateGame(Long id, Game updates);
    Game closeSignups(Long id);
}
