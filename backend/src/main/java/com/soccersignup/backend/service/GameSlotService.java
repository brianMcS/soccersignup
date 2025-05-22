package com.soccersignup.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;

public interface GameSlotService {
    List<GameSlot> getSignupsForWeek(LocalDate gameDate);

    GameSlot addSignup(GameSlot slot);

    void removeSignup(Player player, LocalDate gameDate);
}
