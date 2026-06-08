package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.TeamSheetEntry;
import com.soccersignup.backend.model.TeamSide;

public record TeamSheetEntryResponse(
        Long id,
        Long playerId,
        String playerName,
        TeamSide teamSide,
        int jerseyNumber,
        double positionX,
        double positionY
) {
    public static TeamSheetEntryResponse from (TeamSheetEntry entry){
        return new TeamSheetEntryResponse(
                entry.getId(),
                entry.getPlayer().getId(),
                entry.getPlayer().getName(),
                entry.getTeamSide(),
                entry.getJerseyNumber(),
                entry.getPositionX(),
                entry.getPositionY()
        );
    }
}
