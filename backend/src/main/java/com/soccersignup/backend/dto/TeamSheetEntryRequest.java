package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.TeamSide;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TeamSheetEntryRequest(
        @NotNull Long playerId,

        @NotNull TeamSide teamSide,

        @Min(1) @Max(99) int jerseyNumber,

        // Percentage of pitch width (0.0 - 100.0)
        @Min(0) @Max(100) double positionX,

        // Percentage of pitch height (0.0 - 100.0)
        @Min(0) @Max(100) double positionY,

        // Optional e.g. "GK", "ST" - can be null
        String positionLabel
) { }
