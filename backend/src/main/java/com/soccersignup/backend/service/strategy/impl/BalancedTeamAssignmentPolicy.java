package com.soccersignup.backend.service.strategy.impl;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.TeamSheet;
import com.soccersignup.backend.model.TeamSheetEntry;
import com.soccersignup.backend.model.TeamSide;
import com.soccersignup.backend.service.strategy.TeamAssignmentPolicy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BalancedTeamAssignmentPolicy implements TeamAssignmentPolicy {

    private static final double HOME_POSITION_X = 25.0;
    private static final double AWAY_POSITION_X = 75.0;
    private static final double PITCH_HEIGHT = 100.0;

    @Override
    public List<TeamSheetEntry> assign(TeamSheet sheet, List<GameSlot> orderedSlots) {
        int homeTeamSize = (orderedSlots.size() + 1) / 2;
        List<TeamSheetEntry> entries = new ArrayList<>(orderedSlots.size());

        for (int index = 0; index < orderedSlots.size(); index++) {
            TeamSide side = index < homeTeamSize ? TeamSide.HOME : TeamSide.AWAY;
            entries.add(createEntry(
                    sheet,
                    orderedSlots.get(index),
                    side,
                    index,
                    homeTeamSize,
                    orderedSlots.size()));
        }

        return entries;
    }

    private TeamSheetEntry createEntry(
            TeamSheet sheet,
            GameSlot slot,
            TeamSide side,
            int index,
            int homeTeamSize,
            int playerCount) {
        int teamIndex = side == TeamSide.HOME ? index : index - homeTeamSize;
        int teamSize = side == TeamSide.HOME ? homeTeamSize : playerCount - homeTeamSize;

        TeamSheetEntry entry = new TeamSheetEntry();
        entry.setTeamSheet(sheet);
        entry.setPlayer(slot.getPlayer());
        entry.setTeamSide(side);
        entry.setJerseyNumber(index + 1);
        entry.setPositionX(side == TeamSide.HOME ? HOME_POSITION_X : AWAY_POSITION_X);
        entry.setPositionY(calculatePositionY(teamIndex, teamSize));
        return entry;
    }

    private double calculatePositionY(int teamIndex, int teamSize) {
        return PITCH_HEIGHT / (teamSize + 1) * (teamIndex + 1);
    }
}
