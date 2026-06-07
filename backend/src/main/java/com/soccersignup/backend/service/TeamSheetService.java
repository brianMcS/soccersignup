package com.soccersignup.backend.service;

import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.dto.TeamSheetResponse;

public interface TeamSheetService {

    TeamSheetResponse getTeamSheet(Long gameId);

    TeamSheetResponse autoSplit(Long gameId);

    TeamSheetResponse saveTeamSheet(Long gameId, TeamSheetRequest request);

    TeamSheetResponse publishTeamSheet(Long gameId);
}
