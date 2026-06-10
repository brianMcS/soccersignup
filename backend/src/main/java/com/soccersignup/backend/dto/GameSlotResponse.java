package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.PaymentStatus;
import com.soccersignup.backend.model.SlotStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GameSlotResponse(
        Long id,
        Long playerId,
        String playerName,
        String playerEmail,
        SlotStatus status,
        LocalDateTime signedUpAt,
        PaymentStatus paymentStatus,
        BigDecimal feeAmount,
        LocalDateTime paidAt,
        LocalDateTime confirmedAt)
{
    public static GameSlotResponse from(GameSlot gameSlot) {
        return new GameSlotResponse(
                gameSlot.getId(),
                gameSlot.getPlayer().getId(),
                gameSlot.getPlayer().getName(),
                gameSlot.getPlayer().getEmail(),
                gameSlot.getStatus(),
                gameSlot.getSignedUpAt(),
                gameSlot.getPaymentStatus(),
                gameSlot.getFeeAmount(),
                gameSlot.getPaidAt(),
                gameSlot.getConfirmedAt()
        );
    }
}
