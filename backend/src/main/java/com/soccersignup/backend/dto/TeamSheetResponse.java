package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.TeamSheet;

import java.time.LocalDateTime;
import java.util.List;

public record TeamSheetResponse(
        Long id,
        Long gameId,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        List<TeamSheetEntryResponse> entries
) {
    public static TeamSheetResponse from(TeamSheet sheet){
        return new TeamSheetResponse(
                sheet.getId(),
                sheet.getGame().getId(),
                sheet.isPublished(),
                sheet.getPublishedAt(),
                sheet.getUpdatedAt(),
                sheet.getEntries().stream()
                        .map(TeamSheetEntryResponse::from)
                        .toList()
        );
    }
}
