package com.soccersignup.backend.service.strategy;

import com.soccersignup.backend.model.GameSlot;

import java.util.List;

public interface TeamSheetRandomizer {

    List<GameSlot> randomize(List<GameSlot> slots);
}
