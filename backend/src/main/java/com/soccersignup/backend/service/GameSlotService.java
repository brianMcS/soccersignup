package com.soccersignup.backend.service;

import java.util.List;

import com.soccersignup.backend.model.GameSlot;
public interface GameSlotService {
    List<GameSlot> getSignupsForGame(Long gameId);

    GameSlot addSignup(Long gameId, Long playerId);

    void removeSignup(Long gameId, Long playerId);
}
