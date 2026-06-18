package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.dto.TeamSheetResponse;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.Player;

public interface TeamSheetService {

    TeamSheetResponse getTeamSheet(Long gameId);

    TeamSheetResponse autoSplit(Long gameId, Long expectedVersion);

    TeamSheetResponse saveTeamSheet(Long gameId, TeamSheetRequest request);

    TeamSheetResponse publishTeamSheet(Long gameId, Long expectedVersion);

    void handlePublishedSheetDeparture(
            Game game,
            Player leavingPlayer,
            Player promotedPlayer);
}
