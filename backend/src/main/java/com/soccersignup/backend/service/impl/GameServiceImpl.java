package com.soccersignup.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.service.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.repository.GameRepository;

@Service
public class GameServiceImpl  implements GameService {

    private final GameRepository gameRepository;
    private final GameSlotRepository gameSlotRepository;

    public GameServiceImpl(
            GameRepository gameRepository,
            GameSlotRepository gameSlotRepository) {
        this.gameRepository = gameRepository;
        this.gameSlotRepository = gameSlotRepository;
    }

    @Override
    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getGamesByStatus(GameStatus status){
        return gameRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Game getGameById(Long id){
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));
    }

    @Override
    @Transactional
    public Game updateGame(Long id, GameRequest request){
        Game existing = getGameById(id);
        long confirmedCount = gameSlotRepository.countByGameAndStatus(
                existing, SlotStatus.CONFIRMED);
        if (request.maxPlayers() < confirmedCount) {
            throw new IllegalStateException(
                    "Maximum players cannot be lower than the confirmed player count of "
                            + confirmedCount);
        }
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

    private BigDecimal defaultFee(BigDecimal feeAmount) {
        return feeAmount != null ? feeAmount : new BigDecimal("5.00");
    }

    private String normalizeLink(String revolutLink) {
        return revolutLink == null || revolutLink.isBlank() ? null : revolutLink.trim();
    }
}
