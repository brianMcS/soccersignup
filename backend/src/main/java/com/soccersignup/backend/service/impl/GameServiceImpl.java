package com.soccersignup.backend.service.impl;

import java.util.List;

import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.service.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.repository.GameRepository;

@Service
public class GameServiceImpl  implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Override
    public List<Game> getGamesByStatus(GameStatus status){
        return gameRepository.findByStatus(status);
    }

    @Override
    public Game getGameById(Long id){
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));
    }

    @Override
    @Transactional
    public Game updateGame(Long id, GameRequest request){
        Game existing = getGameById(id);
        existing.setGameDate(request.gameDate());
        existing.setKickOffTime(request.kickOffTime());
        existing.setLocation(request.location());
        existing.setMaxPlayers(request.maxPlayers());
        return gameRepository.save(existing);
    }

    @Override
    @Transactional
    public Game closeSignups(Long id) {
        Game game = getGameById(id);
        if (game.getStatus() != GameStatus.OPEN) {
            throw new IllegalStateException("Only OPEN games can be closed");
        }
        game.setStatus(GameStatus.CLOSED);
        return gameRepository.save(game);
    }
}
