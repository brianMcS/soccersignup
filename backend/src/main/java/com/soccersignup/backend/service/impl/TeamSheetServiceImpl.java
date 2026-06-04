package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.model.TeamSheet;
import com.soccersignup.backend.repository.TeamSheetRepository;
import com.soccersignup.backend.service.TeamSheetService;
import org.springframework.stereotype.Service;

@Service
public class TeamSheetServiceImpl implements TeamSheetService {
    private final TeamSheetRepository teamSheetRepository;

    public TeamSheetServiceImpl(TeamSheetRepository teamSheetRepository) {
        this.teamSheetRepository = teamSheetRepository;
    }


    @Override
    public TeamSheet createTeamSheet(TeamSheet teamSheet) {
        return teamSheetRepository.save(teamSheet);
    }
}
