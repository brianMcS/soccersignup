package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.SlotStatus;
import java.time.LocalDateTime;

public record GameSlotResponse(
        Long id,
        String playerName,
        String playerEmail,
        SlotStatus status,
        LocalDateTime signedUpAt)
{
    public static GameSlotResponse from(GameSlot gameSlot) {
        return new GameSlotResponse(
                gameSlot.getId(),
                gameSlot.getPlayer().getName(),
                gameSlot.getPlayer().getEmail(),
                gameSlot.getStatus(),
                gameSlot.getSignedUpAt()
        );
    }
}
