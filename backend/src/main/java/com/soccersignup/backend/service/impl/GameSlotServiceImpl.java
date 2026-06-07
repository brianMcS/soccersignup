package com.soccersignup.backend.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.GameSlotService;
import com.soccersignup.backend.service.TeamSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class GameSlotServiceImpl implements GameSlotService {

    private final GameSlotRepository gameSlotRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final TeamSheetService teamSheetService;

    public GameSlotServiceImpl(
            GameSlotRepository gameSlotRepository,
            PlayerRepository playerRepository,
            GameRepository gameRepository,
            TeamSheetService teamSheetService) {
        this.gameSlotRepository = gameSlotRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.teamSheetService = teamSheetService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlot> getSignupsForGame(Long gameId) {
        Game game = findGameById(gameId);
        return gameSlotRepository.findByGame(game);
    }

    @Override
    @Transactional
    public GameSlot addSignup(Long gameId, Long playerId) {
        Game game = findWithLockingById(gameId);
        validateGameOpen(game);
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
            Player promotedPlayer = promoteNextWaitlistedPlayer(game);
            teamSheetService.handlePublishedSheetDeparture(
                    game, removedSlot.getPlayer(), promotedPlayer);
        }
    }


    private Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
    }

    private Game findWithLockingById(Long gameId){
        return gameRepository.findWithLockingById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
    }

    private Player findPlayerById(Long playerId){
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private GameSlot findSignup(Game game, Player player){
        return gameSlotRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new ResourceNotFoundException("Signup not found for this player/game"));
    }

    private void validatePlayerNotAlreadySignedUp(Game game, Player player){
        boolean exists = gameSlotRepository.existsByGameAndPlayer(game, player);
        if (exists) {
            throw new IllegalStateException("Player already signed up for this game.");
        }
    }

    private void validateGameOpen(Game game) {
        if (isPastGame(game)) {
            game.setStatus(GameStatus.COMPLETED);
            gameRepository.save(game);
        }
        if (game.getStatus() != GameStatus.OPEN) {
            throw new IllegalStateException("Only OPEN games can be joined.");
        }
    }

    private boolean isPastGame(Game game) {
        if (game.getGameDate() == null) {
            return false;
        }
        LocalTime kickOff = game.getKickOffTime() != null ? game.getKickOffTime() : LocalTime.MAX;
        return LocalDateTime.of(game.getGameDate(), kickOff).isBefore(LocalDateTime.now());
    }

    private SlotStatus determineSlotStatus(Game game){
        long confirmedCount = gameSlotRepository.countByGameAndStatus(game, SlotStatus.CONFIRMED);
        return game.determineSignupStatus(confirmedCount);
    }

    private GameSlot createGameSlot(Game game, Player player, SlotStatus status) {
        return GameSlot.create(game, player, status, LocalDateTime.now());
    }

    private Player promoteNextWaitlistedPlayer(Game game){
        return gameSlotRepository
                .findFirstByGameAndStatusOrderBySignedUpAtAsc(game, SlotStatus.WAITLISTED)
                .map(slot -> {
                    slot.setStatus(SlotStatus.CONFIRMED);
                    gameSlotRepository.save(slot);
                    return slot.getPlayer();
                })
                .orElse(null);
    }
}
