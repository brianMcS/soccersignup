package com.soccersignup.backend.service.impl;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.soccersignup.backend.dto.PlayerRequest;
import com.soccersignup.backend.dto.PlayerResponse;
import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.OAuthProvider;
import com.soccersignup.backend.model.PlayerRole;
import org.springframework.stereotype.Service;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.PlayerService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public void deactivatePlayer(Long id){
        Optional<Player> playerOpt = playerRepository.findById(id);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setIsActive(false); // Mark as inactive instead of deleting
            playerRepository.save(player);
        }
    }

    @Override
    public Player createPlayer(PlayerRequest request) {
        Player player = new Player();
        player.setName(request.name());
        player.setEmail(request.email());
        player.setPhone(request.phone());
        player.setIsActive(true); // New players are active by default
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + id));
        player.setName(request.name());
        player.setEmail(request.email());
        player.setPhone(request.phone());
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player findOrCreateOAuthPlayer(String email, String name, OAuthProvider provider, String oauthProviderId) {
        return playerRepository.findByEmail(email)
                .orElseGet(() -> {
                    Player newPlayer = new Player(name, email, null, provider, oauthProviderId);
                    newPlayer.addRole(PlayerRole.PLAYER);
                    return playerRepository.save(newPlayer);
                });
    }

    @Override
    public PlayerResponse updateRoles(Long id, Set<PlayerRole> roles){
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + id));
        Set<PlayerRole> updatedRoles = new HashSet<>(roles);
        updatedRoles.add(PlayerRole.PLAYER);
        player.setRoles(updatedRoles);
        return PlayerResponse.from(playerRepository.save(player));
    }
}
