package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.dto.TeamSheetResponse;
import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.Player;

public interface TeamSheetService {

    TeamSheetResponse getTeamSheet(Long gameId);

    TeamSheetResponse autoSplit(Long gameId);

    TeamSheetResponse saveTeamSheet(Long gameId, TeamSheetRequest request);

    TeamSheetResponse publishTeamSheet(Long gameId);

    void handlePublishedSheetDeparture(
            Game game,
            Player leavingPlayer,
            Player promotedPlayer);
}
