package com.soccersignup.backend.service;

import java.util.List;
import java.util.Optional;

import com.soccersignup.backend.dto.PlayerRequest;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.Player;


public interface PlayerService {
    List<Player> getAllPlayers();
    Optional<Player> getPlayerById(Long id);
    Player savePlayer(Player player);
    void deletePlayer(Long id);
    void deactivatePlayer(Long id);
    Player createPlayer(PlayerRequest request);
    Player updatePlayer(Long id, PlayerRequest request);
    Player findOrCreateOAuthPlayer(String email, String name, OAuthProvider provider, String oauthProviderId);
}
