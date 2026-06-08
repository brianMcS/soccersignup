package com.soccersignup.backend.service.strategy.impl;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.service.strategy.TeamSheetRandomizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CollectionsTeamSheetRandomizer implements TeamSheetRandomizer {

    @Override
    public List<GameSlot> randomize(List<GameSlot> slots) {
        List<GameSlot> randomizedSlots = new ArrayList<>(slots);
        Collections.shuffle(randomizedSlots);
        return randomizedSlots;
    }
}
