package com.soccersignup.backend.service;

import java.util.List;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
public interface GameSlotService {
    List<GameSlot> getSignupsForGame(Long gameId);

    GameSlot addSignup(Long gameId, Long playerId);

    void removeSignup(Long gameId, Long playerId);

    GameSlot reportPayment(Long gameId, Long playerId);

    GameSlot confirmPayment(Long gameId, Long playerId, Player confirmedBy);

    GameSlot rejectPayment(Long gameId, Long playerId);
}
