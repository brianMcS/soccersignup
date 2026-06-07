package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.dto.TeamSheetEntryRequest;
import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.dto.TeamSheetResponse;
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
import com.soccersignup.backend.service.TeamSheetService;
import com.soccersignup.backend.service.strategy.TeamAssignmentPolicy;
import com.soccersignup.backend.service.strategy.TeamSheetRandomizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TeamSheetServiceImpl implements TeamSheetService {

    private static final String NOTIFICATION_LINK = "/play";

    private final TeamSheetRepository teamSheetRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameSlotRepository gameSlotRepository;
    private final NotificationRepository notificationRepository;
    private final TeamSheetRandomizer teamSheetRandomizer;
    private final TeamAssignmentPolicy teamAssignmentPolicy;

    public TeamSheetServiceImpl(
            TeamSheetRepository teamSheetRepository,
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            GameSlotRepository gameSlotRepository,
            NotificationRepository notificationRepository,
            TeamSheetRandomizer teamSheetRandomizer,
            TeamAssignmentPolicy teamAssignmentPolicy) {
        this.teamSheetRepository = teamSheetRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameSlotRepository = gameSlotRepository;
        this.notificationRepository = notificationRepository;
        this.teamSheetRandomizer = teamSheetRandomizer;
        this.teamAssignmentPolicy = teamAssignmentPolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public TeamSheetResponse getTeamSheet(Long gameId) {
        Game game = findGameById(gameId);
        return toResponse(findTeamSheet(game));
    }

    @Override
    @Transactional
    public TeamSheetResponse autoSplit(Long gameId) {
        Game game = findGameById(gameId);
        List<GameSlot> confirmedSlots = findConfirmedSlots(game);
        if (confirmedSlots.isEmpty()) {
            throw new IllegalStateException("No confirmed players to split for game: " + gameId);
        }

        List<GameSlot> randomizedSlots = teamSheetRandomizer.randomize(confirmedSlots);
        TeamSheet sheet = findOrCreateTeamSheet(game);
        List<TeamSheetEntry> entries = teamAssignmentPolicy.assign(sheet, randomizedSlots);

        sheet.getEntries().clear();
        sheet.getEntries().addAll(entries);
        markAsDraft(sheet);
        return saveAndConvert(sheet);
    }

    @Override
    @Transactional
    public TeamSheetResponse saveTeamSheet(Long gameId, TeamSheetRequest request) {
        Game game = findGameById(gameId);
        TeamSheet sheet = findOrCreateTeamSheet(game);
        List<TeamSheetEntry> replacementEntries = createEntries(sheet, game, request);

        sheet.getEntries().clear();
        sheet.getEntries().addAll(replacementEntries);

        return saveAndConvert(sheet);
    }

    @Override
    @Transactional
    public TeamSheetResponse publishTeamSheet(Long gameId) {
        Game game = findGameById(gameId);
        TeamSheet sheet = findTeamSheet(game);
        validateNotEmpty(sheet);
        sheet.setPublished(true);
        sheet.setPublishedAt(LocalDateTime.now());
        teamSheetRepository.save(sheet);

        notificationRepository.deleteByGameAndReadFalse(game);
        notificationRepository.saveAll(createNotifications(game));

        return toResponse(sheet);
    }

    private Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game not found with id: " + gameId));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found: " + playerId));
    }

    private TeamSheet findTeamSheet(Game game) {
        return teamSheetRepository.findByGame(game)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team sheet not found for game: " + game.getId()));
    }

    private TeamSheet findOrCreateTeamSheet(Game game) {
        return teamSheetRepository.findByGame(game)
                .orElseGet(() -> createTeamSheet(game));
    }

    private TeamSheet createTeamSheet(Game game) {
        TeamSheet sheet = new TeamSheet();
        sheet.setGame(game);
        return sheet;
    }

    private List<GameSlot> findConfirmedSlots(Game game) {
        return gameSlotRepository.findByGameAndStatus(game, SlotStatus.CONFIRMED);
    }

    private TeamSheetEntry createEntry(
            TeamSheet sheet,
            Player player,
            TeamSheetEntryRequest request) {
        TeamSheetEntry entry = new TeamSheetEntry();
        entry.setTeamSheet(sheet);
        entry.setPlayer(player);
        entry.setTeamSide(request.teamSide());
        entry.setJerseyNumber(request.jerseyNumber());
        entry.setPositionX(request.positionX());
        entry.setPositionY(request.positionY());
        return entry;
    }

    private List<TeamSheetEntry> createEntries(
            TeamSheet sheet,
            Game game,
            TeamSheetRequest request) {
        Set<Long> playerIds = new HashSet<>();
        List<TeamSheetEntry> entries = new ArrayList<>();

        for (TeamSheetEntryRequest entryRequest : request.entries()) {
            validatePositionOnTeamSide(entryRequest);

            if (!playerIds.add(entryRequest.playerId())) {
                throw new IllegalArgumentException(
                        "Player appears more than once on the team sheet: "
                                + entryRequest.playerId());
            }

            Player player = findPlayerById(entryRequest.playerId());
            validateConfirmedPlayer(game, player);
            entries.add(createEntry(sheet, player, entryRequest));
        }

        return entries;
    }

    private void validatePositionOnTeamSide(TeamSheetEntryRequest request) {
        boolean crossesHalfwayLine =
                request.teamSide() == TeamSide.HOME && request.positionX() > 50
                || request.teamSide() == TeamSide.AWAY && request.positionX() < 50;

        if (crossesHalfwayLine) {
            throw new IllegalArgumentException(
                    "Players must remain on their team's side of the halfway line.");
        }
    }

    private void validateConfirmedPlayer(Game game, Player player) {
        boolean isConfirmed = gameSlotRepository.existsByGameAndPlayerAndStatus(
                game, player, SlotStatus.CONFIRMED);
        if (!isConfirmed) {
            throw new IllegalStateException(
                    "Player " + player.getName() + " is not confirmed for this game.");
        }
    }

    private void validateNotEmpty(TeamSheet sheet) {
        if (sheet.getEntries().isEmpty()) {
            throw new IllegalStateException("Cannot publish an empty team sheet.");
        }
    }

    private void markAsDraft(TeamSheet sheet) {
        sheet.setPublished(false);
        sheet.setPublishedAt(null);
    }

    private List<Notification> createNotifications(Game game) {
        String message = "Teams have been announced for " + game.getGameDate()
                + "! Check which team you're on.";
        return findConfirmedSlots(game).stream()
                .map(slot -> createNotification(slot.getPlayer(), game, message))
                .toList();
    }

    private Notification createNotification(Player player, Game game, String message) {
        Notification notification = new Notification();
        notification.setPlayer(player);
        notification.setGame(game);
        notification.setMessage(message);
        notification.setLink(NOTIFICATION_LINK);
        return notification;
    }

    private TeamSheetResponse saveAndConvert(TeamSheet sheet) {
        return toResponse(teamSheetRepository.save(sheet));
    }

    private TeamSheetResponse toResponse(TeamSheet sheet) {
        return TeamSheetResponse.from(sheet);
    }
}
