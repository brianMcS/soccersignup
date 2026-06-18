package com.soccersignup.backend.controller;

import com.soccersignup.backend.dto.TeamSheetRequest;
import com.soccersignup.backend.dto.TeamSheetResponse;
import com.soccersignup.backend.dto.VersionedActionRequest;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.service.TeamSheetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/teamsheet")
public class TeamSheetController {

    private final TeamSheetService teamSheetService;

    public TeamSheetController(TeamSheetService teamSheetService) {
        this.teamSheetService = teamSheetService;
    }

    // GET /api/games/{gameId}/teamsheet
    // Players can call this but only see data once published
    // Organisers can always see the draft
    @GetMapping
    public ResponseEntity<TeamSheetResponse> getTeamSheet(
            @PathVariable Long gameId,
            Authentication authentication) {

        TeamSheetResponse response = teamSheetService.getTeamSheet(gameId);

        // If not published, only organisers/admins can see it
        if (!response.published()) {
            if (authentication == null) {
                return ResponseEntity.notFound().build();
            }
            Player currentPlayer = (Player) authentication.getPrincipal();
            boolean isOrganiser = currentPlayer.hasRole(PlayerRole.ORGANISER)
                    || currentPlayer.hasRole(PlayerRole.ADMIN);
            if (!isOrganiser) {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok(response);
    }

    // POST /api/games/{gameId}/teamsheet/auto-split
    // Randomly splits confirmed players into two teams, saves as draft
    @PostMapping("/auto-split")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<TeamSheetResponse> autoSplit(
            @PathVariable Long gameId,
            @RequestBody(required = false) VersionedActionRequest request) {
        Long expectedVersion = request == null ? null : request.version();
        return ResponseEntity.ok(teamSheetService.autoSplit(gameId, expectedVersion));
    }

    // PUT /api/games/{gameId}/teamsheet
    // Save the full team sheet (organiser drag-and-drop result)
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<TeamSheetResponse> saveTeamSheet(
            @PathVariable Long gameId,
            @Valid @RequestBody TeamSheetRequest request) {
        return ResponseEntity.ok(teamSheetService.saveTeamSheet(gameId, request));
    }

    // POST /api/games/{gameId}/teamsheet/publish
    // Publishes the team sheet and notifies all confirmed players
    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<TeamSheetResponse> publishTeamSheet(
            @PathVariable Long gameId,
            @RequestBody VersionedActionRequest request) {
        return ResponseEntity.ok(teamSheetService.publishTeamSheet(
                gameId, request.version()));
    }
}
