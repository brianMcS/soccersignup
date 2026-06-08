package com.soccersignup.backend.service.strategy;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.TeamSheet;
import com.soccersignup.backend.model.TeamSheetEntry;

import java.util.List;

public interface TeamAssignmentPolicy {

    List<TeamSheetEntry> assign(TeamSheet sheet, List<GameSlot> orderedSlots);
}
