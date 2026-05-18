package com.soccersignup.backend.service;

import com.soccersignup.backend.model.Game;

import java.util.List;

public interface GameService {
    Game createGame(Game game);
    List<Game> getAllGames();
}
