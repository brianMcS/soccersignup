package com.soccersignup.backend.service.strategy.impl;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.model.TeamSheet;
import com.soccersignup.backend.model.TeamSheetEntry;
import com.soccersignup.backend.model.TeamSide;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BalancedTeamAssignmentPolicyTest {

    private final BalancedTeamAssignmentPolicy policy =
            new BalancedTeamAssignmentPolicy();

    @Test
    void assignBalancesTeamsAndGivesHomeTheExtraPlayer() {
        Game game = new Game();
        TeamSheet sheet = new TeamSheet();
        sheet.setGame(game);
        List<GameSlot> slots = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> createSlot(game, index))
                .toList();

        List<TeamSheetEntry> entries = policy.assign(sheet, slots);

        assertThat(entries).hasSize(5);
        assertThat(entries.subList(0, 3))
                .extracting(TeamSheetEntry::getTeamSide)
                .containsOnly(TeamSide.HOME);
        assertThat(entries.subList(3, 5))
                .extracting(TeamSheetEntry::getTeamSide)
                .containsOnly(TeamSide.AWAY);
        assertThat(entries)
                .extracting(TeamSheetEntry::getJerseyNumber)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(entries)
                .allSatisfy(entry -> assertThat(entry.getTeamSheet()).isSameAs(sheet));
    }

    @Test
    void assignPlacesPlayersEvenlyOnTheirSideOfThePitch() {
        Game game = new Game();
        TeamSheet sheet = new TeamSheet();
        sheet.setGame(game);
        List<GameSlot> slots = IntStream.rangeClosed(1, 4)
                .mapToObj(index -> createSlot(game, index))
                .toList();

        List<TeamSheetEntry> entries = policy.assign(sheet, slots);

        assertThat(entries)
                .extracting(TeamSheetEntry::getPositionX)
                .containsExactly(25.0, 25.0, 75.0, 75.0);
        assertThat(entries)
                .extracting(TeamSheetEntry::getPositionY)
                .containsExactly(
                        100.0 / 3.0,
                        200.0 / 3.0,
                        100.0 / 3.0,
                        200.0 / 3.0);
    }

    private GameSlot createSlot(Game game, int playerId) {
        Player player = new Player();
        player.setId((long) playerId);
        return GameSlot.create(
                game,
                player,
                SlotStatus.CONFIRMED,
                LocalDateTime.now());
    }
}
