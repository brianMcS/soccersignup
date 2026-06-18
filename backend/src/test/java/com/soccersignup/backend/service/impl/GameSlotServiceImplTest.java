package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.exception.ConcurrencyConflictException;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.PaymentStatus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void reportPaymentRejectsStaleVersion() {
        Game game = createGame(1L);
        Player player = createPlayer(1L, "Alex");
        GameSlot slot = createSlot(game, player, SlotStatus.CONFIRMED);
        slot.setVersion(3L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGameAndPlayer(game, player))
                .thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service.reportPayment(1L, 1L, 2L))
                .isInstanceOf(ConcurrencyConflictException.class)
                .hasMessage("This signup was updated by someone else. Refresh and try again.");

        assertThat(slot.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        verify(gameSlotRepository, never()).save(any());
    }

    @Test
    void confirmPaymentUsesExpectedVersion() {
        Game game = createGame(1L);
        Player player = createPlayer(1L, "Alex");
        Player organiser = createPlayer(2L, "Jordan");
        GameSlot slot = createSlot(game, player, SlotStatus.CONFIRMED);
        slot.setVersion(3L);
        slot.setPaymentStatus(PaymentStatus.SELF_REPORTED);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGameAndPlayer(game, player))
                .thenReturn(Optional.of(slot));
        when(gameSlotRepository.save(slot)).thenReturn(slot);

        GameSlot updated = service.confirmPayment(1L, 1L, organiser, 3L);

        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(updated.getConfirmedBy()).isSameAs(organiser);
        assertThat(updated.getConfirmedAt()).isNotNull();
        verify(gameSlotRepository).save(slot);
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
