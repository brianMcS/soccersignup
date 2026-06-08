package com.soccersignup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeamSheetRequest(
        // The complete list of entries for both teams
        // We replace the entire sheet on each save - simpler than patching individual entries
        @NotNull @Valid List<TeamSheetEntryRequest> entries
) {}
