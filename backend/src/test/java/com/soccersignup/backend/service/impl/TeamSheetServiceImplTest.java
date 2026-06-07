package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.dto.TeamSheetEntryRequest;
import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.exception.ResourceNotFoundException;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Notification;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.model.TeamSheet;
import com.soccersignup.backend.model.TeamSheetEntry;
import com.soccersignup.backend.model.TeamSide;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.NotificationRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.repository.TeamSheetRepository;
import com.soccersignup.backend.service.strategy.TeamAssignmentPolicy;
import com.soccersignup.backend.service.strategy.TeamSheetRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamSheetServiceImplTest {

    @Mock
    private TeamSheetRepository teamSheetRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameSlotRepository gameSlotRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TeamSheetRandomizer teamSheetRandomizer;
    @Mock
    private TeamAssignmentPolicy teamAssignmentPolicy;

    private TeamSheetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TeamSheetServiceImpl(
                teamSheetRepository,
                gameRepository,
                playerRepository,
                gameSlotRepository,
                notificationRepository,
                teamSheetRandomizer,
                teamAssignmentPolicy);
    }

    @Test
    void getTeamSheetThrowsResourceNotFoundWhenGameDoesNotExist() {
        when(gameRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTeamSheet(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Game not found with id: 42");
    }

    @Test
    void saveTeamSheetRejectsPlayerWhoIsNotConfirmed() {
        Game game = createGame(1L);
        Player player = createPlayer(2L, "Alex");
        TeamSheetRequest request = new TeamSheetRequest(List.of(
                new TeamSheetEntryRequest(2L, TeamSide.HOME, 7, 25, 50)));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(teamSheetRepository.findByGame(game)).thenReturn(Optional.empty());
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.existsByGameAndPlayerAndStatus(
                game, player, SlotStatus.CONFIRMED)).thenReturn(false);

        assertThatThrownBy(() -> service.saveTeamSheet(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Player Alex is not confirmed for this game.");

        verify(teamSheetRepository, never()).save(any());
    }

    @Test
    void saveTeamSheetRejectsHomePlayerBeyondHalfwayLine() {
        Game game = createGame(1L);
        TeamSheetRequest request = new TeamSheetRequest(List.of(
                new TeamSheetEntryRequest(2L, TeamSide.HOME, 7, 51, 50)));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(teamSheetRepository.findByGame(game)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveTeamSheet(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Players must remain on their team's side of the halfway line.");

        verify(playerRepository, never()).findById(any());
        verify(teamSheetRepository, never()).save(any());
    }

    @Test
    void saveTeamSheetRejectsAwayPlayerBeforeHalfwayLine() {
        Game game = createGame(1L);
        TeamSheetRequest request = new TeamSheetRequest(List.of(
                new TeamSheetEntryRequest(2L, TeamSide.AWAY, 7, 49, 50)));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(teamSheetRepository.findByGame(game)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveTeamSheet(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Players must remain on their team's side of the halfway line.");

        verify(playerRepository, never()).findById(any());
        verify(teamSheetRepository, never()).save(any());
    }

    @Test
    void autoSplitReplacesEntriesAndResetsPublishedState() {
        Game game = createGame(1L);
        TeamSheet sheet = createSheet(game);
        sheet.setPublished(true);
        sheet.setPublishedAt(LocalDateTime.now());
        sheet.getEntries().add(new TeamSheetEntry());

        Player firstPlayer = createPlayer(1L, "Alex");
        Player secondPlayer = createPlayer(2L, "Sam");
        GameSlot firstSlot = createSlot(game, firstPlayer);
        GameSlot secondSlot = createSlot(game, secondPlayer);
        List<GameSlot> randomizedSlots = List.of(secondSlot, firstSlot);
        TeamSheetEntry homeEntry = createEntry(sheet, secondPlayer);
        TeamSheetEntry awayEntry = createEntry(sheet, firstPlayer);
        awayEntry.setTeamSide(TeamSide.AWAY);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameSlotRepository.findByGameAndStatus(game, SlotStatus.CONFIRMED))
                .thenReturn(List.of(firstSlot, secondSlot));
        when(teamSheetRandomizer.randomize(List.of(firstSlot, secondSlot)))
                .thenReturn(randomizedSlots);
        when(teamSheetRepository.findByGame(game)).thenReturn(Optional.of(sheet));
        when(teamAssignmentPolicy.assign(sheet, randomizedSlots))
                .thenReturn(List.of(homeEntry, awayEntry));
        when(teamSheetRepository.save(sheet)).thenReturn(sheet);

        service.autoSplit(1L);

        assertThat(sheet.isPublished()).isFalse();
        assertThat(sheet.getPublishedAt()).isNull();
        assertThat(sheet.getEntries()).hasSize(2);
        assertThat(sheet.getEntries())
                .extracting(TeamSheetEntry::getTeamSide)
                .containsExactlyInAnyOrder(TeamSide.HOME, TeamSide.AWAY);
        verify(teamSheetRandomizer).randomize(List.of(firstSlot, secondSlot));
        verify(teamAssignmentPolicy).assign(sheet, randomizedSlots);
    }

    @Test
    void publishTeamSheetReplacesUnreadNotificationsInOneBatch() {
        Game game = createGame(1L);
        game.setGameDate(LocalDate.of(2026, 6, 12));
        TeamSheet sheet = createSheet(game);
        sheet.getEntries().add(createEntry(sheet, createPlayer(1L, "Alex")));

        Player firstPlayer = createPlayer(1L, "Alex");
        Player secondPlayer = createPlayer(2L, "Sam");
        GameSlot firstSlot = createSlot(game, firstPlayer);
        GameSlot secondSlot = createSlot(game, secondPlayer);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(teamSheetRepository.findByGame(game)).thenReturn(Optional.of(sheet));
        when(gameSlotRepository.findByGameAndStatus(game, SlotStatus.CONFIRMED))
                .thenReturn(List.of(firstSlot, secondSlot));

        service.publishTeamSheet(1L);

        verify(notificationRepository).deleteByGameAndReadFalse(game);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> notificationsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(notificationsCaptor.capture());

        assertThat(notificationsCaptor.getValue()).hasSize(2);
        assertThat(notificationsCaptor.getValue())
                .extracting(Notification::getPlayer)
                .containsExactly(firstPlayer, secondPlayer);
        assertThat(sheet.isPublished()).isTrue();
        assertThat(sheet.getPublishedAt()).isNotNull();
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

    private TeamSheet createSheet(Game game) {
        TeamSheet sheet = new TeamSheet();
        sheet.setGame(game);
        return sheet;
    }

    private GameSlot createSlot(Game game, Player player) {
        return GameSlot.create(game, player, SlotStatus.CONFIRMED, LocalDateTime.now());
    }

    private TeamSheetEntry createEntry(TeamSheet sheet, Player player) {
        TeamSheetEntry entry = new TeamSheetEntry();
        entry.setTeamSheet(sheet);
        entry.setPlayer(player);
        entry.setTeamSide(TeamSide.HOME);
        return entry;
    }
}
