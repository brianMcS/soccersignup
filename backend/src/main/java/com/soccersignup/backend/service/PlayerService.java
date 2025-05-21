package com.soccersignup.backend.service;

import java.util.List;
import java.util.Optional;

import com.soccersignup.backend.model.Player;


public interface PlayerService {
    List<Player> getAllPlayers();
    Optional<Player> getPlayerById(Long id);
    Player savePlayer(Player player);
    void deletePlayer(Long id);
}
