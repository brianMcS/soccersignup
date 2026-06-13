package com.soccersignup.backend.service;

import java.util.List;
import java.util.Set;

import com.soccersignup.backend.dto.PlayerRequest;
import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;


public interface PlayerService {
    List<Player> getAllPlayers();
    void deactivatePlayer(Long id);
    Player updatePlayer(Long id, PlayerRequest request);
    Player findOrCreateOAuthPlayer(String email, String name, OAuthProvider provider, String oauthProviderId);
    PlayerResponse updateRoles(Long id, Set<PlayerRole> roles);
}
