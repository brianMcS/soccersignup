package com.soccersignup.backend.service.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.PlayerService;

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
}
