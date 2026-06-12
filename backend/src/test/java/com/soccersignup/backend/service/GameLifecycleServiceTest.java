package com.soccersignup.backend.service;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLifecycleServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Test
    void completesOnlyPastOpenOrClosedGames() {
        Game pastOpen = game(LocalDate.now().minusDays(1), GameStatus.OPEN);
        Game pastClosed = game(LocalDate.now().minusDays(1), GameStatus.CLOSED);
        Game futureOpen = game(LocalDate.now().plusDays(1), GameStatus.OPEN);
        Game cancelled = game(LocalDate.now().minusDays(1), GameStatus.CANCELLED);
        when(gameRepository.findAll())
                .thenReturn(List.of(pastOpen, pastClosed, futureOpen, cancelled));

        new GameLifecycleService(gameRepository).completePastGames();

        assertThat(pastOpen.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(pastClosed.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(futureOpen.getStatus()).isEqualTo(GameStatus.OPEN);
        assertThat(cancelled.getStatus()).isEqualTo(GameStatus.CANCELLED);
        verify(gameRepository).saveAll(List.of(pastOpen, pastClosed));
    }

    @Test
    void doesNotSaveWhenNoGamesNeedCompletion() {
        Game futureOpen = game(LocalDate.now().plusDays(1), GameStatus.OPEN);
        when(gameRepository.findAll()).thenReturn(List.of(futureOpen));

        new GameLifecycleService(gameRepository).completePastGames();

        verify(gameRepository, never()).saveAll(
                org.mockito.ArgumentMatchers.any());
    }

    private Game game(LocalDate date, GameStatus status) {
        Game game = new Game();
        game.setGameDate(date);
        game.setKickOffTime(LocalTime.NOON);
        game.setStatus(status);
        return game;
    }
}
