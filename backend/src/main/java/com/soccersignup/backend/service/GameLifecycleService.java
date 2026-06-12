package com.soccersignup.backend.service;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.repository.GameRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class GameLifecycleService {

    private final GameRepository gameRepository;

    public GameLifecycleService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Scheduled(
            initialDelayString = "${app.game-completion.initial-delay-ms:60000}",
            fixedDelayString = "${app.game-completion.interval-ms:60000}")
    @Transactional
    public void completePastGames() {
        List<Game> changedGames = gameRepository.findAll().stream()
                .filter(this::shouldComplete)
                .peek(game -> game.setStatus(GameStatus.COMPLETED))
                .toList();

        if (!changedGames.isEmpty()) {
            gameRepository.saveAll(changedGames);
        }
    }

    private boolean shouldComplete(Game game) {
        if ((game.getStatus() != GameStatus.OPEN
                && game.getStatus() != GameStatus.CLOSED)
                || game.getGameDate() == null) {
            return false;
        }

        LocalTime kickOff = game.getKickOffTime() != null
                ? game.getKickOffTime()
                : LocalTime.MAX;
        return LocalDateTime.of(game.getGameDate(), kickOff)
                .isBefore(LocalDateTime.now());
    }
}
