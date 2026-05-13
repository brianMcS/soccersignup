package com.soccersignup.backend.service.imp;

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
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Game not found" + gameId));
        return gameSlotRepository.findByGame(game);
    }

    @Override
    @Transactional
    public GameSlot addSignup(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId).
                orElseThrow(()-> new IllegalArgumentException("Game not found" + gameId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(()-> new IllegalArgumentException("Player not found" + playerId));

        List<GameSlot> currentSignups = gameSlotRepository.findByGame(game);
        if (currentSignups.size() >= game.getMaxPlayers()) {
            throw new IllegalStateException("Game is full (" + game.getMaxPlayers() + " players max)");
        }

        // Prevent duplicate signup by same player
        boolean alreadySignedUp = gameSlotRepository.findByGameAndPlayer(game, player).isPresent();
        if (alreadySignedUp) {
            throw new IllegalStateException("Player already signed up.");
        }

        GameSlot slot = new GameSlot();
        slot.setGame(game);
        slot.setPlayer(player);
        slot.setStatus(SlotStatus.CONFIRMED);
        slot.setSignedUpAt(LocalDateTime.now());

        return gameSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public void removeSignup(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found " + playerId));

        gameSlotRepository.findByGameAndPlayer(game, player)
                .ifPresent(gameSlotRepository::delete);
    }
}
