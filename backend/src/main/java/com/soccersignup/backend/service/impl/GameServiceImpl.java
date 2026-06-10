package com.soccersignup.backend.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
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
    @Transactional
    public List<Game> getAllGames() {
        return completePastGames(gameRepository.findAll());
    }

    @Override
    @Transactional
    public List<Game> getGamesByStatus(GameStatus status){
        completePastGames(gameRepository.findAll());
        return gameRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Game getGameById(Long id){
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));
        completePastGame(game);
        return game;
    }

    @Override
    @Transactional
    public Game updateGame(Long id, GameRequest request){
        Game existing = getGameById(id);
        existing.setGameDate(request.gameDate());
        existing.setKickOffTime(request.kickOffTime());
        existing.setLocation(request.location());
        existing.setMaxPlayers(request.maxPlayers());
        existing.setFeeAmount(defaultFee(request.feeAmount()));
        existing.setRevolutLink(normalizeLink(request.revolutLink()));
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

    private List<Game> completePastGames(List<Game> games) {
        boolean changed = games.stream()
                .map(this::completePastGame)
                .reduce(false, Boolean::logicalOr);
        if (changed) {
            gameRepository.saveAll(games);
        }
        return games;
    }

    private boolean completePastGame(Game game) {
        if ((game.getStatus() != GameStatus.OPEN && game.getStatus() != GameStatus.CLOSED)
                || game.getGameDate() == null) {
            return false;
        }

        LocalTime kickOff = game.getKickOffTime() != null ? game.getKickOffTime() : LocalTime.MAX;
        LocalDateTime startsAt = LocalDateTime.of(game.getGameDate(), kickOff);
        if (!startsAt.isBefore(LocalDateTime.now())) {
            return false;
        }

        game.setStatus(GameStatus.COMPLETED);
        return true;
    }

    private BigDecimal defaultFee(BigDecimal feeAmount) {
        return feeAmount != null ? feeAmount : new BigDecimal("5.00");
    }

    private String normalizeLink(String revolutLink) {
        return revolutLink == null || revolutLink.isBlank() ? null : revolutLink.trim();
    }
}
