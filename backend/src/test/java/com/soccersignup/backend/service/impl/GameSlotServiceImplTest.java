package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.TeamSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSlotServiceImplTest {

    @Mock
    private GameSlotRepository gameSlotRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private TeamSheetService teamSheetService;

    private GameSlotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameSlotServiceImpl(
                gameSlotRepository,
                playerRepository,
                gameRepository,
                teamSheetService);
    }

    @Test
    void confirmedDeparturePromotesWaitlistedPlayerAndUpdatesPublishedSheet() {
        Game game = createGame(1L);
        Player leavingPlayer = createPlayer(1L, "Alex");
        Player promotedPlayer = createPlayer(2L, "Jordan");
        GameSlot leavingSlot = createSlot(
                game, leavingPlayer, SlotStatus.CONFIRMED);
        GameSlot waitlistedSlot = createSlot(
                game, promotedPlayer, SlotStatus.WAITLISTED);

        when(gameRepository.findWithLockingById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(leavingPlayer));
        when(gameSlotRepository.findByGameAndPlayer(game, leavingPlayer))
                .thenReturn(Optional.of(leavingSlot));
        when(gameSlotRepository
                .findFirstByGameAndStatusOrderBySignedUpAtAsc(
                        game, SlotStatus.WAITLISTED))
                .thenReturn(Optional.of(waitlistedSlot));

        service.removeSignup(1L, 1L);

        assertThat(waitlistedSlot.getStatus()).isEqualTo(SlotStatus.CONFIRMED);
        verify(gameSlotRepository).delete(leavingSlot);
        verify(gameSlotRepository).save(waitlistedSlot);
        verify(gameRepository).findWithLockingById(1L);
        verify(teamSheetService).handlePublishedSheetDeparture(
                game, leavingPlayer, promotedPlayer);
    }

    private Game createGame(Long id) {
        Game game = new Game();
        game.setId(id);
        return game;
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        return player;
    }

    private GameSlot createSlot(
            Game game,
            Player player,
            SlotStatus status) {
        return GameSlot.create(game, player, status, LocalDateTime.now());
    }
}
