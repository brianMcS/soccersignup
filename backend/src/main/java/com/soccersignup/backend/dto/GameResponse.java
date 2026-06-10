package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameStatus;
import com.soccersignup.backend.model.SlotStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record GameResponse(
        Long id,
        LocalDate gameDate,
        LocalTime kickOffTime,
        String location,
        int maxPlayers,
        int confirmedCount,
        int waitlistedCount,
        GameStatus status,
        BigDecimal feeAmount,
        String revolutLink
        ) {
    public static GameResponse from(Game game) {
        long confirmed = game.getSlots().stream()
                .filter(s -> s.getStatus() == SlotStatus.CONFIRMED).count();
        long waitlisted = game.getSlots().stream()
                .filter(s -> s.getStatus() == SlotStatus.WAITLISTED).count();
        return new GameResponse(
                game.getId(), game.getGameDate(), game.getKickOffTime(), game.getLocation(),
                game.getMaxPlayers(), (int) confirmed, (int) waitlisted, game.getStatus(),
                game.getFeeAmount(), game.getRevolutLink()
        );
    }
}
