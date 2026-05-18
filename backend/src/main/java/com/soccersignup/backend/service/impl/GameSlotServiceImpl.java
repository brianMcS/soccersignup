package com.soccersignup.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.GameSlotService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class GameSlotServiceImpl implements GameSlotService {

    private final GameSlotRepository gameSlotRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    public GameSlotServiceImpl(
            GameSlotRepository gameSlotRepository,
            PlayerRepository playerRepository,
            GameRepository gameRepository) {
        this.gameSlotRepository = gameSlotRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<GameSlot> getSignupsForGame(Long gameId) {
        Game game = findGameById(gameId);
        return gameSlotRepository.findByGame(game);
    }

    @Override
    @Transactional
    public GameSlot addSignup(Long gameId, Long playerId) {
        Game game = findWithLockingById(gameId);
        Player player = findPlayerById(playerId);
        validatePlayerNotAlreadySignedUp(game, player);
        SlotStatus status = determineSlotStatus(game);
        GameSlot slot = createGameSlot(game, player, status);

        return gameSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public void removeSignup(Long gameId, Long playerId) {
        Game game = findGameById(gameId);
        Player player = findPlayerById(playerId);
        GameSlot removedSlot = findSignup(game, player);
        gameSlotRepository.delete(removedSlot);

        if (removedSlot.getStatus() == SlotStatus.CONFIRMED){
             promoteNextWaitlistedPlayer(game);
        }
    }


    private Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }

    private Game findWithLockingById(Long gameId){
        return gameRepository.findWithLockingById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }

    private Player findPlayerById(Long playerId){
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    private GameSlot findSignup(Game game, Player player){
        return gameSlotRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new IllegalArgumentException("Signup not found for this player/game"));
    }

    private void validatePlayerNotAlreadySignedUp(Game game, Player player){
        boolean exists = gameSlotRepository.existsByGameAndPlayer(game, player);
        if (exists) {
            throw new IllegalStateException("Player already signed up for this game.");
        }
    }

    private SlotStatus determineSlotStatus(Game game){
        long confirmedCount = gameSlotRepository.countByGameAndStatus(game, SlotStatus.CONFIRMED);
        return game.determineSignupStatus(confirmedCount);
    }

    private GameSlot createGameSlot(Game game, Player player, SlotStatus status) {
        return GameSlot.create(game, player, status, LocalDateTime.now());
    }

    private void promoteNextWaitlistedPlayer(Game game){
            gameSlotRepository
                    .findFirstByGameAndStatusOrderBySignedUpAtAsc(game, SlotStatus.WAITLISTED)
                    .ifPresent(slot ->{
                        slot.setStatus(SlotStatus.CONFIRMED);
                        gameSlotRepository.save(slot);
                    });
    }
}