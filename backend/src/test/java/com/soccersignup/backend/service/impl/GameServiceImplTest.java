package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.dto.GameRequest;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameSlotRepository gameSlotRepository;

    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(gameRepository, gameSlotRepository);
    }

    @Test
    void readsDoNotChangeOrSavePastGames() {
        Game pastGame = game(1L, 18);
        pastGame.setGameDate(LocalDate.now().minusDays(1));
        when(gameRepository.findAll()).thenReturn(List.of(pastGame));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(pastGame));
        when(gameRepository.findByStatus(GameStatus.OPEN))
                .thenReturn(List.of(pastGame));

        service.getAllGames();
        service.getGameById(1L);
        service.getGamesByStatus(GameStatus.OPEN);

        assertThat(pastGame.getStatus()).isEqualTo(GameStatus.OPEN);
        verify(gameRepository, never()).save(pastGame);
        verify(gameRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateRejectsCapacityBelowConfirmedCount() {
        Game game = game(1L, 18);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameSlotRepository.countByGameAndStatus(game, SlotStatus.CONFIRMED))
                .thenReturn(12L);

        GameRequest request = new GameRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                "Dublin",
                10,
                new BigDecimal("5.00"),
                null);

        assertThatThrownBy(() -> service.updateGame(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("confirmed player count of 12");
        verify(gameRepository, never()).save(game);
    }

    @Test
    void updateAllowsCapacityEqualToConfirmedCount() {
        Game game = game(1L, 18);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameSlotRepository.countByGameAndStatus(game, SlotStatus.CONFIRMED))
                .thenReturn(12L);
        when(gameRepository.save(game)).thenReturn(game);

        GameRequest request = new GameRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                "Dublin",
                12,
                new BigDecimal("5.00"),
                null);

        Game updated = service.updateGame(1L, request);

        assertThat(updated.getMaxPlayers()).isEqualTo(12);
        verify(gameRepository).save(game);
    }

    private Game game(Long id, int maxPlayers) {
        Game game = new Game();
        game.setId(id);
        game.setGameDate(LocalDate.now().plusDays(1));
        game.setKickOffTime(LocalTime.of(19, 0));
        game.setStatus(GameStatus.OPEN);
        game.setMaxPlayers(maxPlayers);
        return game;
    }
}
